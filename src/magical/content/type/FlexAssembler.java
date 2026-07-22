package magical.content;

import arc.graphics.Color;
import arc.math.geom.Rect;
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
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.units.*;
import mindustry.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

/**
 * 可手动选择配方的单位组装厂。
 * 等级分组，动态采摘范围，支持方块载荷。
 */
public class FlexAssembler extends UnitAssembler {

    // 存储每个配方对应的采摘范围（格）
    public Map<AssemblerUnitPlan, Float> areaPerPlan = new HashMap<>();
    // 存储每个配方需要的最低模块等级（currentTier）
    public Map<AssemblerUnitPlan, Integer> tierRequired = new HashMap<>();
    // 存储配方的 UI 标签（如 "T1", "T2" ...）
    public Map<AssemblerUnitPlan, String> planLabel = new HashMap<>();

    public FlexAssembler(String name) {
        super(name);
    }

    /**
     * 添加一个配方。
     * @param label         等级标签（用于 UI 分组）
     * @param output        输出单位
     * @param time          生产时间（秒）
     * @param areaSize      采摘区域大小（格）
     * @param requiredTier  需要多少个模块才能解锁（0 为基础）
     * @param requirements  载荷需求（PayloadStack 数组）
     */
    public void addPlan(String label, UnitType output, float time, float areaSize, int requiredTier, PayloadStack... requirements) {
        // 将 PayloadStack 数组转为 Seq
        Seq<PayloadStack> reqSeq = new Seq<>(requirements);
        AssemblerUnitPlan plan = new AssemblerUnitPlan(output, time, reqSeq);
        plans.add(plan);
        areaPerPlan.put(plan, areaSize);
        tierRequired.put(plan, requiredTier);
        planLabel.put(plan, label);
    }

    // ==================== 建筑实体 ====================
    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        public boolean configured = false;
        public AssemblerUnitPlan selectedPlan;
        public float customAreaSize = areaSize; // 默认值

        // ---------- 配方选择 UI ----------
        @Override
        public void buildConfiguration(Table table) {
            if (!configured) {
                // 收集当前 tier 下所有可用的配方，按标签分组
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

                Table tabs = new Table();
                Table content = new Table();

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

                if (grouped.size > 0) {
                    String first = grouped.keys().next();
                    buildIconGrid(content, grouped.get(first));
                }

                table.add(tabs).grow();
            } else {
                table.label(() -> "Producing: " + selectedPlan.unit.localizedName).padBottom(4).row();
                table.button("Change", () -> {
                    configured = false;
                    selectedPlan = null;
                    customAreaSize = areaSize;
                    ui.build.hide(); // 重新打开配置面板
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
                btn.clicked(() -> {
                    selectPlan(plan);
                    ui.build.hide(); // 关闭配置面板
                });
                btn.row();
                btn.add(plan.unit.localizedName).color(Color.lightGray);
                grid.add(btn).pad(4);
                i++;
            }
        }

        /** 选择一个配方 */
        public void selectPlan(AssemblerUnitPlan plan) {
            selectedPlan = plan;
            configured = true;
            customAreaSize = areaPerPlan.getOrDefault(plan, areaSize);
            configure(selectedPlan.unit.id); // 保存到 config
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
                            customAreaSize = areaPerPlan.getOrDefault(p, areaSize);
                            break;
                        }
                    }
                }
            }
            super.configure(value);
        }

        // ---------- 核心：覆盖 plan() 返回选定配方 ----------
        @Override
        public AssemblerUnitPlan plan() {
            if (configured && selectedPlan != null) {
                return selectedPlan;
            }
            return super.plan(); // 未选择时使用原版默认
        }

        // ---------- 动态区域绘制 ----------
        @Override
        public void drawSelect() {
            if (configured) {
                // 绘制自定义矩形
                Rect rect = getCustomRect(Tmp.r1);
                Drawf.dashRect(Pal.accent, rect);
            } else {
                super.drawSelect(); // 未配置时使用原版
            }
        }

        /** 获取自定义采摘范围的矩形 */
        public Rect getCustomRect(Rect rect) {
            float fulls = customAreaSize * tilesize / 2f;
            Vec2 spawn = getUnitSpawn();
            return rect.set(spawn.x - fulls, spawn.y - fulls, fulls * 2f, fulls * 2f);
        }

        // ---------- 安全检查：如果当前 tier 下降导致配方不可用，自动取消选择 ----------
        @Override
        public void updateTile() {
            super.updateTile();
            if (configured && selectedPlan != null) {
                if (tierRequired.getOrDefault(selectedPlan, 0) > currentTier) {
                    configured = false;
                    selectedPlan = null;
                    customAreaSize = areaSize;
                }
            }
        }

        // ---------- 存档支持 ----------
        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(configured);
            if (configured && selectedPlan != null) {
                write.i(selectedPlan.unit.id);
            }
            write.f(customAreaSize);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            // 原版 revision 可能是 1，我们的扩展数据在末尾，且 revision 未变，但我们可以安全读取（如果存档版本不同，需另做处理）
            configured = read.bool();
            if (configured) {
                int id = read.i();
                UnitType type = content.getByID(ContentType.unit, id);
                if (type != null) {
                    for (AssemblerUnitPlan p : plans) {
                        if (p.unit == type) {
                            selectedPlan = p;
                            break;
                        }
                    }
                }
                if (selectedPlan == null) configured = false; // 未找到配方则清除状态
            }
            customAreaSize = read.f();
        }
    }
}