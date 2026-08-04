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
        private int selectedIndex = NO_PLAN;       // 当前选择的配方索引
        private boolean userAction = false;        // 是否由用户点击触发
        /** 根据 selectedIndex 获取对应计划 */
        private AssemblerUnitPlan getChosenPlan() {
            if (selectedIndex >= 0 && selectedIndex < plans.size) {
                return plans.get(selectedIndex);
            }
            return null;
        }
        /** 同步面积到当前选定计划 */
        private void syncArea() {
            AssemblerUnitPlan plan = getChosenPlan();
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
        // ---------- 客户端 UI ----------
        @Override
        public void buildConfiguration(Table table) {
            if (Core.app.isHeadless()) return;
            AssemblerUnitPlan current = getChosenPlan();
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
                    table.button(Core.bundle.get("flexassembler.deselect"), this::unlock);
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
                btn.clicked(() -> lockOnPlan(indexInPlans));
                grid.add(btn).size(80f, 80f).pad(4f);
            }
            ScrollPane pane = new ScrollPane(grid);
            table.add(pane).grow().maxHeight(400f).row();
            if (locked) {
                table.row();
                table.button(Core.bundle.get("flexassembler.deselect"), this::unlock);
            }
        }
        private void lockOnPlan(int index) {
            if (index >= 0 && index < plans.size) {
                userAction = true;         // 标记用户操作
                configure(index);          // 发送配置
                selectedIndex = index;
                syncArea();
            }
        }
        private void unlock() {
            userAction = true;
            configure(NO_PLAN);
            selectedIndex = NO_PLAN;
            syncArea();
        }
        // ---------- 配置序列化 ----------
        @Override
        public Object config() {
            return selectedIndex;
        }
        /** 核心保护：只有 userAction 为真时才允许修改 selectedIndex，否则一律忽略 */
        @Override
        public void configure(@Nullable Object value) {
            if (!userAction) {
                // 忽略所有外部调用，确保锁不被意外修改
                super.configure(selectedIndex);   // 强制同步回正确值
                return;
            }
            // 处理用户操作
            userAction = false;
            if (value instanceof Integer) {
                int val = (Integer) value;
                if (val == NO_PLAN) {
                    selectedIndex = NO_PLAN;
                } else if (val >= 0 && val < plans.size) {
                    selectedIndex = val;
                }
            }
            super.configure(value);
            syncArea();
        }
        // ---------- 强制计划 ----------
        @Override
        public AssemblerUnitPlan plan() {
            AssemblerUnitPlan chosen = getChosenPlan();
            return chosen != null ? chosen : (plans.isEmpty() ? super.plan() : plans.get(0));
        }
        @Override
        public boolean shouldConsume() {
            AssemblerUnitPlan chosen = getChosenPlan();
            if (chosen == null) return false;
            int reqTier = tierRequired.getOrDefault(chosen, 0);
            if (reqTier > currentTier) return false;
            return super.shouldConsume();
        }
        @Override
        public void updateTile() {
            // 仅负责同步面积，其余完全交给原版
            syncArea();
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
            write.i(selectedIndex);
            write.i(areaSize);
        }
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            selectedIndex = read.i();
            areaSize = read.i();
        }
    }
}