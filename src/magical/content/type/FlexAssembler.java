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
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.PayloadStack;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.ai.types.AssemblerAI;
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
        private AssemblerUnitPlan lockedPlan;
        public int myAreaSize = FlexAssembler.this.areaSize;
        private void updateLockedPlan() {
            if (lockedIndex >= 0 && lockedIndex < plans.size) {
                lockedPlan = plans.get(lockedIndex);
            } else {
                lockedPlan = null;
            }
        }
        private void syncArea() {
            AssemblerUnitPlan effective = plan();
            if (effective != null) {
                myAreaSize = planAreaMap.getOrDefault(effective, FlexAssembler.this.areaSize);
            }
        }
        @Override
        public void created() {
            super.created();
            updateLockedPlan();
            syncArea();
        }
        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            syncArea();
        }
        @Override
        public boolean moduleFits(Block other, float ox, float oy, int rotation) {
            float dx = ox + Geometry.d4x(rotation) * (other.size / 2f + 0.5f) * tilesize;
            float dy = oy + Geometry.d4y(rotation) * (other.size / 2f + 0.5f) * tilesize;
            Vec2 spawn = getUnitSpawn();
            if (Tile.relativeTo(ox, oy, spawn.x, spawn.y) != rotation) return false;
            float dst = Math.max(Math.abs(dx - spawn.x), Math.abs(dy - spawn.y));
            return Mathf.equal(dst, tilesize * myAreaSize / 2f - tilesize / 2f);
        }
        @Override
        public Vec2 getUnitSpawn() {
            float len = tilesize * (myAreaSize + block.size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }
        @Override
        public void buildConfiguration(Table table) {
            if (Vars.headless) return;
            updateLockedPlan();
            AssemblerUnitPlan current = lockedPlan;
            boolean locked = current != null;
            Seq<AssemblerUnitPlan> available = new Seq<>();
            for (AssemblerUnitPlan plan : plans) {
                if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                    available.add(plan);
                }
            }
            if (available.isEmpty()) {
                table.label(() -> Core.bundle.get("flexassembler.no-plans")).pad(10);
                if (locked) {
                    table.row();
                    table.button(Core.bundle.get("flexassembler.deselect"), () -> configure(NO_PLAN));
                }
                return;
            }
            if (locked) {
                table.label(() -> Core.bundle.format("flexassembler.producing", current.unit.localizedName))
                        .padBottom(4).row();
            } else {
                table.label(() -> Core.bundle.get("flexassembler.select-unit"))
                        .padBottom(4).color(Color.gray).row();
            }
            Table grid = new Table();
            int cols = 4;
            for (int i = 0; i < available.size; i++) {
                if (i % cols == 0 && i != 0) grid.row();
                AssemblerUnitPlan plan = available.get(i);
                boolean isChosen = locked && current == plan;
                int index = plans.indexOf(plan);
                Button btn = new Button(Tex.button);
                btn.table(inner -> {
                    inner.image(plan.unit.uiIcon).size(30f).padBottom(4f);
                    inner.row();
                    inner.add(plan.unit.localizedName).color(isChosen ? Pal.accent : Color.lightGray);
                }).pad(8);
                btn.clicked(() -> {
                    lockedIndex = index;
                    updateLockedPlan();
                    configure(index);
                    syncArea();
                });
                grid.add(btn).size(80f, 80f).pad(4f);
            }
            ScrollPane pane = new ScrollPane(grid);
            table.add(pane).grow().maxHeight(400f).row();
        }
        @Override
        public Object config() {
            return lockedIndex;
        }
        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                int idx = (Integer) value;
                if (idx == NO_PLAN || (idx >= 0 && idx < plans.size)) {
                    lockedIndex = idx;
                    updateLockedPlan();
                    syncArea();
                }
            }
            super.configure(value);
        }
        @Override
        public AssemblerUnitPlan plan() {
            if (lockedPlan != null) {
                int reqTier = tierRequired.getOrDefault(lockedPlan, 0);
                if (reqTier <= currentTier) return lockedPlan;
            }
            return super.plan();
        }
        @Override
        public boolean shouldConsume() {
            if (lockedPlan != null && tierRequired.getOrDefault(lockedPlan, 0) > currentTier) {
                return false;
            }
            return super.shouldConsume();
        }
        @Override
        public void updateTile() {
            syncArea();
            int prevArea = areaSize;
            areaSize = myAreaSize;
            super.updateTile();
            areaSize = prevArea;
        }
        @Override
        public void draw() {
            int prevArea = areaSize;
            areaSize = myAreaSize;
            super.draw();
            areaSize = prevArea;
        }
        @Override
        public void drawSelect() {
            int prevArea = areaSize;
            areaSize = myAreaSize;
            super.drawSelect();
            areaSize = prevArea;
        }
        public void spawned() {
            AssemblerUnitPlan plan = lockedPlan != null ? lockedPlan : super.plan();
            if (plan == null) return;
            Vec2 spawn = getUnitSpawn();
            consume();
            Unit unit = plan.unit.create(team);
            if (unit.isCommandable() && commandPos != null) unit.command().commandPosition(commandPos);
            unit.set(spawn.x + Mathf.range(0.001f), spawn.y + Mathf.range(0.001f));
            unit.rotation = rotdeg();
            if (!net.client()) unit.add();
            createSound.at(spawn.x, spawn.y, 1f + Mathf.range(0.06f), createSoundVolume);
            progress = 0f;
            Fx.unitAssemble.at(spawn.x, spawn.y, rotdeg() - 90f, plan.unit);
            blocks.clear();
        }
        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(lockedIndex);
            write.i(myAreaSize);
        }
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            lockedIndex = read.i();
            myAreaSize = read.i();
            updateLockedPlan();
            syncArea();
        }
    }
}