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
        private static final int NO_SELECTION = -1;
        private int selectedUnitId = NO_SELECTION;   // 用户选择的单位ID，-1 表示未选择
        /** 根据 selectedUnitId 查找对应的计划，找不到则返回第一个计划（但生产被禁用） */
        private AssemblerUnitPlan getEffectivePlan() {
            if (selectedUnitId != NO_SELECTION) {
                for (AssemblerUnitPlan p : plans) {
                    if (p.unit.id == selectedUnitId) {
                        return p;
                    }
                }
            }
            // 未选择或计划丢失时，返回一个安全计划（用于UI显示等），但实际生产会被 shouldConsume 阻止
            return plans.isEmpty() ? super.plan() : plans.get(0);
        }
        /** 同步面积到当前计划 */
        private void syncArea() {
            AssemblerUnitPlan plan = getEffectivePlan();
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
                if (other instanceof UnitAssemblerModuleBuild mod) {
                    modules.add(mod);
                }
            }
            checkTier();
        }
        // 客户端UI（保留原有所有功能）
        @Override
        public void buildConfiguration(Table table) {
            if (Vars.headless) return;
            final AssemblerUnitPlan current = getEffectivePlan();
            final boolean hasSelection = selectedUnitId != NO_SELECTION;
            Seq<AssemblerUnitPlan> available = new Seq<>();
            for (AssemblerUnitPlan plan : plans) {
                if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                    available.add(plan);
                }
            }
            if (available.isEmpty()) {
                table.label(() -> Core.bundle.get("flexassembler.no-plans")).pad(10);
                if (hasSelection && current != null) {
                    table.row();
                    table.label(() -> Core.bundle.format("flexassembler.tier-low", current.unit.localizedName, tierRequired.getOrDefault(current, 0)))
                            .color(Pal.remove).padTop(4).row();
                    table.button(Core.bundle.get("flexassembler.deselect"), this::clearSelection)
                            .size(120f, 40f).padTop(8).row();
                }
                return;
            }
            if (hasSelection && current != null) {
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
                boolean isChosen = hasSelection && current != null && current == plan;
                Button btn = new Button(Tex.button);
                btn.table(inner -> {
                    inner.image(plan.unit.uiIcon).size(30f).padBottom(4f);
                    inner.row();
                    inner.add(plan.unit.localizedName).color(isChosen ? Pal.accent : Color.lightGray);
                }).pad(8);
                btn.clicked(() -> selectPlan(plan));
                grid.add(btn).size(80f, 80f).pad(4f);
            }
            ScrollPane pane = new ScrollPane(grid);
            table.add(pane).grow().maxHeight(400f).row();
            if (hasSelection) {
                table.row();
                table.button(Core.bundle.get("flexassembler.deselect"), this::clearSelection)
                        .size(120f, 40f).padTop(8).row();
            }
        }
        /** 选择指定计划 */
        private void selectPlan(AssemblerUnitPlan plan) {
            selectedUnitId = plan.unit.id;
            configure(selectedUnitId);
            syncArea();
        }
        /** 取消选择 */
        private void clearSelection() {
            selectedUnitId = NO_SELECTION;
            configure(NO_SELECTION);
            syncArea();
        }
        @Override
        public Object config() {
            return selectedUnitId;
        }
        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                int id = (Integer) value;
                if (id == NO_SELECTION) {
                    selectedUnitId = NO_SELECTION;
                } else {
                    // 验证该 ID 是否存在于 plans 中，若存在则接受
                    boolean found = false;
                    for (AssemblerUnitPlan p : plans) {
                        if (p.unit.id == id) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        selectedUnitId = id;
                    }
                    // 如果 ID 无效，忽略此次配置，保持原选择
                }
            }
            super.configure(value);   // 触发网络同步
            syncArea();
        }
        @Override
        public AssemblerUnitPlan plan() {
            return getEffectivePlan();
        }
        @Override
        public boolean shouldConsume() {
            // 未选择时不生产
            if (selectedUnitId == NO_SELECTION) return false;
            // 检查模块等级是否足够
            AssemblerUnitPlan current = getEffectivePlan();
            if (current != null && selectedUnitId != NO_SELECTION) {
                int reqTier = tierRequired.getOrDefault(current, 0);
                if (reqTier > currentTier) return false;
            }
            return super.shouldConsume();
        }
        @Override
        public void updateTile() {
            syncArea();
            super.updateTile();
            syncArea();   // 防止原版修改面积
        }
        @Override
        public Vec2 getUnitSpawn() {
            float len = tilesize * (areaSize + block.size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }
        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(selectedUnitId);
            write.i(areaSize);
        }
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            selectedUnitId = read.i();
            areaSize = read.i();
        }
    }
}