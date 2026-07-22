package magical.content;

import arc.*;
import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
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
 * 等级化单位组装厂（继承自 UnitAssembler）
 */
public class FlexAssembler extends UnitAssembler {

    // 存储每个配方的自定义采摘面积
    public Map<AssemblerUnitPlan, Float> areaPerPlan = new HashMap<>();
    // 等级标签 → 配方列表
    public OrderedMap<String, Seq<AssemblerUnitPlan>> tierMap = new OrderedMap<>();
    // 配方需要的载荷种类数（用于 UI 容量判断）
    public Map<AssemblerUnitPlan, Integer> requiredTypesPerPlan = new HashMap<>();

    public FlexAssembler(String name) {
        super(name);
        plans.clear(); // 手动管理配方
    }

    /** 添加一个配方（无物品消耗） */
    public void addPlan(String tier, UnitType output, float time, float areaSize, PayloadStack... inputs) {
        addPlan(tier, output, time, areaSize, null, inputs);
    }

    /** 添加一个配方（可带物品消耗） */
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

        int distinct = (int) Arrays.stream(inputs).map(s -> s.item).distinct().count();
        requiredTypesPerPlan.put(plan, distinct);
    }

    @Override
    public void setBars() {
        super.setBars(); // 使用原版进度条
    }

    // ========== 配置界面 ==========
    @Override
    public void buildConfiguration(Table table, Building build) {
        FlexAssemblerBuild entity = (FlexAssemblerBuild) build;
        int currentCapacity = entity.capacities != null ? entity.capacities.length : 0;

        if (!entity.configured) {
            Table tabs = new Table();
            Table content = new Table();

            for (String tier : tierMap.keys()) {
                Button tabBtn = new Button(Tex.button);
                tabBtn.label(() -> tier).growX();
                final String t = tier;
                tabBtn.clicked(() -> {
                    content.clear();
                    buildIconGrid(content, tierMap.get(t), entity, currentCapacity);
                });
                tabs.add(tabBtn).growX().pad(4);

                // 该等级下所有配方都不可用时禁用标签
                boolean allDisabled = tierMap.get(tier).allMatch(
                        p -> requiredTypesPerPlan.getOrDefault(p, 0) > currentCapacity
                );
                if (allDisabled) tabBtn.setDisabled(true);
            }
            tabs.row();
            ScrollPane pane = new ScrollPane(content);
            tabs.add(pane).colspan(tierMap.size).grow();

            if (tierMap.size > 0) {
                String first = tierMap.keys().next();
                buildIconGrid(content, tierMap.get(first), entity, currentCapacity);
            }

            table.add(tabs).grow();
        } else {
            table.label(() -> "Producing: " + entity.selectedPlan.unit.localizedName).padBottom(4).row();
            table.button("Change", () -> {
                entity.setConfigured(false);
                ui.build.hide();
            }).size(100f, 40f).row();
        }
    }

    private void buildIconGrid(Table grid, Seq<AssemblerUnitPlan> group, FlexAssemblerBuild entity, int currentCapacity) {
        grid.clear();
        int i = 0;
        for (AssemblerUnitPlan plan : group) {
            if (i % 4 == 0 && i != 0) grid.row();

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
        public float customAreaSize = areaSize; // areaSize 来自 Block

        public void setConfigured(boolean val) {
            configured = val;
            if (!val) {
                selectedPlan = null;
                customAreaSize = areaSize;
                capacities = FlexAssembler.this.capacities.clone();
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
            capacities = FlexAssembler.this.capacities.clone();
            productionProgress = 0f;
            warmup = 0f;
            configure(selectedPlan.unit.id);
        }

        @Override
        public Object config() {
            return configured && selectedPlan != null ? selectedPlan.unit.id : null;
        }

        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                UnitType type = content.getByID(ContentType.unit, (Integer)value);
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

        // 动态采摘范围
        @Override
        public float areaSize() {
            return configured ? customAreaSize : super.areaSize();
        }

        // 只生产选定的配方
        @Override
        public AssemblerUnitPlan findPlan() {
            if (!configured || selectedPlan == null) return null;
            if (canProduce(selectedPlan)) {
                return selectedPlan;
            }
            return null;
        }

        @Override
        public void drawSelect() {
            if (configured) {
                Drawf.dashCircle(x, y, customAreaSize * 8f, Pal.accent);
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(configured);
            write.f(customAreaSize);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            configured = read.bool();
            customAreaSize = read.f();
            // selectedPlan 会在 configure() 方法被调用时恢复（通过 config）
        }
    }
}