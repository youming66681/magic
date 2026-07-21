package magical.content;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.ui.Button;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.units.*;
import mindustry.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

/**
 * 自定义组装厂：
 * - 通过相邻 UnitAssemblerModule 提高 capacitie 长度来解锁更高等级配方
 * - 分等级标签页手动选择合成单位
 * - 每个配方拥有独立的拾取范围（areaSize）
 * - 支持单位与方块载荷
 */
public class FlexAssembler extends UnitAssembler {

    // 存储每个配方对应的拾取面积(格)
    public Map<AssemblerUnitPlan, Float> areaPerPlan = new HashMap<>();
    // 存储等级标签 → 配方列表
    public OrderedMap<String, Seq<AssemblerUnitPlan>> tierMap = new OrderedMap<>();
    // 存储每个配方所需的不同载荷类型数（用于判断模块容量是否足够）
    public Map<AssemblerUnitPlan, Integer> requiredTypesPerPlan = new HashMap<>();

    public FlexAssembler(String name) {
        super(name);
        plans.clear(); // 手动管理配方列表
    }

    /** 向指定等级添加一个配方（无物品消耗） */
    public void addPlan(String tier, UnitType output, float time, float areaSize, PayloadStack... inputs) {
        addPlan(tier, output, time, areaSize, null, inputs);
    }

    /** 向指定等级添加一个配方（可带物品消耗） */
    public void addPlan(String tier, UnitType output, float time, float areaSize, @Nullable ItemStack payItem, PayloadStack... inputs) {
        AssemblerUnitPlan plan = new AssemblerUnitPlan(output, time, inputs);
        if (payItem != null) plan.payItem = payItem;
        plans.add(plan);
        areaPerPlan.put(plan, areaSize);

        Seq<AssemblerUnitPlan> group = tierMap.get(tier);
        if (group == null) {
            group = new Seq<>();
            tierMap.put(tier, group);
        }
        group.add(plan);

        // 计算所需的不同载荷类型数
        int distinct = (int) Arrays.stream(inputs).map(s -> s.item).distinct().count();
        requiredTypesPerPlan.put(plan, distinct);
    }

    @Override
    public void setBars() {
        super.setBars();
    }

    // ============ 配置界面 ============
    @Override
    public void buildConfiguration(Table table, Building build) {
        FlexAssemblerBuild entity = (FlexAssemblerBuild) build;
        int currentCapacity = entity.capacities != null ? entity.capacities.length : 0;

        if (!entity.configured) {
            // 显示等级标签页 + 图标网格
            Table tabs = new Table();
            Table content = new Table();

            for (String tier : tierMap.keys()) {
                Button tabBtn = new Button(Tex.button, Styles.clearToggleTransi);
                tabBtn.label(tier).growX();
                final String tierName = tier;
                tabBtn.clicked(() -> {
                    content.clear();
                    buildIconGrid(content, tierMap.get(tierName), entity, currentCapacity);
                });
                tabs.add(tabBtn).growX().pad(4);

                // 若该等级下所有配方均因容量不足而不可用，标签半透明
                boolean allDisabled = tierMap.get(tier).allMatch(p -> requiredTypesPerPlan.getOrDefault(p, 0) > currentCapacity);
                if (allDisabled) {
                    tabBtn.setDisabled(true);
                }
            }
            tabs.row();
            ScrollPane pane = new ScrollPane(content);
            tabs.add(pane).colspan(tierMap.size).grow();

            // 默认显示第一个等级
            if (tierMap.size > 0) {
                String first = tierMap.keys().first();
                buildIconGrid(content, tierMap.get(first), entity, currentCapacity);
            }

            table.add(tabs).grow();
        } else {
            // 已配置：显示当前生产单位，提供更改按钮
            table.label("Producing: " + entity.selectedPlan.output().localizedName).padBottom(4).row();
            table.button("Change", () -> {
                entity.setConfigured(false);
                ui.build.hide();
            }).size(100f, 40f).row();
        }
    }

    private void buildIconGrid(Table grid, Seq<AssemblerUnitPlan> group, FlexAssemblerBuild entity, int currentCapacity) {
        grid.clear();
        int cols = 4;
        int i = 0;
        for (AssemblerUnitPlan plan : group) {
            if (i % cols == 0 && i != 0) grid.row();

            boolean canProduce = requiredTypesPerPlan.getOrDefault(plan, 0) <= currentCapacity;

            ImageButton button = new ImageButton(plan.output().uiIcon, Styles.clearToggleTransi);
            button.getStyle().imageUpColor = canProduce ? plan.output().color : Color.gray;
            button.resizeImage(40f);
            if (canProduce) {
                button.clicked(() -> entity.configure(plan));
            } else {
                button.setDisabled(true);
            }
            button.row();
            button.add(plan.output().localizedName).color(canProduce ? Color.lightGray : Color.darkGray);
            grid.add(button).pad(4);
            i++;
        }
    }

    // ==================== 建筑实体 ====================
    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        public boolean configured = false;
        public AssemblerUnitPlan selectedPlan;
        public float customAreaSize;
        public float productionProgress;
        public float warmup = 0f;

        /** 清除配置 */
        public void setConfigured(boolean val) {
            configured = val;
            if (!val) {
                selectedPlan = null;
                customAreaSize = areaSize; // 恢复到默认区域大小
                capacities = block().capacities.clone();
                productionProgress = 0f;
                warmup = 0f;
            }
        }

        /** 选定一个配方 */
        public void configure(AssemblerUnitPlan plan) {
            selectedPlan = plan;
            configured = true;
            Float area = areaPerPlan.get(plan);
            customAreaSize = area != null ? area : areaSize;
            capacities = block().capacities.clone();
            productionProgress = 0f;
            warmup = 0f;
            configure(new Integer(selectedPlan.output().id));
        }

        @Override
        public Object config() {
            if (configured && selectedPlan != null) {
                return selectedPlan.output().id;
            }
            return null;
        }

        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                int id = (Integer) value;
                UnitType type = content.getByID(ContentType.unit, id);
                if (type != null) {
                    for (AssemblerUnitPlan p : plans) {
                        if (p.output() == type) {
                            configure(p);
                            return;
                        }
                    }
                }
            }
            super.configure(value);
        }

        // ========== 动态范围 ==========
        @Override
        public float areaSize() {
            return configured ? customAreaSize : super.areaSize();
        }

        // ========== 自定义生产逻辑 ==========
        @Override
        public void updateTile() {
            super.updateTile();

            if (!configured || selectedPlan == null) {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.02f);
                productionProgress = 0f;
                return;
            }

            // 电力检查
            if (!consPower()) {  // 原版方法：检查电力满足
                warmup = Mathf.lerpDelta(warmup, 0f, 0.02f);
                return;
            }

            // 满足生产条件？
            if (!meetsPlan(selectedPlan)) {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.02f);
                return;
            }

            warmup = Mathf.lerpDelta(warmup, 1f, 0.02f);
            productionProgress += delta() / (selectedPlan.time * 60f);

            if (productionProgress >= 1f) {
                consumePlan(selectedPlan);
                Unit unit = selectedPlan.output().create(team);
                unit.set(x, y);
                unit.add();
                Fx.producesmoke.at(x, y);
                productionProgress = 0f;
            }
        }

        /** 检查当前存储中是否满足计划的载荷和物品需求 */
        public boolean meetsPlan(AssemblerUnitPlan plan) {
            // 物品检查
            if (plan.payItem != null && (items == null || items.get(plan.payItem.item) < plan.payItem.amount)) {
                return false;
            }
            // 载荷检查
            if (payloads != null && plan.requirements != null) {
                int[] required = new int[content.units().size + content.blocks().size];
                for (PayloadStack stack : plan.requirements) {
                    int idx = stack.item.getPayloadIndex();
                    if (idx >= 0 && idx < required.length) {
                        required[idx] += stack.amount;
                    }
                }
                for (int i = 0; i < required.length; i++) {
                    if (required[i] > 0) {
                        int present = 0;
                        for (int j = 0; j < payloads.length; j++) {
                            if (payloads[j] != null && payloads[j].getPayloadIndex() == i) {
                                present++;
                            }
                        }
                        if (present < required[i]) return false;
                    }
                }
            }
            return true;
        }

        /** 消耗计划所需的载荷和物品 */
        public void consumePlan(AssemblerUnitPlan plan) {
            if (plan.payItem != null && items != null) {
                items.remove(plan.payItem);
            }
            int[] required = new int[content.units().size + content.blocks().size];
            for (PayloadStack stack : plan.requirements) {
                int idx = stack.item.getPayloadIndex();
                if (idx >= 0 && idx < required.length) {
                    required[idx] += stack.amount;
                }
            }
            for (int i = 0; i < required.length; i++) {
                if (required[i] > 0) {
                    int toRemove = required[i];
                    for (int j = 0; j < payloads.length && toRemove > 0; j++) {
                        if (payloads[j] != null && payloads[j].getPayloadIndex() == i) {
                            payloads[j] = null;
                            toRemove--;
                        }
                    }
                }
            }
            Fx.absorb.at(x, y);
        }

        @Override
        public float progress() {
            return productionProgress;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public void drawSelect() {
            if (configured) {
                Drawf.dashCircle(x, y, customAreaSize * 8f, team.color);
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(configured);
            write.f(customAreaSize);
            write.f(productionProgress);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            configured = read.bool();
            customAreaSize = read.f();
            productionProgress = read.f();
        }
    }   // 闭合 FlexAssemblerBuild
}   // 闭合 FlexAssembler