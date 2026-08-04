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
        private static final int NO_SELECTION = -1;
        private int lockedUnitId = NO_SELECTION;   // 锁定的单位ID
        /** 根据 ID 找到对应的计划 */
        private AssemblerUnitPlan findPlanById(int id) {
            for (AssemblerUnitPlan p : plans) {
                if (p.unit.id == id) return p;
            }
            return null;
        }
        /** 获取当前锁定的计划（可能为空） */
        private AssemblerUnitPlan getLockedPlan() {
            return findPlanById(lockedUnitId);
        }
        /** 同步面积到锁定计划 */
        private void syncArea() {
            AssemblerUnitPlan plan = getLockedPlan();
            if (plan != null) {
                areaSize = planAreaMap.getOrDefault(plan, areaSize);
            }
        }
        @Override
        public void created() {
            super.created();
            syncArea();
        }
        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            modules.clear();
            for (Building other : proximity) {
                if (other instanceof UnitAssemblerModuleBuild mod) modules.add(mod);
            }
            checkTier();
        }
        @Override
        public void buildConfiguration(Table table) {
            if (Core.app.isHeadless()) return;
            AssemblerUnitPlan current = getLockedPlan();
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
                    table.label(() -> Core.bundle.format("flexassembler.tier-low", current.unit.localizedName, tierRequired.getOrDefault(current, 0)))
                            .color(Pal.remove).padTop(4).row();
                    table.button(Core.bundle.get("flexassembler.deselect"), () -> unlock());
                }
                return;
            }
            if (locked) {
                table.label(() -> Core.bundle.format("flexassembler.producing", current.unit.localizedName))
                        .padBottom(4).row();
            } else {
                table.label(() -> Core.bundle.get("flexassembler.select-unit")).padBottom(4).color(Color.gray).row();
            }
            Table grid = new Table();
            int cols = 4;
            for (int i = 0; i < available.size; i++) {
                if (i % cols == 0 && i != 0) grid.row();
                AssemblerUnitPlan plan = available.get(i);
                boolean isChosen = locked && current == plan;
                Button btn = new Button(Tex.button);
                btn.table(inner -> {
                    inner.image(plan.unit.uiIcon).size(30f).padBottom(4f);
                    inner.row();
                    inner.add(plan.unit.localizedName).color(isChosen ? Pal.accent : Color.lightGray);
                }).pad(8);
                btn.clicked(() -> lockOnPlan(plan));
                grid.add(btn).size(80f, 80f).pad(4f);
            }
            ScrollPane pane = new ScrollPane(grid);
            table.add(pane).grow().maxHeight(400f).row();
            if (locked) {
                table.row();
                table.button(Core.bundle.get("flexassembler.deselect"), () -> unlock());
            }
        }
        /** 用户点击图标时锁定 */
        private void lockOnPlan(AssemblerUnitPlan plan) {
            lockedUnitId = plan.unit.id;
            configure(lockedUnitId);
            syncArea();
        }
        /** 取消锁定 */
        private void unlock() {
            lockedUnitId = NO_SELECTION;
            configure(NO_SELECTION);
            syncArea();
        }
        @Override
        public Object config() {
            return lockedUnitId;
        }
        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                int id = (Integer) value;
                if (id == NO_SELECTION) {
                    // 取消锁定：允许
                    lockedUnitId = NO_SELECTION;
                } else if (lockedUnitId == NO_SELECTION) {
                    // 未锁定时，接受新的锁定
                    if (findPlanById(id) != null) {
                        lockedUnitId = id;
                    }
                } else if (id == lockedUnitId) {
                    // 外部发来相同的ID，正常接受
                } else {
                    // ❗ 已锁定，但收到不同的ID → 拒绝，并立刻发回正确的锁定值
                    super.configure(lockedUnitId);
                    return;
                }
            }
            super.configure(value);
            syncArea();
        }
        @Override
        public AssemblerUnitPlan plan() {
            AssemblerUnitPlan locked = getLockedPlan();
            return locked != null ? locked : (plans.isEmpty() ? super.plan() : plans.get(0));
        }
        @Override
        public boolean shouldConsume() {
            AssemblerUnitPlan locked = getLockedPlan();
            if (locked == null) return false;
            int reqTier = tierRequired.getOrDefault(locked, 0);
            if (reqTier > currentTier) return false;
            return super.shouldConsume();
        }
        @Override
        public void updateTile() {
            // 1. 如果被锁定，强制确认 plan 确为锁定单位
            if (lockedUnitId != NO_SELECTION) {
                AssemblerUnitPlan currentPlan = super.plan();
                if (currentPlan == null || currentPlan.unit.id != lockedUnitId) {
                    // 计划被篡改！立刻重新锁定
                    configure(lockedUnitId);
                }
            }
            // 2. 同步面积
            syncArea();
            // 3. 调用原版逻辑
            super.updateTile();
        }
        @Override
        public Vec2 getUnitSpawn() {
            float len = tilesize * (areaSize + block.size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }
        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(lockedUnitId);
            write.i(areaSize);
        }
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            lockedUnitId = read.i();
            areaSize = read.i();
        }
    }
}