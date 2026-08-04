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
        private int selectedIndex = NO_PLAN;   // 当前配置索引（可能被原版修改）
        private int lockedIndex = NO_PLAN;     // 用户锁定索引（服务端/客户端同步）

        private void syncArea(AssemblerUnitPlan plan) {
            if (plan != null) {
                areaSize = planAreaMap.getOrDefault(plan, areaSize);
            }
        }

        private AssemblerUnitPlan effectivePlan() {
            if (lockedIndex >= 0 && lockedIndex < plans.size) {
                return plans.get(lockedIndex);
            }
            if (selectedIndex >= 0 && selectedIndex < plans.size) {
                return plans.get(selectedIndex);
            }
            for (AssemblerUnitPlan plan : plans) {
                if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                    return plan;
                }
            }
            return plans.isEmpty() ? null : plans.first();
        }

        @Override
        public void created() {
            super.created();
            AssemblerUnitPlan plan = effectivePlan();
            if (plan != null) syncArea(plan);
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

        // 客户端 UI（保留全部原有功能）
        @Override
        public void buildConfiguration(Table table) {
            if (Vars.headless) return;

            final AssemblerUnitPlan current = effectivePlan();

            Seq<AssemblerUnitPlan> available = new Seq<>();
            for (AssemblerUnitPlan plan : plans) {
                if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                    available.add(plan);
                }
            }

            if (available.isEmpty()) {
                table.label(() -> Core.bundle.get("flexassembler.no-plans")).pad(10);
                if (current != null && lockedIndex != NO_PLAN) {
                    table.row();
                    table.label(() -> Core.bundle.format("flexassembler.tier-low", current.unit.localizedName, tierRequired.getOrDefault(current, 0)))
                            .color(Pal.remove).padTop(4).row();
                    table.button(Core.bundle.get("flexassembler.deselect"), () -> unlockAndClear())
                            .size(120f, 40f).padTop(8).row();
                }
                return;
            }

            boolean chosenAvailable = current != null && available.contains(current);

            if (!chosenAvailable && current != null && lockedIndex != NO_PLAN) {
                table.label(() -> Core.bundle.format("flexassembler.tier-low", current.unit.localizedName, tierRequired.getOrDefault(current, 0)))
                        .padBottom(4).color(Pal.remove).row();
            } else if (current != null && lockedIndex != NO_PLAN) {
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
                boolean isChosen = Objects.equals(current, plan) && lockedIndex != NO_PLAN;
                int indexInPlans = plans.indexOf(plan);

                Button btn = new Button(Tex.button);
                btn.table(inner -> {
                    inner.image(plan.unit.uiIcon).size(30f).padBottom(4f);
                    inner.row();
                    inner.add(plan.unit.localizedName).color(isChosen ? Pal.accent : Color.lightGray);
                }).pad(8);

                btn.clicked(() -> lockAndSelect(indexInPlans));
                grid.add(btn).size(80f, 80f).pad(4f);
            }

            ScrollPane pane = new ScrollPane(grid);
            table.add(pane).grow().maxHeight(400f).row();

            if (lockedIndex != NO_PLAN) {
                table.row();
                table.button(Core.bundle.get("flexassembler.deselect"), () -> unlockAndClear())
                        .size(120f, 40f).padTop(8).row();
            }
        }

        // 锁定选择（客户端调用，同时服务端设置 lockedIndex 通过 configure）
        private void lockAndSelect(int index) {
            if (index >= 0 && index < plans.size) {
                lockedIndex = index;
                selectedIndex = index;
                configure(index);   // 服务端收到后也会锁定
                syncArea(plans.get(index));
            }
        }

        private void unlockAndClear() {
            lockedIndex = NO_PLAN;
            selectedIndex = NO_PLAN;
            configure(NO_PLAN);     // 服务端收到后也会清除锁
            AssemblerUnitPlan def = effectivePlan();
            if (def != null) syncArea(def);
        }

        @Override
        public Object config() {
            return lockedIndex != NO_PLAN ? lockedIndex : NO_PLAN;
        }

        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                int val = (Integer) value;
                if (val == NO_PLAN) {
                    // 唯一取消锁的情况：lockedIndex 也是 NO_PLAN 时
                    if (lockedIndex == NO_PLAN) {
                        selectedIndex = NO_PLAN;
                    }
                } else if (val >= 0 && val < plans.size) {
                    // 收到有效索引时，服务端和客户端都锁定
                    lockedIndex = val;
                    selectedIndex = val;
                }
                // 其他值（无效索引）一律忽略，保持原锁
            }
            super.configure(value);
        }

        @Override
        public AssemblerUnitPlan plan() {
            return effectivePlan();
        }

        @Override
        public void updateTile() {
            // 强制恢复锁，以防原版逻辑修改了 selectedIndex
            if (lockedIndex != NO_PLAN) {
                selectedIndex = lockedIndex;
            }
            AssemblerUnitPlan cur = effectivePlan();
            if (cur != null) {
                areaSize = planAreaMap.getOrDefault(cur, areaSize);
            }
            // 执行原版逻辑（可能会试图调用 configure，但我们的 configure 会防御）
            super.updateTile();
            // 再次恢复，确保面积和计划绝对正确
            if (lockedIndex != NO_PLAN) {
                selectedIndex = lockedIndex;
                if (lockedIndex >= 0 && lockedIndex < plans.size) {
                    areaSize = planAreaMap.getOrDefault(plans.get(lockedIndex), areaSize);
                }
            }
        }

        @Override
        public Vec2 getUnitSpawn() {
            float len = tilesize * (areaSize + block.size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(lockedIndex);
            write.i(selectedIndex);
            write.i(areaSize);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            lockedIndex = read.i();
            selectedIndex = read.i();
            areaSize = read.i();
            // 若 lockedIndex 越界，则重置为未锁定
            if (lockedIndex != NO_PLAN && (lockedIndex < 0 || lockedIndex >= plans.size)) {
                lockedIndex = NO_PLAN;
            }
        }
    }
}