package magical.content;

import arc.Core;
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
import mindustry.world.*;
import mindustry.world.blocks.units.*;
import mindustry.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

public class FlexAssembler extends UnitAssembler {

    // 存储每个配方的自定义采摘面积 (格)
    public Map<AssemblerUnitPlan, Integer> planAreaMap = new HashMap<>();
    // 存储每个配方所需的最低模块数量
    public Map<AssemblerUnitPlan, Integer> tierRequired = new HashMap<>();

    public FlexAssembler(String name) {
        super(name);
    }

    /**
     * 添加一个配方。
     * @param label         分组标签 (未使用)
     * @param output        输出单位
     * @param time          耗时 (秒)
     * @param customArea    采摘区域大小 (格)
     * @param requiredTier  最低模块数量
     * @param requirements  载荷需求
     */
    public void addPlan(String label, UnitType output, float time, int customArea, int requiredTier, PayloadStack... requirements) {
        Seq<PayloadStack> reqSeq = new Seq<>(requirements);
        AssemblerUnitPlan plan = new AssemblerUnitPlan(output, time, reqSeq);
        plans.add(plan);
        tierRequired.put(plan, requiredTier);
        planAreaMap.put(plan, customArea);
    }

    // ===================== 建筑实例 =====================
    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        public boolean selected = false;           // 是否已选定配方
        public AssemblerUnitPlan chosenPlan;       // 当前选中的配方
        public int myAreaSize = areaSize;          // 当前建筑的实际面积

        // ---------- 配置面板 ----------
        @Override
        public void buildConfiguration(Table table) {
            if (!selected) {
                // 收集当前模块等级下可用的配方
                Seq<AssemblerUnitPlan> available = new Seq<>();
                for (AssemblerUnitPlan plan : plans) {
                    if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                        available.add(plan);
                    }
                }

                if (available.isEmpty()) {
                    table.label(() -> Core.bundle.get("flexassembler.no-plans")).pad(10);
                    return;
                }

                Table grid = new Table();
                int cols = 4;
                for (int i = 0; i < available.size; i++) {
                    if (i % cols == 0 && i != 0) grid.row();
                    AssemblerUnitPlan plan = available.get(i);
                    // 使用普通按钮，保证点击区域足够大
                    Button btn = new Button(Tex.button);
                    btn.table(inner -> {
                        inner.image(plan.unit.uiIcon).size(40f).padBottom(4f);
                        inner.row();
                        inner.add(plan.unit.localizedName).color(Color.lightGray);
                    }).pad(8);
                    btn.clicked(() -> {
                        chosenPlan = plan;
                        selected = true;
                        myAreaSize = planAreaMap.getOrDefault(plan, areaSize);
                        configure(plan.unit.id);   // 保存配置
                        table.clear();
                        buildConfiguration(table);  // 刷新面板
                    });
                    grid.add(btn).size(90f, 90f).pad(4f);
                }

                ScrollPane pane = new ScrollPane(grid);
                table.add(pane).grow().maxHeight(400f).row();
                table.label(() -> Core.bundle.get("flexassembler.select-unit")).padTop(4).color(Color.gray);
            } else {
                // 已选中配方时显示状态
                table.label(() -> Core.bundle.format("flexassembler.producing", chosenPlan.unit.localizedName))
                        .padBottom(8).row();
                table.button(
                        Core.bundle.get("flexassembler.change"),
                        () -> {
                            selected = false;
                            chosenPlan = null;
                            myAreaSize = areaSize;
                            configure(null);                // 清除 config
                            table.clear();
                            buildConfiguration(table);      // 回到选择界面
                        }
                ).size(120f, 40f).row();
            }
        }

        // ---------- 配置序列化 ----------
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
            } else if (value == null) {
                selected = false;
                chosenPlan = null;
                myAreaSize = areaSize;
            }
            super.configure(value);
        }

        // ---------- 生产核心 ----------
        @Override
        public AssemblerUnitPlan plan() {
            if (selected && chosenPlan != null) return chosenPlan;
            return super.plan();
        }

        @Override
        public void updateTile() {
            // 模块降级保护
            if (selected && chosenPlan != null) {
                if (tierRequired.getOrDefault(chosenPlan, 0) > currentTier) {
                    selected = false;
                    chosenPlan = null;
                    myAreaSize = areaSize;
                }
            }

            // 动态设置 areaSize，使原版所有逻辑使用配方专属面积
            AssemblerUnitPlan plan = plan();
            myAreaSize = planAreaMap.getOrDefault(plan, areaSize);
            int prevArea = areaSize;
            areaSize = myAreaSize;
            super.updateTile();          // 执行原版逻辑
            areaSize = prevArea;         // 恢复
        }

        // ---------- 绘制与区域 ----------
        @Override
        public void drawSelect() {
            float fulls = myAreaSize * tilesize / 2f;
            Vec2 spawn = getUnitSpawn();
            Drawf.dashRect(Pal.accent, Tmp.r1.set(spawn.x - fulls, spawn.y - fulls, fulls * 2f, fulls * 2f));
        }

        @Override
        public Vec2 getUnitSpawn() {
            float len = tilesize * (myAreaSize + block.size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }

        // 注意：不再重写 getRect，因为父类 UnitAssemblerBuild 没有该方法，避免编译错误。
        // 区域碰撞由 updateTile 中设置的 areaSize 和原版逻辑自动处理。

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