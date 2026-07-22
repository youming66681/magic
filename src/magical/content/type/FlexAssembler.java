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
    public Map<UnitType, Integer> areaMap = new HashMap<>();

    public FlexAssembler(String name) {
        super(name);
    }

    /**
     * @param label         等级标签（如 "T1"）
     * @param output        输出单位
     * @param time          耗时（秒）
     * @param customArea    该配方对应的采摘范围（格）
     * @param requiredTier  需要的最低模块数量
     * @param requirements  载荷需求
     */
    public void addPlan(String label, UnitType output, float time, int customArea, int requiredTier, PayloadStack... requirements) {
        Seq<PayloadStack> reqSeq = new Seq<>(requirements);
        AssemblerUnitPlan plan = new AssemblerUnitPlan(output, time, reqSeq);
        plans.add(plan);
        tierRequired.put(plan, requiredTier);
        planLabel.put(plan, label);
        areaMap.put(output, customArea);
    }

    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        public boolean configured = false;
        public AssemblerUnitPlan selectedPlan;
        public int myAreaSize = areaSize;   // 每个建筑独立的采摘范围

        // ---------- 配方选择 UI ----------
        @Override
        public void buildConfiguration(Table table) {
            if (!configured) {
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
                    table.label(() -> "No plans (need more modules)").growX().pad(10);
                    return;
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
                    myAreaSize = areaSize;   // 恢复默认
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
            myAreaSize = areaMap.getOrDefault(plan.unit, areaSize);
            configure(selectedPlan.unit.id);
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
                            myAreaSize = areaMap.getOrDefault(type, areaSize);
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
            if (configured && selectedPlan != null) {
                return selectedPlan;
            }
            return super.plan();
        }

        // ---------- 动态区域（使用实例 myAreaSize） ----------
        @Override
        public void drawSelect() {
            if (configured) {
                float fulls = myAreaSize * tilesize / 2f;
                Vec2 spawn = getUnitSpawn();
                Drawf.dashRect(Pal.accent, Tmp.r1.set(spawn.x - fulls, spawn.y - fulls, fulls * 2f, fulls * 2f));
            } else {
                super.drawSelect();
            }
        }

        // 重写 getUnitSpawn 使生成的单位位置和无人机轨道基于 myAreaSize
        @Override
        public Vec2 getUnitSpawn() {
            float len = tilesize * (myAreaSize + size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }

        // 安全：模块降级时取消选择
        @Override
        public void updateTile() {
            super.updateTile();
            if (configured && selectedPlan != null) {
                if (tierRequired.getOrDefault(selectedPlan, 0) > currentTier) {
                    configured = false;
                    selectedPlan = null;
                    myAreaSize = areaSize;
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
            write.i(myAreaSize);
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
                            break;
                        }
                    }
                }
                if (selectedPlan == null) configured = false;
            }
            myAreaSize = read.i();
            if (!configured) myAreaSize = areaSize;   // 恢复默认
        }
    }
}