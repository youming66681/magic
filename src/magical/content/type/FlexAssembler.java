package magical.content;

import arc.*;
import arc.graphics.*;
import arc.math.*;
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
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class FlexAssembler extends Block {
    public Seq<AssemblerRecipe> recipes = new Seq<>();
    public OrderedMap<String, Seq<AssemblerRecipe>> tierMap = new OrderedMap<>();

    public FlexAssembler(String name) {
        super(name);
        update = true;
        solid = true;
        hasPower = true;
        configurable = true;
        saveConfig = true;
    }

    /** 添加配方：tier 标签，output 输出单位，time 秒，areaSize 格，inputs 需要的单位（可重复） */
    public void addRecipe(String tier, UnitType output, float time, float areaSize, UnitType... inputs) {
        AssemblerRecipe recipe = new AssemblerRecipe(output, time, areaSize, inputs);
        recipes.add(recipe);
        Seq<AssemblerRecipe> group = tierMap.get(tier);
        if (group == null) {
            group = new Seq<>();
            tierMap.put(tier, group);
        }
        group.add(recipe);
    }

    // ========== 建筑实体 ==========
    public class FlexAssemblerBuild extends Building {
        public AssemblerRecipe selectedRecipe;
        public boolean configured;
        public float customAreaSize = areaSize;
        public float progress;
        public float warmup;

        @Override
        public void buildConfiguration(Table table) {
            if (!configured) {
                Table tabs = new Table();
                Table content = new Table();
                for (String tier : tierMap.keys()) {
                    Button btn = new Button(Tex.button, Styles.cleari);
                    btn.label(() -> tier).growX();
                    String t = tier;
                    btn.clicked(() -> {
                        content.clear();
                        buildIconGrid(content, tierMap.get(t));
                    });
                    tabs.add(btn).growX().pad(4);
                }
                tabs.row();
                ScrollPane pane = new ScrollPane(content);
                tabs.add(pane).colspan(tierMap.size).grow();
                if (tierMap.size > 0) {
                    String first = tierMap.keys().next();
                    buildIconGrid(content, tierMap.get(first));
                }
                table.add(tabs).grow();
            } else {
                table.label("Producing: " + selectedRecipe.output.localizedName).padBottom(4).row();
                table.button("Change", () -> {
                    configured = false;
                    ui.build.hide();
                }).size(100f, 40f).row();
            }
        }

        private void buildIconGrid(Table grid, Seq<AssemblerRecipe> group) {
            grid.clear();
            int i = 0;
            for (AssemblerRecipe r : group) {
                if (i % 4 == 0 && i != 0) grid.row();
                ImageButton btn = new ImageButton(r.output.uiIcon, Styles.cleari);
                btn.resizeImage(40f);
                btn.clicked(() -> configureRecipe(r));
                btn.row();
                btn.add(r.output.localizedName).color(Color.lightGray);
                grid.add(btn).pad(4);
                i++;
            }
        }

        public void configureRecipe(AssemblerRecipe recipe) {
            selectedRecipe = recipe;
            configured = true;
            customAreaSize = recipe.areaSize;
            progress = 0f;
            warmup = 0f;
            configure(selectedRecipe.output.id); // 保存输出单位ID
        }

        @Override
        public Object config() {
            return configured && selectedRecipe != null ? selectedRecipe.output.id : null;
        }

        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                UnitType type = content.getByID(ContentType.unit, (Integer)value);
                if (type != null) {
                    for (AssemblerRecipe r : recipes) {
                        if (r.output == type) {
                            configureRecipe(r);
                            return;
                        }
                    }
                }
            }
            super.configure(value);
        }

        @Override
        public void updateTile() {
            if (!configured || selectedRecipe == null) {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.02f);
                return;
            }
            if (!power.status) {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.02f);
                return;
            }
            if (!hasRequiredUnits()) {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.02f);
                return;
            }
            warmup = Mathf.lerpDelta(warmup, 1f, 0.02f);
            progress += delta() / (selectedRecipe.time * 60f);
            if (progress >= 1f) {
                consumeUnits();
                Unit u = selectedRecipe.output.create(team);
                u.set(this);
                u.add();
                Fx.producesmoke.at(this);
                progress = 0f;
            }
        }

        boolean hasRequiredUnits() {
            float radius = customAreaSize * 8f;
            Seq<Unit> near = Groups.unit.within(x, y, radius)
                    .select(u -> u.team == team && !u.spawnedByCore);
            // 对每个需求单位，检查是否都存在
            for (UnitType required : selectedRecipe.inputs) {
                if (!near.contains(u -> u.type == required)) {
                    return false;
                }
            }
            return true;
        }

        void consumeUnits() {
            float radius = customAreaSize * 8f;
            Seq<Unit> near = Groups.unit.within(x, y, radius)
                    .select(u -> u.team == team && !u.spawnedByCore);
            for (UnitType required : selectedRecipe.inputs) {
                Unit found = near.find(u -> u.type == required);
                if (found != null) {
                    found.remove();
                    near.remove(found);
                }
            }
            Fx.absorb.at(this);
        }

        @Override
        public void drawSelect() {
            if (configured) {
                Drawf.dashCircle(x, y, customAreaSize * 8f, team.color);
            }
        }

        @Override
        public float progress() { return progress; }
        @Override
        public float warmup() { return warmup; }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(configured);
            write.f(customAreaSize);
            write.f(progress);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            configured = read.bool();
            customAreaSize = read.f();
            progress = read.f();
        }
    }

    // 配方数据类
    public static class AssemblerRecipe {
        public UnitType output;
        public float time;
        public float areaSize;
        public UnitType[] inputs;

        public AssemblerRecipe(UnitType output, float time, float areaSize, UnitType... inputs) {
            this.output = output;
            this.time = time;
            this.areaSize = areaSize;
            this.inputs = inputs;
        }
    }
}