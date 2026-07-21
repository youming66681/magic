package example;

import arc.*;
import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
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
    // ======================= 扩展的核心类 =======================
    public static class TieredAssemblerBlock extends UnitAssembler {

        // 存储每个配方对应的面积
        public Map<AssemblerUnitPlan, Float> areaPerPlan = new HashMap<>();
        // 存储等级标签 → 配方列表
        public OrderedMap<String, Seq<AssemblerUnitPlan>> tierMap = new OrderedMap<>();

        public TieredAssemblerBlock(String name) {
            super(name);
            plans.clear(); // 我们将手动管理 plans
        }

        /** 添加一个配方到指定等级 */
        public void addPlan(String tier, UnitType output, float time, float areaSize, PayloadStack... inputs) {
            AssemblerUnitPlan plan = new AssemblerUnitPlan(output, time, inputs);
            plans.add(plan);
            areaPerPlan.put(plan, areaSize);

            Seq<AssemblerUnitPlan> group = tierMap.get(tier);
            if (group == null) {
                group = new Seq<>();
                tierMap.put(tier, group);
            }
            group.add(plan);

            // 计算该配方所需的不同载荷类型数量（例如：不同单位/方块的数量）
            // 用于判断当前 capacities 是否足够
            int distinctTypes = (int) Arrays.stream(inputs).map(s -> s.item).distinct().count();
            // 将类型需求数量附加到计划上（用一个小技巧：利用 plan 本身的不使用的字段？）
            // 这里我们存储到自定义 Map 中
            requiredTypesPerPlan.put(plan, distinctTypes);
        }

        // 存储每个配方所需的最小 capacities 长度（即不同载荷类型数）
        public Map<AssemblerUnitPlan, Integer> requiredTypesPerPlan = new HashMap<>();

        @Override
        public void setBars() {
            super.setBars(); // 使用原版的进度条和暖机条
        }

        // ---------- 玩家配置界面 ----------
        @Override
        public void buildConfiguration(Table table, Building build) {
            TieredAssemblerBuild entity = (TieredAssemblerBuild) build;
            int currentCapacity = entity.capacities != null ? entity.capacities.length : 0;

            if (!entity.configured) {
                // 显示等级标签页 + 单位图标网格
                Table tabs = new Table();
                Table content = new Table();

                // 为每个等级创建一个标签按钮
                for (String tier : tierMap.keys()) {
                    Button tabBtn = new Button(Tex.button, Styles.cleart);
                    tabBtn.label(tier).growX();
                    String currentTier = tier;
                    tabBtn.clicked(() -> {
                        content.clear();
                        buildIconGrid(content, tierMap.get(currentTier), entity, currentCapacity);
                    });
                    tabs.add(tabBtn).growX().pad(4);

                    // 可选：如果该等级下所有配方都不可用，标签半透明
                    boolean allDisabled = tierMap.get(tier).allMatch(p -> requiredTypesPerPlan.getOrDefault(p, 0) > currentCapacity);
                    if (allDisabled) {
                        tabBtn.getStyle().disabled = Styles.clearPartiali;
                        tabBtn.setDisabled(true);  // 不能点击或视觉提示
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
                // 已配置：显示当前生产单位及更改按钮
                table.label("Producing: " + entity.selectedPlan.output().localizedName).padBottom(4).row();
                table.button("Change", () -> {
                    entity.setConfigured(false);
                    ui.build.hide();
                }).size(100f, 40f).row();
            }
        }

        private void buildIconGrid(Table grid, Seq<AssemblerUnitPlan> group, TieredAssemblerBuild entity, int currentCapacity) {
            grid.clear();
            int cols = 4;
            int i = 0;
            for (AssemblerUnitPlan plan : group) {
                if (i % cols == 0 && i != 0) grid.row();

                boolean canProduce = requiredTypesPerPlan.getOrDefault(plan, 0) <= currentCapacity;

                ImageButton button = new ImageButton(plan.output().uiIcon, Styles.clearPartiali);
                button.getStyle().imageUpColor = canProduce ? plan.output().color : Color.gray; // 不可用呈灰色
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
        public class TieredAssemblerBuild extends UnitAssemblerBuild {
            public boolean configured = false;
            public AssemblerUnitPlan selectedPlan;
            public float customAreaSize;
            public int lastCapacity = 0; // 用于检测容量变化

            public void setConfigured(boolean val) {
                configured = val;
                if (!val) {
                    selectedPlan = null;
                    customAreaSize = block().areaSize;
                    capacities = block().capacities.clone(); // 重置容量到基础值
                    // 注意：实际 capacites 会在下一帧更新，这里仅清除选择
                }
            }

            /** 选择配方时调用 */
            public void configure(AssemblerUnitPlan plan) {
                selectedPlan = plan;
                configured = true;
                // 设置该配方的拾取范围
                Float area = areaPerPlan.get(plan);
                customAreaSize = area != null ? area : block().areaSize;
                // 根据配方的载荷需求重置容量槽位（这里我们会依赖模块，但容量值在父类中管理）
                // 我们不需要手动设置 capacities，因为原版会处理
                this.currentPlan = null;
                this.progress = 0;
                this.warmup = 0;
                configure(plan.output().name); // 保存配置
            }

            @Override
            public Object config() {
                return configured && selectedPlan != null ? selectedPlan.output().name : null;
            }

            @Override
            public void configure(@Nullable Object value) {
                if (value instanceof String && !((String)value).isEmpty()) {
                    UnitType type = content.getByName(ContentType.unit, (String)value);
                    if (type != null) {
                        for (AssemblerUnitPlan p : block().plans) {
                            if (p.output() == type) {
                                configure(p);
                                return;
                            }
                        }
                    }
                }
                super.configure(value);
            }

            // ---------- 动态范围 ----------
            @Override
            public float areaSize() {
                return configured ? customAreaSize : super.areaSize();
            }

            // ---------- 限定只执行选定的配方，并检查容量 ----------
            @Override
            public AssemblerUnitPlan findPlan() {
                if (!configured || selectedPlan == null) return null;
                // 检查当前是否能生产（包括容量）
                if (canProduce(selectedPlan)) {
                    return selectedPlan;
                }
                return null;
            }

            // 检测 capacity 变化，如果变低导致当前配方不可用，自动取消配置
            @Override
            public void updateTile() {
                if (!configured) {
                    super.updateTile();
                    return;
                }
                // 容量检查：如果 capacities 长度不足以支持当前配方，取消选择
                int reqTypes = requiredTypesPerPlan.getOrDefault(selectedPlan, 0);
                if (capacities != null && capacities.length < reqTypes) {
                    // 自动重置
                    setConfigured(false);
                }
                super.updateTile();
            }

            // 绘制选定配方的有效范围
            @Override
            public void drawSelect() {
                if (configured) {
                    Drawf.dashCircle(x, y, customAreaSize * 8f, team.color);
                }
            }

            // 存档 & 读档
            @Override
            public void write(Writes write) {
                super.write(write);
                write.bool(configured);
                write.str(selectedPlan != null ? selectedPlan.output().name : "");
                write.f(customAreaSize);
            }

            @Override
            public void read(Reads read, byte revision) {
                super.read(read, revision);
                configured = read.bool();
                String name = read.str();
                float area = read.f();
                if (!name.isEmpty()) {
                    UnitType type = content.getByName(ContentType.unit, name);
                    if (type != null) {
                        // 先设置 selectedPlan，但要确保 capacity 足够，否则不配置
                        AssemblerUnitPlan found = null;
                        for (AssemblerUnitPlan p : block().plans) {
                            if (p.output() == type) {
                                found = p;
                                break;
                            }
                        }
                        if (found != null) {
                            int req = requiredTypesPerPlan.getOrDefault(found, 0);
                            if (capacities != null && capacities.length >= req) {
                                configure(found);
                            } else {
                                configured = false; // 容量不足，不恢复配方
                            }
                        } else {
                            configured = false;
                        }
                    } else {
                        configured = false;
                    }
                }
                if (!configured) customAreaSize = area;
            }
        }
    }
}