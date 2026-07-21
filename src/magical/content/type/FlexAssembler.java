package magical.content.type;

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

// 自定义组装厂：模块提高等级，手动选择配方，动态范围，支持方块载荷
public class FlexAssembler extends UnitAssembler {

    // 存储每个配方对应的拾取面积(格)
    public Map<AssemblerUnitPlan, Float> areaPerPlan = new HashMap<>();
    // 存储等级标签 → 配方列表
    public OrderedMap<String, Seq<AssemblerUnitPlan>> tierMap = new OrderedMap<>();
    // 存储每个配方所需的不同载荷类型数（用于判断模块容量是否足够）
    public Map<AssemblerUnitPlan, Integer> requiredTypesPerPlan = new HashMap<>();

    public FlexAssembler(String name) {
        super(name);
        plans.clear(); // 我们手动管理配方列表
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

        // 计算所需的不同载荷类型数（例如：2种单位 = 2，1单位+1方块 = 2）
        int distinct = (int) Arrays.stream(inputs).map(s -> s.item).distinct().count();
        requiredTypesPerPlan.put(plan, distinct);
    }

    @Override
    public void setBars() {
        super.setBars();
        // 进度条原版已有，这里无需操作
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
                Button tabBtn = new Button(Tex.button, Styles.clearToggleTransi); // 兼容样式
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
        public float productionProgress;   // 独立进度（用于 UI）
        public float warmup = 0f;

        /** 清除配置，重置容量和计划 */
        public void setConfigured(boolean val) {
            configured = val;
            if (!val) {
                selectedPlan = null;
                customAreaSize = block().areaSize;