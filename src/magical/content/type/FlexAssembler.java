package magical.content.type;

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
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.units.*;
import mindustry.type.PayloadStack;
import mindustry.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

/**
 * 等级化单位组装厂（基于 UnitAssembler）
 */
public class FlexAssembler extends UnitAssembler {

    public Map<AssemblerUnitPlan, Float> areaPerPlan = new HashMap<>();
    public Map<AssemblerUnitPlan, Integer> tierRequired = new HashMap<>();
    public Map<AssemblerUnitPlan, String> planLabel = new HashMap<>();

    public FlexAssembler(String name) {
        super(name);
    }

    public void addPlan(String label, UnitType output, float time, float areaSize, int requiredTier, PayloadStack... requirements) {
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
        public float customAreaSize = (float) areaSize;

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
                    customAreaSize = (float) areaSize;
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

        public void selectPlan(AssemblerUnitPlan plan) {
            selectedPlan = plan;
            configured = true;
            customAreaSize = areaPerPlan.getOrDefault(plan, (float) areaSize);
            configure(selectedPlan.unit.id);
        }

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
                            customAreaSize = areaPerPlan.getOrDefault(p, (float) areaSize);
                            break;
                        }
                    }
                }
            }
            super.configure(value);
        }

        @Override
        public AssemblerUnitPlan plan() {
            if (configured && selectedPlan != null) {
                return selectedPlan;
            }
            return super.plan();
        }

        @Override
        public void drawSelect() {
            if (configured) {
                float fulls = customAreaSize * tilesize / 2f;
                Vec2 spawn = getUnitSpawn();
                Drawf.dashRect(Pal.accent, Tmp.r1.set(spawn.x - fulls, spawn.y - fulls, fulls * 2f, fulls * 2f));
            } else {
                super.drawSelect();
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (configured && selectedPlan != null) {
                if (tierRequired.getOrDefault(selectedPlan, 0) > currentTier) {
                    configured = false;
                    selectedPlan = null;
                    customAreaSize = (float) areaSize;
                }
            }
        }

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
            customAreaSize = read.f();
        }
    }
}