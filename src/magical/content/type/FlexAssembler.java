package magical.content;

import arc.graphics.*;
import arc.math.geom.*;
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
import mindustry.type.PayloadStack;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.units.*;
import mindustry.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

public class FlexAssembler extends UnitAssembler {

    // 存储每个配方的自定义面积
    public Map<AssemblerUnitPlan, Integer> planAreaMap = new HashMap<>();
    // 存储每个配方所需的模块等级
    public Map<AssemblerUnitPlan, Integer> tierRequired = new HashMap<>();

    public FlexAssembler(String name) {
        super(name);
    }

    /**
     * 添加一个配方。
     * @param groupLabel    分组标签（目前未用于 UI，保留扩展）
     * @param output        输出单位
     * @param time          耗时（秒）
     * @param customArea    采摘区域大小（格）
     * @param requiredTier  最低模块数量
     * @param requirements  载荷需求
     */
    public void addPlan(String groupLabel, UnitType output, float time, int customArea, int requiredTier, PayloadStack... requirements) {
        Seq<PayloadStack> reqSeq = new Seq<>(requirements);
        AssemblerUnitPlan plan = new AssemblerUnitPlan(output, time, reqSeq);
        plans.add(plan);
        tierRequired.put(plan, requiredTier);
        planAreaMap.put(plan, customArea);
    }

    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        public boolean selected = false;
        public AssemblerUnitPlan chosenPlan;
        public int myAreaSize = areaSize;   // 默认面积

        // ---------- 配置界面 ----------
        @Override
        public void buildConfiguration(Table table) {
            if (!selected) {
                // 收集当前模块等级可用的配方
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

                Table grid = new Table();
                int cols = 4;
                for (int i = 0; i < available.size; i++) {
                    if (i % cols == 0 && i != 0) grid.row();
                    AssemblerUnitPlan plan = available.get(i);
                    // 使用普通的按钮，增加可点击区域
                    Button btn = new Button(Tex.button);
                    btn.table(t -> {
                        t.image(plan.unit.uiIcon).size(40f).pad(4);
                        t.row();
                        t.add(plan.unit.localizedName).color(Color.lightGray);
                    });
                    btn.clicked(() -> choosePlan(plan));
                    grid.add(btn).pad(4).size(80f, 80f);
                }

                ScrollPane pane = new ScrollPane(grid);
                table.add(pane).grow().maxHeight(400f).row();
                table.label(() -> "Select a unit to produce").padTop(4).color(Color.gray);
            } else {
                table.label(() -> "Producing: " + chosenPlan.unit.localizedName).padBottom(4).row();
                table.button("Change", () -> {
                    selected = false;
                    chosenPlan = null;
                    myAreaSize = areaSize;   // 恢复默认
                }).size(100f, 40f).row();
            }
        }

        /** 选中配方 */
        public void choosePlan(AssemblerUnitPlan plan) {
            chosenPlan = plan;
            selected = true;
            myAreaSize = planAreaMap.getOrDefault(plan, areaSize);
            // 使用 config 保存，触发界面更新
            configure(plan.unit.id);
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

        // ---------- 生产核心：返回选定配方 ----------
        @Override
        public AssemblerUnitPlan plan() {
            if (selected && chosenPlan != null) return chosenPlan;
            return super.plan();
        }

        // 覆盖 findPlan，如果父类使用了它
        public AssemblerUnitPlan findPlan() {
            if (selected && chosenPlan != null) return chosenPlan;
            return super.plan();
        }

        // ---------- 动态区域绘制 ----------
        @Override
        public void drawSelect() {
            // 绘制自定义虚线矩形
            float fulls = myAreaSize * tilesize / 2f;
            Vec2 spawn = getUnitSpawn();
            Drawf.dashRect(Pal.accent, Tmp.r1.set(spawn.x - fulls, spawn.y - fulls, fulls * 2f, fulls * 2f));
        }

        // 重写 getUnitSpawn，使单位生成位置适应可能变化的区域
        @Override
        public Vec2 getUnitSpawn() {
            float len = tilesize * (myAreaSize + block.size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }

        // 安全：模块降级时取消选择
        @Override
        public void updateTile() {
            super.updateTile();   // 完全使用原版逻辑，不干扰
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