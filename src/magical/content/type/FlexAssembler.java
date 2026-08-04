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
        private int lockedIndex = NO_PLAN;
        private int customArea = 0;
        private AssemblerUnitPlan curPlan;

        private AssemblerUnitPlan getLockedPlan() {
            if (lockedIndex >= 0 && lockedIndex < plans.size) {
                return plans.get(lockedIndex);
            }
            return null;
        }

        private void setPlan(int index) {
            if (index >= 0 && index < plans.size) {
                lockedIndex = index;
                curPlan = plans.get(index);
            } else {
                lockedIndex = NO_PLAN;
                curPlan = null;
            }
            syncArea();
        }

        private void syncArea() {
            AssemblerUnitPlan plan = getLockedPlan();
            if (plan != null) {
                customArea = planAreaMap.getOrDefault(plan, areaSize);
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
            AssemblerUnitPlan current = getLockedPlan();
            if (current != null) {
                table.label(() -> Core.bundle.format("flexassembler.producing", current.unit.localizedName)).row();
            } else {
                table.label(() -> Core.bundle.get("flexassembler.select-unit")).row();
            }
            Table grid = new Table();
            int cols = 4;
            int count = 0;
            for (AssemblerUnitPlan plan : plans) {
                int tier = tierRequired.getOrDefault(plan, 0);
                if (tier > currentTier) continue;
                int index = plans.indexOf(plan);
                boolean selected = current == plan;
                if (count % cols == 0) {
                    grid.row();
                }
                Button button = new Button(Tex.button);
                button.table(t -> {
                    t.image(plan.unit.uiIcon).size(36).padBottom(4);
                    t.row();
                    t.add(plan.unit.localizedName).color(selected ? Pal.accent : Color.white);
                });
                button.clicked(() -> {
                    configure(index);
                });
                grid.add(button).size(90, 90).pad(4);
                count++;
            }
            table.add(new ScrollPane(grid)).grow().maxHeight(400).row();
            if (current != null) {
                table.button(Core.bundle.get("flexassembler.deselect"), this::unlock);
            }
        }

        private void unlock() {
            configure(NO_PLAN);
        }

        @Override
        public Object config() {
            return Integer.valueOf(lockedIndex);
        }

        @Override
        public void configure(@Nullable Object value) {
            if (!(value instanceof Integer)) return;
            setPlan((Integer) value);
        }

        @Override
        public AssemblerUnitPlan plan() {
            if (curPlan != null) {
                return curPlan;
            }
            return getLockedPlan();
        }

        @Override
        public boolean shouldConsume() {
            AssemblerUnitPlan plan = getLockedPlan();
            if (plan == null) return false;
            int tier = tierRequired.getOrDefault(plan, 0);
            if (tier > currentTier) return false;
            return super.shouldConsume();
        }

        @Override
        public void updateTile() {
            syncArea();
            super.updateTile();
        }

        @Override
        public Vec2 getUnitSpawn() {
            float len = tilesize * (customArea + block.size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(1);
            write.i(lockedIndex);
            write.i(customArea);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int ver = read.i();
            if (ver >= 1) {
                lockedIndex = read.i();
                customArea = read.i();
            } else {
                lockedIndex = NO_PLAN;
                customArea = areaSize;
            }
            setPlan(lockedIndex);
        }
    }
}