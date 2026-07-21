package magical.content;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.ui.Button;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
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
 * 自定义组装厂（兼容 Mindustry v7）：
 * - 通过模块数量扩展 capacities 以解锁高级配方
 * - 等级标签页手动选择合成单位
 * - 每个配方独立的拾取范围（areaSize）
 * - 支持单位与方块载荷
 */
public class FlexAssembler extends UnitAssembler {

    // 存储每个配方对应的自定义面积（格）
    public Map<AssemblerUnitPlan, Float> areaPerPlan = new HashMap<>();
    // 等级标签 → 配方列表
    public OrderedMap<String, Seq<AssemblerUnitPlan>> tierMap = new OrderedMap<>();
    // 配方所需的载荷类型数（用于容量检查）
    public Map<AssemblerUnitPlan, Integer> requiredTypesPerPlan = new HashMap<>();

    public FlexAssembler(String name) {
        super(name);
        plans.clear(); // 手动控制配方
    }

    /** 添加配方（无物品消耗） */
    public void addPlan(String tier, UnitType output, float time, float areaSize, PayloadStack... inputs) {
        addPlan(tier, output, time, areaSize, null, inputs);
    }

    /** 添加配方（可带物品消耗） */
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

        // 计算需要多少种不同的载荷
        int distinct = (int) Arrays.stream(inputs).map(s -> s.item).distinct().count();
        requiredTypesPerPlan.put(plan, distinct);
    }

    // ============ 建筑配置界面 ============
    @Override
    public void buildConfiguration(Table table, Building build) {
        FlexAssemblerBuild entity = (FlexAssemblerBuild) build;
        // capacities 长度代表模块提供的槽位数
        int currentCapacity = entity.capacities != null ? entity.capacities.length : 0;

        if (!entity.configured) {
            Table tabs = new Table();
            Table content = new Table();

            for (String tier : tierMap.keys()) {
                Button tabBtn = new Button(Tex.button, Styles.cleari);
                tabBtn.label(() -> tier).growX();
                final String tierName = tier;
                tabBtn.clicked(() -> {
                    content.clear();
                    buildIconGrid(content, tierMap.get(tierName), entity, currentCapacity);
                });
                tabs.add(tabBtn).growX().pad(4);

                // 若该等级所有配方都不可用，标签置灰
                boolean allDisabled = tierMap.get(tier).allMatch(p -> requiredTypesPerPlan.getOrDefault(p, 0) > currentCapacity);
                if (allDisabled) {
                    tabBtn.setDisabled(true);
                }
            }
            tabs.row();
            ScrollPane pane = new ScrollPane(content);
            tabs.add(pane).colspan(tierMap.size).grow();

            if (tierMap.size > 0) {
                String first = tierMap.keys().next(); // 获取第一个 key
                buildIconGrid(content, tierMap.get(first), entity, currentCapacity);
            }

            table.add(tabs).grow();
        } else {
            table.label("Producing: " + entity.selectedPlan.unit.localizedName).padBottom(4).row();
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

            ImageButton button = new ImageButton(plan.unit.uiIcon, Styles.cleari);
            button.getStyle().imageUpColor = canProduce ? plan.unit.color : Color.gray;
            button.resizeImage(40f);
            if (canProduce) {
                button.clicked(() -> entity.configure(plan));
            } else {
                button.setDisabled(true);
            }
            button.row();
            button.add(plan.unit.localizedName).color(canProduce ? Color.lightGray : Color.darkGray);
            grid.add(button).pad(4);
            i++;
        }
    }

    // ==================== 建筑实体 ====================
    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        public boolean configured = false;
        public AssemblerUnitPlan selectedPlan;
        public float customAreaSize = areaSize; // 使用 block 的默认值
        public float productionProgress;
        public float warmup = 0f;

        public void setConfigured(boolean val) {
            configured = val;
            if (!val) {
                selectedPlan = null;
                customAreaSize = areaSize;
                capacities = FlexAssembler.this.capacities.clone(); // 恢复默认容量
                productionProgress = 0f;
                warmup = 0f;
            }
        }

        public void configure(AssemblerUnitPlan plan) {
            selectedPlan = plan;
            configured = true;
            Float area = areaPerPlan.get(plan);
            customAreaSize = area != null ? area : areaSize;
            capacities = FlexAssembler.this.capacities.clone();
            productionProgress = 0f;
            warmup = 0f;
            configure(new Integer(selectedPlan.unit.id)); // 存储单位 ID
        }

        @Override
        public Object config() {
            return configured && selectedPlan != null ? selectedPlan.unit.id : null;
        }

        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                int id = (Integer) value;
                UnitType type = content.getByID(ContentType.unit, id);
                if (type != null) {
                    for (AssemblerUnitPlan p : plans) {
                        if (p.unit == type) {
                            configure(p);
                            return;
                        }
                    }
                }
            }
            super.configure(value);
        }

        // ---------- 动态范围（直接覆写 areaSize 字段的读取行为，通过重写 drawSelect 实现） ----------
        // 注意：v7 的 UnitAssemblerBuild 没有 areaSize() 方法，只有 areaSize 字段
        // 需要确保无人机拾取范围使用的是我们自定义的 customAreaSize。
        // 原版会使用 this.areaSize 字段，因此我们只需在合适时机设置 this.areaSize = customAreaSize
        @Override
        public void updateTile() {
            // 设置当前 areaSize 为配方对应的值
            if (configured) {
                areaSize = customAreaSize;
            } else {
                areaSize = FlexAssembler.this.areaSize;
            }

            // 执行父类逻辑（包含无人机拾取、生产等）
            super.updateTile();
        }

        // 绘制自定义范围
        @Override
        public void drawSelect() {
            if (configured) {
                Drawf.dashCircle(x, y, customAreaSize * 8f, team.color);
            }
        }

        // 自定义生产进度条（覆盖父类的 progress 和 warmup 方法？）
        // 原版已有 progress() 和 warmup() 方法，我们直接使用父类的字段，但需要返回正确的值
        // 在 updateTile 中我们没有调用父类的生产逻辑，而是自己实现
        // 为简化，我们仍调用父类 updateTile，但通过重写 findPlan() 限制配方
        // 但我们之前重写了 updateTile，这里改为依赖 findPlan
        // 修改为：利用原版的 findPlan 机制

        // 重写 findPlan，只返回选定的配方（如果满足条件）
        @Override
        public AssemblerUnitPlan findPlan() {
            if (!configured || selectedPlan == null) return null;
            // 检查是否能生产
            if (canProduce(selectedPlan)) {
                return selectedPlan;
            }
            return null;
        }

        // 序列化
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
    }
}