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
        private int selectedIndex = NO_PLAN;      // 当前配置索引（被锁保护）
        private int lockedIndex = NO_PLAN;        // 用户主动锁定的索引，永不自动清除
        private boolean explicitSelection = false;// 是否曾手动选择过（若为false且未锁定，则不生产）
        private void syncArea(int index) {
            if (index >= 0 && index < plans.size) {
                areaSize = planAreaMap.getOrDefault(plans.get(index), areaSize);
            }
        }
        private AssemblerUnitPlan effectivePlan() {
            if (lockedIndex != NO_PLAN && lockedIndex < plans.size) {
                return plans.get(lockedIndex);
            }
            // 未锁定时，不自动选择任何计划，返回 null 表示“无生产”
            return null;
        }
        @Override
        public void created() {
            super.created();
            if (lockedIndex != NO_PLAN && lockedIndex < plans.size) {
                syncArea(lockedIndex);
                explicitSelection = true;
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
        // 客户端 UI
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
            // 无可用配方（可能模块不够）
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
        // 锁定并选择
        private void lockAndSelect(int index) {
            if (index >= 0 && index < plans.size) {
                lockedIndex = index;
                selectedIndex = index;
                explicitSelection = true;
                configure(index);
                syncArea(index);
            }
        }
        // 取消选择
        private void unlockAndClear() {
            lockedIndex = NO_PLAN;
            selectedIndex = NO_PLAN;
            explicitSelection = false;
            configure(NO_PLAN);
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
                    // 只有当我们自己发起的解锁请求（explicitSelection=false）才真正清除
                    if (!explicitSelection) {
                        selectedIndex = NO_PLAN;
                        lockedIndex = NO_PLAN;
                    }
                    // 否则忽略
                } else if (val >= 0 && val < plans.size) {
                    if (lockedIndex == NO_PLAN) {
                        lockedIndex = val;
                        explicitSelection = true;
                    }
                    selectedIndex = val;
                }
            }
            super.configure(value);
        }
        @Override
        public AssemblerUnitPlan plan() {
            if (lockedIndex != NO_PLAN && lockedIndex < plans.size) {
                return plans.get(lockedIndex);
            }
            // 没有锁定时，返回一个安全的非null计划，但绝不自动选择
            if (plans.isEmpty()) return super.plan();
            return plans.get(0);
        }
        @Override
        public boolean shouldConsume() {
            if (!explicitSelection || lockedIndex == NO_PLAN) {
                return false;   // 未手动选择时不生产
            }
            if (lockedIndex >= 0 && lockedIndex < plans.size) {
                int reqTier = tierRequired.getOrDefault(plans.get(lockedIndex), 0);
                if (reqTier > currentTier) return false; // 等级不足
            }
            return super.shouldConsume();
        }
        @Override
        public void updateTile() {
            if (lockedIndex != NO_PLAN) {
                selectedIndex = lockedIndex;
                syncArea(lockedIndex);
            }
            super.updateTile();
            // 再次防止面积被篡改
            if (lockedIndex != NO_PLAN) {
                syncArea(lockedIndex);
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
            write.bool(explicitSelection);
            write.i(lockedIndex);
            write.i(selectedIndex);
            write.i(areaSize);
        }
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            explicitSelection = read.bool();
            lockedIndex = read.i();
            selectedIndex = read.i();
            areaSize = read.i();
            if (lockedIndex != NO_PLAN && (lockedIndex < 0 || lockedIndex >= plans.size)) {
                lockedIndex = NO_PLAN;
            }
        }
    }
}