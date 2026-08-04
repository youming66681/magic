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
        private int selectedIndex = NO_PLAN;   // 配置值（可能被原版改动，但会被锁覆盖）
        private int lockedIndex = NO_PLAN;     // 实际上锁定的索引，只有用户主动操作才会改变
        private boolean ignoreNextConfigure = false; // 防止递归调用

        private void syncArea(int index) {
            if (index >= 0 && index < plans.size) {
                areaSize = planAreaMap.getOrDefault(plans.get(index), areaSize);
            }
        }

        /** 获取应当真正使用的计划（永远优先锁定计划） */
        private AssemblerUnitPlan effectivePlan() {
            if (lockedIndex != NO_PLAN && lockedIndex < plans.size) {
                return plans.get(lockedIndex);
            }
            // 未锁定时，使用默认计划（最高 tier 且 unlocked）
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
            // 如果读档后 lockedIndex 有效，直接同步面积
            if (lockedIndex != NO_PLAN && lockedIndex < plans.size) {
                syncArea(lockedIndex);
            } else {
                AssemblerUnitPlan plan = effectivePlan();
                if (plan != null) syncArea(plans.indexOf(plan));
            }
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

        // 客户端 UI（保留等级过滤、高亮、取消选择，但不主动刷新面板）
        @Override
        public void buildConfiguration(Table table) {
            if (Vars.headless) return;

            final AssemblerUnitPlan current = effectivePlan();
            final boolean locked = lockedIndex != NO_PLAN;

            Seq<AssemblerUnitPlan> available = new Seq<>();
            for (AssemblerUnitPlan plan : plans) {
                if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                    available.add(plan);
                }
            }

            if (available.isEmpty()) {
                table.label(() -> Core.bundle.get("flexassembler.no-plans")).pad(10);
                if (locked) {
                    AssemblerUnitPlan lockedPlan = plans.get(lockedIndex);
                    table.row();
                    table.label(() -> Core.bundle.format("flexassembler.tier-low", lockedPlan.unit.localizedName, tierRequired.getOrDefault(lockedPlan, 0)))
                            .color(Pal.remove).padTop(4).row();
                    table.button(Core.bundle.get("flexassembler.deselect"), () -> unlockAndClear())
                            .size(120f, 40f).padTop(8).row();
                }
                return;
            }

            if (locked && !available.contains(current)) {
                table.label(() -> Core.bundle.format("flexassembler.tier-low", current.unit.localizedName, tierRequired.getOrDefault(current, 0)))
                        .padBottom(4).color(Pal.remove).row();
            } else if (locked) {
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

            if (locked) {
                table.row();
                table.button(Core.bundle.get("flexassembler.deselect"), () -> unlockAndClear())
                        .size(120f, 40f).padTop(8).row();
            }
        }

        /** 用户点击图标时调用（客户端） */
        private void lockAndSelect(int index) {
            if (index < 0 || index >= plans.size) return;
            // 先设置 lockedIndex，再调用 configure，这样 configure 中会接受该值
            lockedIndex = index;
            selectedIndex = index;
            ignoreNextConfigure = true; // 确保 configure 中不会因为递归而清掉我们的锁
            configure(index);           // 同步到服务端
            syncArea(index);
        }

        /** 用户点击取消选择时调用 */
        private void unlockAndClear() {
            lockedIndex = NO_PLAN;
            selectedIndex = NO_PLAN;
            ignoreNextConfigure = true;
            configure(NO_PLAN);         // 服务端也会收到 NO_PLAN，然后因为 lockedIndex 已经是 NO_PLAN，会清除一切
            AssemblerUnitPlan def = effectivePlan();
            if (def != null) syncArea(plans.indexOf(def));
        }

        @Override
        public Object config() {
            return selectedIndex;
        }

        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                int val = (Integer) value;
                if (val == NO_PLAN) {
                    // 只有当我们已经解锁（lockedIndex == NO_PLAN）时才允许将 selectedIndex 也设为 NO_PLAN
                    if (lockedIndex == NO_PLAN) {
                        selectedIndex = NO_PLAN;
                    }
                    // 否则，无论原版怎么发送 NO_PLAN，我们都忽略
                } else if (val >= 0 && val < plans.size) {
                    if (lockedIndex == NO_PLAN) {
                        // 第一次锁定（由客户端触发）
                        lockedIndex = val;
                        selectedIndex = val;
                    } else {
                        // 如果已经锁定，则只更新 selectedIndex（例如服务端同步回来），但 lockedIndex 不变
                        selectedIndex = val;
                    }
                }
                // 其他无效索引直接忽略，不改变任何东西
            }
            if (!ignoreNextConfigure) {
                super.configure(value);   // 只有非锁定触发的 configure 才调用父类，防止递归
            } else {
                ignoreNextConfigure = false;
                super.configure(value);   // 依然需要调用父类进行网络同步，但此时锁已更新
            }
        }

        @Override
        public AssemblerUnitPlan plan() {
            return effectivePlan();
        }

        @Override
        public boolean shouldConsume() {
            if (lockedIndex != NO_PLAN && lockedIndex < plans.size) {
                int reqTier = tierRequired.getOrDefault(plans.get(lockedIndex), 0);
                if (reqTier > currentTier) return false;   // 模块不足，暂停生产
            }
            return super.shouldConsume();
        }

        @Override
        public void updateTile() {
            // 每帧强制使用锁定计划（即使原版在 super 中尝试修改 selectedIndex）
            if (lockedIndex != NO_PLAN) {
                selectedIndex = lockedIndex;
                if (lockedIndex >= 0 && lockedIndex < plans.size) {
                    syncArea(lockedIndex);
                }
            }
            super.updateTile();
            // 二次保障（因为 super 可能调用了 configure）
            if (lockedIndex != NO_PLAN) {
                selectedIndex = lockedIndex;
                if (lockedIndex >= 0 && lockedIndex < plans.size) {
                    syncArea(lockedIndex);
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
            // 如果存档中的 lockedIndex 越界，则重置
            if (lockedIndex != NO_PLAN && (lockedIndex < 0 || lockedIndex >= plans.size)) {
                lockedIndex = NO_PLAN;
            }
        }
    }
}