package magical.content;

import arc.graphics.Color;
import arc.math.geom.Vec2;
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
import mindustry.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

public class FlexAssembler extends UnitAssembler {

    public Map<AssemblerUnitPlan, Integer> tierRequired = new HashMap<>();
    public Map<AssemblerUnitPlan, String> planLabel = new HashMap<>();
    // 不再需要 areaPerPlan，直接使用整数面积

    public FlexAssembler(String name) {
        super(name);
    }

    /**
     * @param label         等级标签（如 "T1"）
     * @param output        输出单位
     * @param time          耗时（秒）
     * @param customArea    该配方对应的采摘范围（格）
     * @param requiredTier  解锁需要的最小模块数量
     * @param requirements  载荷需求
     */
    public void addPlan(String label, UnitType output, float time, int customArea, int requiredTier, PayloadStack... requirements) {
        Seq<PayloadStack> reqSeq = new Seq<>(requirements);
        AssemblerUnitPlan plan = new AssemblerUnitPlan(output, time, reqSeq);
        plans.add(plan);
        // 用 plan 对象的 unit 作为 key 存储自定义面积（之后在 selectPlan 中读取）
        tierRequired.put(plan, requiredTier);
        planLabel.put(plan, label);
        // 存储面积到一个额外 map，这里我们直接使用 unit 作为 key 的 map
        areaMap.put(plan.unit, customArea);
    }

    // 存储每个输出单位对应的配方面积
    public Map<UnitType, Integer> areaMap = new HashMap<>();

    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        public boolean configured = false;
        public AssemblerUnitPlan selectedPlan;
        public int defaultArea = areaSize;   // 保存默认面积

        // ---------- 配方选择 UI ----------
        @Override
        public void buildConfiguration(Table table) {
            if (!configured) {
                // 收集当前模块等级下可用的配方，按标签分组
                OrderedMap<String, Seq<AssemblerUnitPlan>> grouped = new OrderedMap<>();
                for (AssemblerUnitPlan plan : plans) {
                    if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                        String label = planLabel.getOrDefault(plan, "T" + currentTier);
                        Seq<AssemblerUnitPlan> group = grouped.get(label);
                        if (group == null) {
                            group = new Seq<>();
                            grouped.put(label, group);
                        }
                        group.add(plan);
                    }
                }

                if (grouped.size == 0) {
                    table.label("No available plans (need more modules)").colspan(2).pad(10);
                    return;
                }

                Table tabs = new Table();
                Table content = new Table();

                // 标签按钮
                for (String tier : grouped.keys()) {
                    Button tabBtn = new Button(Tex.button);
                    tabBtn.label(() -> tier).growX();
                    final String t = tier;
                    tabBtn.clicked(() -> {
                        content.clear();
                        buildIconGrid(content, grouped.get(t));
                    });
                    tabs.add(tabBtn).growX().pad(4);
                }
                tabs.row();
                ScrollPane pane = new ScrollPane(content);
                tabs.add(pane).colspan(grouped.size).grow();

                // 默认显示第一个标签的内容
                if (grouped.size > 0) {
                    String first = grouped.keys().next();
                    buildIconGrid(content, grouped.get(first));
                }

                table.add(tabs).grow();
            } else {
                // 已配置状态
                table.label(() -> "Producing: " + selectedPlan.unit.localizedName).padBottom(4).row();
                table.button("Change", () -> {
                    configured = false;
                    selectedPlan = null;
                    areaSize = defaultArea;   // 恢复默认区域
                }).size(100f, 40f).row();
            }
        }

        private void buildIconGrid(Table grid, Seq<AssemblerUnitPlan> group) {
            grid.clear();
            int i = 0;
            for (AssemblerUnitPlan plan : group) {
                if (i % 4 == 0 && i != 0) grid.row();
                ImageButton btn = new ImageButton(plan.unit.uiIcon, Styles.cleari);
                btn.resizeImage(40f);
                btn.clicked(() -> selectPlan(plan));
                btn.row();
                btn.add(plan.unit.localizedName).color(Color.lightGray);
                grid.add(btn).pad(4);
                i++;
            }
        }

        /** 选定配方 */
        public void selectPlan(AssemblerUnitPlan plan) {
            selectedPlan = plan;
            configured = true;
            // 设置采摘区域为配方自定义值，若没有则使用默认
            int newArea = areaMap.getOrDefault(plan.unit, defaultArea);
            areaSize = newArea;                 // 直接修改父类字段
            configure(selectedPlan.unit.id);    // 保存到 config
        }

        // ---------- 配置序列化 ----------
        @Override
        public Object config() {
            return (configured && selectedPlan != null) ? selectedPlan.unit.id : null;
        }

        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                UnitType type = content.getByID(ContentType.unit, (Integer) value);
                if (type != null) {
                    for (AssemblerUnitPlan p : plans) {
                        if (p.unit == type) {
                            selectedPlan = p;
                            configured = true;
                            areaSize = areaMap.getOrDefault(type, defaultArea);
                            break;
                        }
                    }
                }
            }
            super.configure(value);
        }

        // ---------- 核心：覆盖 plan()，使生产只使用选定配方 ----------
        @Override
        public AssemblerUnitPlan plan() {
            if (configured && selectedPlan != null) {
                return selectedPlan;
            }
            return super.plan();
        }

        // ---------- 安全：模块降级时取消选择 ----------
        @Override
        public void updateTile() {
            super.updateTile();
            if (configured && selectedPlan != null) {
                if (tierRequired.getOrDefault(selectedPlan, 0) > currentTier) {
                    configured = false;
                    selectedPlan = null;
                    areaSize = defaultArea;
                }
            }
        }

        // ---------- 存档 ----------
        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(configured);
            if (configured && selectedPlan != null) {
                write.i(selectedPlan.unit.id);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            configured = read.bool();
            if (configured) {
                int id = read.i();
                UnitType type = content.getByID(ContentType.unit, id);
                if (type != null) {
                    for (AssemblerUnitPlan p : plans) {
                        if (p.unit == type) {
                            selectedPlan = p;
                            areaSize = areaMap.getOrDefault(type, defaultArea);
                            break;
                        }
                    }
                }
                if (selectedPlan == null) configured = false;
            }
        }
    }
}