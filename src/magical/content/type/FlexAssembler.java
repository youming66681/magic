package magical.content;

import arc.graphics.*;
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
import mindustry.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

public class FlexAssembler extends UnitAssembler {

    public Map<AssemblerUnitPlan, Integer> tierRequired = new HashMap<>();
    public Map<AssemblerUnitPlan, String> planLabel = new HashMap<>();
    // 存储配方对应的采摘面积
    public Map<AssemblerUnitPlan, Integer> planAreaMap = new HashMap<>();

    public FlexAssembler(String name) {
        super(name);
    }

    /**
     * 添加一个配方。
     * @param label         显示标签（如 "T1"）
     * @param output        输出单位
     * @param time          生产时间（秒）
     * @param customArea    采摘范围（格）
     * @param requiredTier  最低模块数量要求
     * @param requirements  载荷需求（PayloadStack）
     */
    public void addPlan(String label, UnitType output, float time, int customArea, int requiredTier, PayloadStack... requirements) {
        Seq<PayloadStack> reqSeq = new Seq<>(requirements);
        AssemblerUnitPlan plan = new AssemblerUnitPlan(output, time, reqSeq);
        plans.add(plan);
        tierRequired.put(plan, requiredTier);
        planLabel.put(plan, label);
        planAreaMap.put(plan, customArea);
    }

    // ==================== 建筑实例 ====================
    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        public boolean selected = false;          // 是否已选择配方
        public AssemblerUnitPlan chosenPlan;      // 当前选中配方
        public int myAreaSize = areaSize;         // 实例专属面积

        // ---------- 配置面板（直接显示所有可用配方的图标） ----------
        @Override
        public void buildConfiguration(Table table) {
            if (!selected) {
                // 筛选出当前模块等级可用的配方
                Seq<AssemblerUnitPlan> available = new Seq<>();
                for (AssemblerUnitPlan plan : plans) {
                    if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                        available.add(plan);
                    }
                }

                if (available.isEmpty()) {
                    table.label(() -> "No plans (need more modules)").pad(10);
                    return;
                }

                // 用按钮网格显示
                Table grid = new Table();
                int cols = 4;
                for (int i = 0; i < available.size; i++) {
                    if (i % cols == 0 && i != 0) grid.row();
                    AssemblerUnitPlan plan = available.get(i);
                    ImageButton btn = new ImageButton(plan.unit.uiIcon, Styles.cleari);
                    btn.resizeImage(40f);
                    btn.clicked(() -> {
                        choosePlan(plan);
                        // 选择后自动关闭配置面板（默认行为）
                    });
                    btn.row();
                    btn.add(plan.unit.localizedName).color(Color.lightGray).labelStyle(LabelStyle.outline);
                    grid.add(btn).pad(4);
                }

                ScrollPane pane = new ScrollPane(grid);
                table.add(pane).grow().maxHeight(400f).row();

                // 添加一个提示标签
                table.label(() -> "Select a unit to produce").padTop(4).color(Color.gray);
            } else {
                // 已选择配方时显示当前单位
                table.label(() -> "Producing: " + chosenPlan.unit.localizedName).padBottom(4).row();
                table.button("Change", () -> {
                    selected = false;
                    chosenPlan = null;
                    myAreaSize = areaSize; // 恢复默认面积
                    // 触发重新配置
                    ui.build.hide();
                }).size(100f, 40f).row();
            }
        }

        /** 选中一个配方 */
        public void choosePlan(AssemblerUnitPlan plan) {
            chosenPlan = plan;
            selected = true;
            myAreaSize = planAreaMap.getOrDefault(plan, areaSize);
            configure(plan.unit.id);   // 保存到 config
            ui.build.hide();           // 关闭配置面板
        }

        // ---------- 序列化 ----------
        @Override
        public Object config() {
            return (selected && chosenPlan != null) ? chosenPlan.unit.id : null;
        }

        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                UnitType type = content.getByID(ContentType.unit, (Integer) value);
                if (type != null) {
                    for (AssemblerUnitPlan p : plans) {
                        if (p.unit == type) {
                            chosenPlan = p;
                            selected = true;
                            myAreaSize = planAreaMap.getOrDefault(p, areaSize);
                            break;
                        }
                    }
                }
            }
            super.configure(value);
        }

        // ---------- 生产核心：重写 plan() ----------
        @Override
        public AssemblerUnitPlan plan() {
            if (selected && chosenPlan != null) return chosenPlan;
            return super.plan();
        }

        // ---------- 区域尺寸重写（使所有功能依赖 myAreaSize） ----------
        @Override
        public void drawSelect() {
            // 使用实例面积绘制矩形
            float fulls = myAreaSize * tilesize / 2f;
            Vec2 spawn = getUnitSpawn();
            Drawf.dashRect(Pal.accent, Tmp.r1.set(spawn.x - fulls, spawn.y - fulls, fulls * 2f, fulls * 2f));
        }

        @Override
        public Vec2 getUnitSpawn() {
            float len = tilesize * (myAreaSize + block.size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }

        // 重写区域碰撞判断，使建筑不会重叠放置
        @Override
        public Rect getRect(Rect rect, float x, float y, int rotation) {
            rect.setCentered(x, y, myAreaSize * tilesize);
            float len = tilesize * (myAreaSize + block.size) / 2f;
            rect.x += Geometry.d4x(rotation) * len;
            rect.y += Geometry.d4y(rotation) * len;
            return rect;
        }

        // 安全：模块降级时取消选择
        @Override
        public void updateTile() {
            super.updateTile();
            if (selected && chosenPlan != null) {
                if (tierRequired.getOrDefault(chosenPlan, 0) > currentTier) {
                    selected = false;
                    chosenPlan = null;
                    myAreaSize = areaSize;
                }
            }
        }

        // ---------- 存档 ----------
        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(selected);
            if (selected && chosenPlan != null) write.i(chosenPlan.unit.id);
            write.i(myAreaSize);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            selected = read.bool();
            if (selected) {
                int id = read.i();
                UnitType type = content.getByID(ContentType.unit, id);
                if (type != null) {
                    for (AssemblerUnitPlan p : plans) {
                        if (p.unit == type) {
                            chosenPlan = p;
                            break;
                        }
                    }
                }
                if (chosenPlan == null) selected = false;
            }
            myAreaSize = read.i();
            if (!selected) myAreaSize = areaSize;
        }
    }
}