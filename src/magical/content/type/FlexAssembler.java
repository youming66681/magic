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

//by youming

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
        private int lockedIndex = NO_PLAN;     // 唯一的选择标识，永不自动变化

        // 获取当前应使用的计划（锁定优先，否则返回废弃计划，但生产被停止）
        private AssemblerUnitPlan activePlan() {
            if (lockedIndex >= 0 && lockedIndex < plans.size) {
                return plans.get(lockedIndex);
            }
            // 未锁定时返回一个安全占位计划（绝不等于null），但shouldConsume会阻止生产
            return plans.isEmpty() ? super.plan() : plans.get(0);
        }

        @Override
        public void created() {
            super.created();
            // 如果存档中有锁定，同步面积
            if (lockedIndex >= 0 && lockedIndex < plans.size) {
                areaSize = planAreaMap.getOrDefault(plans.get(lockedIndex), areaSize);
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

        // 客户端 UI（只显示可用配方，但允许点击任意配方进行锁定）
        @Override
        public void buildConfiguration(Table table) {
            if (Vars.headless) return;

            AssemblerUnitPlan current = activePlan();
            boolean locked = lockedIndex != NO_PLAN;

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

        // 锁定并选择（客户端调用）
        private void lockAndSelect(int index) {
            if (index >= 0 && index < plans.size) {
                lockedIndex = index;
                configure(index);          // 同步到服务端
                syncArea();
            }
        }

        // 取消选择（客户端调用）
        private void unlockAndClear() {
            lockedIndex = NO_PLAN;
            configure(NO_PLAN);            // 发送解锁指令
            syncArea();
        }

        private void syncArea() {
            if (lockedIndex >= 0 && lockedIndex < plans.size) {
                areaSize = planAreaMap.getOrDefault(plans.get(lockedIndex), areaSize);
            }
        }

        @Override
        public Object config() {
            return lockedIndex;   // 返回锁定的索引，-1 表示未锁定
        }

        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                int val = (Integer) value;
                if (val == NO_PLAN) {
                    // 只有我们自己调用 unlockAndClear 才会传入 NO_PLAN，此时清除锁
                    lockedIndex = NO_PLAN;
                } else if (val >= 0 && val < plans.size) {
                    // 锁定该索引
                    lockedIndex = val;
                }
                // 其他任何值都完全忽略，锁不会被改变
            }
            super.configure(value);   // 触发网络同步
        }

        @Override
        public AssemblerUnitPlan plan() {
            return activePlan();
        }

        @Override
        public boolean shouldConsume() {
            // 未选择任何计划时禁止消耗
            if (lockedIndex == NO_PLAN) return false;
            if (lockedIndex >= 0 && lockedIndex < plans.size) {
                int reqTier = tierRequired.getOrDefault(plans.get(lockedIndex), 0);
                if (reqTier > currentTier) return false;   // 模块不足，暂停
            }
            return super.shouldConsume();
        }

        @Override
        public void updateTile() {
            // 确保面积与锁定的计划同步
            syncArea();
            super.updateTile();
            // 原版 updateTile 可能会修改某些东西，但我们再次强制面积同步
            syncArea();
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
            write.i(areaSize);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            lockedIndex = read.i();
            areaSize = read.i();
            // 若锁定的索引失效（计划被删除），则解锁
            if (lockedIndex >= plans.size() || lockedIndex < 0) {
                lockedIndex = NO_PLAN;
            }
        }
    }
}