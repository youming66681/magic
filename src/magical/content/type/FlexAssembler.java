package magical.content.type;

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

    public Map<AssemblerUnitPlan, Integer> planAreaMap = new HashMap<>();
    public Map<AssemblerUnitPlan, Integer> tierRequired = new HashMap<>();

    public FlexAssembler(String name) {
        super(name);
        configurable = true;
    }

    public void addPlan(String label, UnitType output, float time, int customArea, int requiredTier, PayloadStack... requirements) {
        Seq<PayloadStack> reqSeq = new Seq<>(requirements);
        AssemblerUnitPlan plan = new AssemblerUnitPlan(output, time, reqSeq);
        plans.add(plan);
        tierRequired.put(plan, requiredTier);
        planAreaMap.put(plan, customArea);
    }

    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        public boolean selected = false;
        public AssemblerUnitPlan chosenPlan;
        public int myAreaSize = areaSize;

        @Override
        public void buildConfiguration(Table table) {
            if (!selected) {
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
                        configure(plan.unit.id);
                        table.clear();
                        buildConfiguration(table);
                    });
                    grid.add(btn).size(90f, 90f).pad(4f);
                }

                ScrollPane pane = new ScrollPane(grid);
                table.add(pane).grow().maxHeight(400f).row();
                table.label(() -> Core.bundle.get("flexassembler.select-unit")).padTop(4).color(Color.gray);
            } else {
                table.label(() -> Core.bundle.format("flexassembler.producing", chosenPlan.unit.localizedName))
                        .padBottom(8).row();
                table.button(
                        Core.bundle.get("flexassembler.change"),
                        () -> {
                            selected = false;
                            chosenPlan = null;
                            myAreaSize = areaSize;
                            configure(null);
                            table.clear();
                            buildConfiguration(table);
                        }
                ).size(120f, 40f).row();
            }
        }

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

        @Override
        public AssemblerUnitPlan plan() {
            if (selected && chosenPlan != null) return chosenPlan;
            return super.plan();
        }

        @Override
        public void updateTile() {
            if (selected && chosenPlan != null) {
                if (tierRequired.getOrDefault(chosenPlan, 0) > currentTier) {
                    selected = false;
                    chosenPlan = null;
                    myAreaSize = areaSize;
                }
            }

            AssemblerUnitPlan plan = plan();
            myAreaSize = planAreaMap.getOrDefault(plan, areaSize);
            int prevArea = areaSize;
            areaSize = myAreaSize;
            super.updateTile();
            areaSize = prevArea;
        }

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