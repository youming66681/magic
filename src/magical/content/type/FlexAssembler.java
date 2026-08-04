package magical.content;

import arc.Core;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.PayloadStack;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.units.*;
import mindustry.world.blocks.units.UnitAssemblerModule.UnitAssemblerModuleBuild;
import mindustry.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

public class FlexAssembler extends UnitAssembler {

    public Map<AssemblerUnitPlan, Integer> planAreaMap = new HashMap<>();
    public Map<AssemblerUnitPlan, Integer> tierRequired = new HashMap<>();

    public FlexAssembler(String name) {
        super(name);
        configurable = true;
    }

    public void addPlan(String label, UnitType output, float time, int customArea, int requiredTier, PayloadStack... requirements) {
        Seq<PayloadStack> reqSeq = new Seq<>(requirements);
        AssemblerUnitPlan plan = new AssemblerUnitPlan(output, time, reqSeq);
        plans.add(plan);
        tierRequired.put(plan, requiredTier);
        planAreaMap.put(plan, customArea);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.output);
        stats.add(Stat.output, table -> {
            table.row();
            Map<Integer, Seq<AssemblerUnitPlan>> byTier = new HashMap<>();
            for (AssemblerUnitPlan plan : plans) {
                int tier = tierRequired.getOrDefault(plan, 0);
                byTier.computeIfAbsent(tier, k -> new Seq<>()).add(plan);
            }
            for (int tier = 0; tier <= byTier.keySet().stream().max(Integer::compareTo).orElse(0); tier++) {
                Seq<AssemblerUnitPlan> group = byTier.get(tier);
                if (group == null || group.isEmpty()) continue;
                final int currentTier = tier;
                table.table(Tex.pane, t ->
                        t.add(Core.bundle.format("flexassembler.tier.stat", currentTier)).pad(5).left().growX()
                ).growX().pad(5).row();
                for (AssemblerUnitPlan plan : group) {
                    table.table(Tex.pane, t -> {
                        if (plan.unit.isBanned()) {
                            t.image(Icon.cancel).color(Pal.remove).size(40).pad(10);
                            return;
                        }
                        if (plan.unit.unlockedNow()) {
                            t.image(plan.unit.uiIcon).scaling(Scaling.fit).size(40).pad(10f).left();
                            t.table(info -> {
                                info.left();
                                info.add(plan.unit.localizedName).left();
                                info.row();
                                info.add(Strings.autoFixed(plan.time / 60f, 1) + " " + Core.bundle.get("unit.seconds")).color(Color.lightGray).left();
                                if (tierRequired.getOrDefault(plan, 0) > 0) {
                                    info.row();
                                    info.add(Core.bundle.format("flexassembler.tier.stat", tierRequired.get(plan))).color(Color.lightGray).left();
                                }
                                info.row();
                                info.add(Core.bundle.format("flexassembler.area.stat", planAreaMap.getOrDefault(plan, areaSize))).color(Color.lightGray).left();
                            }).left();
                            t.table(req -> {
                                for (int i = 0; i < plan.requirements.size; i++) {
                                    if (i % 4 == 0) req.row();
                                    req.add(StatValues.stack(plan.requirements.get(i))).pad(5);
                                }
                            }).right();
                        } else {
                            t.image(Icon.lock).color(Pal.darkerGray).size(40).pad(10);
                        }
                    }).growX().pad(5).row();
                }
            }
        });
    }

    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        private static final int NO_PLAN = -1;
        private int lockedIndex = 0;
        private int customArea;
        private AssemblerUnitPlan curPlan;

        private AssemblerUnitPlan getLockedPlan() {
            if (lockedIndex >= 0 && lockedIndex < plans.size) {
                return plans.get(lockedIndex);
            }
            return null;
        }

        private void setPlan(int index) {
            if (plans.isEmpty()) {
                curPlan = null;
                return;
            }
            if (index >= 0 && index < plans.size) {
                lockedIndex = index;
            } else {
                lockedIndex = 0;
            }
            curPlan = plans.get(lockedIndex);
            syncArea();
        }

        private void syncArea() {
            if (curPlan != null) {
                customArea = planAreaMap.getOrDefault(curPlan, areaSize);
            } else {
                customArea = areaSize;
            }
        }

        @Override
        public void created() {
            super.created();
            setPlan(lockedIndex);
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            modules.clear();
            for (Building other : proximity) {
                if (other instanceof UnitAssemblerModuleBuild mod) {
                    modules.add(mod);
                }
            }
            checkTier();
        }

        @Override
        public void buildConfiguration(Table table) {
            if (Core.app.isHeadless()) return;

            AssemblerUnitPlan current = plan();

            table.label(() ->
                    Core.bundle.format(
                            "flexassembler.current",
                            current.unit.localizedName
                    )
            ).row();

            Table cont = new Table();

            int count = 0;

            for (AssemblerUnitPlan plan : plans) {

                int index = plans.indexOf(plan);

                boolean selected = curPlan == plan;

                if (count % 4 == 0) {
                    cont.row();
                }

                Button b = new Button(Tex.button);

                b.table(t -> {
                    t.image(plan.unit.uiIcon).size(36);
                    t.row();
                    t.add(plan.unit.localizedName)
                            .color(selected ? Pal.accent : Color.white);
                });

                b.clicked(() -> {
                    configure(index);
                });

                cont.add(b)
                        .size(90, 90)
                        .pad(4);

                count++;
            }

            table.add(new ScrollPane(cont))
                    .grow()
                    .row();
        }

        @Override
        public Object config() {
            return lockedIndex;
        }

        @Override
        public void configure(@Nullable Object value) {

            if (!(value instanceof Integer)) {
                return;
            }

            int index = (Integer) value;

            setPlan(index);
        }


        @Override
        public AssemblerUnitPlan plan() {

            if (curPlan != null) {
                return curPlan;
            }

            if (lockedIndex >= 0 && lockedIndex < plans.size) {
                curPlan = plans.get(lockedIndex);
                return curPlan;
            }

            if (!plans.isEmpty()) {
                curPlan = plans.get(0);
                lockedIndex = 0;
                return curPlan;
            }

            return super.plan();
        }


        @Override
        public boolean shouldConsume() {

            AssemblerUnitPlan plan = plan();

            if (plan == null) {
                return false;
            }

            int tier = tierRequired.getOrDefault(plan, 0);

            if (tier > currentTier) {
                return false;
            }

            return super.shouldConsume();
        }


        @Override
        public void updateTile() {
            syncArea();
            super.updateTile();
        }


        @Override
        public Vec2 getUnitSpawn() {

            float len =
                    tilesize * (customArea + block.size) / 2f;

            return Tmp.v4.set(
                    x + Geometry.d4x(rotation) * len,
                    y + Geometry.d4y(rotation) * len
            );
        }


        @Override
        public void write(Writes write) {

            super.write(write);

            write.i(lockedIndex);
            write.i(customArea);
        }


        @Override
        public void read(Reads read, byte revision) {

            super.read(read, revision);

            lockedIndex = read.i();
            customArea = read.i();

            setPlan(lockedIndex);
        }
    }
}