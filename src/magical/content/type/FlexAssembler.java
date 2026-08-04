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
    public void setStats() { /* ... 保持不变，此处省略以节省篇幅，请从原文件复制 */ }

    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        private static final int NO_PLAN = -1;
        private int selectedIndex = NO_PLAN;   // 用于 UI 显示和序列化
        private int lockedIndex = NO_PLAN;     // 用户主动锁定的索引，永不自动清除

        private void syncArea(AssemblerUnitPlan plan) {
            if (plan != null) {
                areaSize = planAreaMap.getOrDefault(plan, areaSize);
            }
        }

        // 获取锁定计划，若未锁定则返回默认（但不改变 lockedIndex）
        private AssemblerUnitPlan effectivePlan() {
            if (lockedIndex >= 0 && lockedIndex < plans.size) {
                return plans.get(lockedIndex);
            }
            if (selectedIndex >= 0 && selectedIndex < plans.size) {
                return plans.get(selectedIndex);
            }
            // 默认逻辑：当前tier下最高级的计划
            for (int i = plans.size - 1; i >= 0; i--) {
                if (tierRequired.getOrDefault(plans.get(i), 0) <= currentTier) {
                    return plans.get(i);
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

        // 客户端 UI（保留等级过滤、高亮、取消选择）
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
                if (lockedIndex != NO_PLAN) {
                    AssemblerUnitPlan locked = plans.get(lockedIndex);
                    table.row();
                    table.label(() -> Core.bundle.format("flexassembler.tier-low", locked.unit.localizedName, tierRequired.getOrDefault(locked, 0)))
                            .color(Pal.remove).padTop(4).row();
                    table.button(Core.bundle.get("flexassembler.deselect"), () -> unlockAndClear())
                            .size(120f, 40f).padTop(8).row();
                }
                return;
            }

            boolean chosenAvailable = current != null && lockedIndex != NO_PLAN && available.contains(current);

            if (!chosenAvailable && lockedIndex != NO_PLAN) {
                table.label(() -> Core.bundle.format("flexassembler.tier-low", current.unit.localizedName, tierRequired.getOrDefault(current, 0)))
                        .padBottom(4).color(Pal.remove).row();
            } else if (lockedIndex != NO_PLAN) {
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

        // 锁定选择
        private void lockAndSelect(int index) {
            if (index >= 0 && index < plans.size) {
                lockedIndex = index;
                selectedIndex = index;
                configure(index);
                syncArea(plans.get(index));
            }
        }

        private void unlockAndClear() {
            lockedIndex = NO_PLAN;
            selectedIndex = NO_PLAN;
            configure(NO_PLAN);
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
                    // 仅当 lockedIndex 也是 NO_PLAN 时才清除 selectedIndex
                    if (lockedIndex == NO_PLAN) {
                        selectedIndex = NO_PLAN;
                    }
                } else if (val >= 0 && val < plans.size) {
                    // 收到有效索引，更新 selectedIndex，但不覆盖 lockedIndex（除非 lockedIndex 为 NO_PLAN）
                    selectedIndex = val;
                    if (lockedIndex == NO_PLAN) {
                        lockedIndex = val;   // 自动锁定（例如服务器同步）
                    }
                }
            }
            super.configure(value);
        }

        @Override
        public AssemblerUnitPlan plan() {
            return effectivePlan();   // 永远返回锁定计划或默认计划，不会修改索引
        }

        @Override
        public boolean shouldConsume() {
            // 如果锁定了计划，但模块等级不足，则暂停生产
            if (lockedIndex >= 0 && lockedIndex < plans.size) {
                int reqTier = tierRequired.getOrDefault(plans.get(lockedIndex), 0);
                if (reqTier > currentTier) {
                    return false;   // 暂停
                }
            }
            return super.shouldConsume();
        }

        @Override
        public void updateTile() {
            // 每帧强制恢复 selectedIndex 和面积
            if (lockedIndex != NO_PLAN) {
                selectedIndex = lockedIndex;
                if (lockedIndex >= 0 && lockedIndex < plans.size) {
                    syncArea(plans.get(lockedIndex));
                }
            }
            super.updateTile();
            // 执行后再次确保面积正确
            AssemblerUnitPlan cur = effectivePlan();
            if (cur != null) syncArea(cur);
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
            if (lockedIndex != NO_PLAN && (lockedIndex < 0 || lockedIndex >= plans.size)) {
                lockedIndex = NO_PLAN;   // 计划可能被删除，解锁
            }
        }
    }
}