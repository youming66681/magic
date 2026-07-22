package magical.content.type;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ctype.ContentType;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

/**
 * 完全自定义的单位组装厂（模拟 UnitAssembler 功能）
 */
public class FlexAssembler extends Block {
    public Seq<AssemblerRecipe> recipes = new Seq<>();
    public OrderedMap<String, Seq<AssemblerRecipe>> tierMap = new OrderedMap<>();

    public FlexAssembler(String name) {
        super(name);
        update = true;
        solid = true;
        hasPower = true;
        hasPayloads = true;       // 启用载荷存储
        acceptsPayload = true;    // 接受传送带输入的载荷
        payloadCapacity = 16;     // 最大载荷槽位
        configurable = true;
        saveConfig = true;
    }

    /**
     * 添加配方
     * @param tier      等级标签（如 "T1"）
     * @param output    生产的单位类型
     * @param time      生产耗时（秒）
     * @param areaSize  采摘范围视觉提示（格）
     * @param payloads  载荷需求，格式：{类型1, 数量1, 类型2, 数量2, ...}
     *                  类型可以是 UnitType 或 Block
     */
    public void addRecipe(String tier, UnitType output, float time, float areaSize, Object... payloads) {
        AssemblerRecipe recipe = new AssemblerRecipe(output, time, areaSize, payloads);
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
        public float customAreaSize = areaSize;   // 默认使用 block 的 areaSize
        public float progress;
        public float warmup;

        // ---------- 界面 ----------
        @Override
        public void buildConfiguration(Table table) {
            if (!configured) {
                Table tabs = new Table();
                Table content = new Table();

                for (String tier : tierMap.keys()) {
                    Button tabBtn = new Button(Tex.button);
                    tabBtn.label(() -> tier).growX();
                    String t = tier;
                    tabBtn.clicked(() -> {
                        content.clear();
                        buildIconGrid(content, tierMap.get(t));
                    });
                    tabs.add(tabBtn).growX().pad(4);
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
                table.label(() -> "Producing: " + selectedRecipe.output.localizedName).padBottom(4).row();
                table.button("Change", () -> {
                    configured = false;
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

        // ---------- 配置逻辑 ----------
        public void configureRecipe(AssemblerRecipe recipe) {
            selectedRecipe = recipe;
            configured = true;
            customAreaSize = recipe.areaSize;
            progress = 0f;
            warmup = 0f;
            configure(selectedRecipe.output.id);   // 保存配方 ID
        }

        @Override
        public Object config() {
            return configured && selectedRecipe != null ? selectedRecipe.output.id : null;
        }

        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                UnitType type = content.getByID(ContentType.unit, (Integer) value);
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

        // ---------- 生产逻辑 ----------
        @Override
        public void updateTile() {
            if (!configured || selectedRecipe == null) {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.02f);
                return;
            }
            if (power == null || power.status < 1f) {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.02f);
                return;
            }
            if (!hasPayloads(selectedRecipe)) {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.02f);
                return;
            }

            warmup = Mathf.lerpDelta(warmup, 1f, 0.02f);
            progress += delta() / (selectedRecipe.time * 60f);

            if (progress >= 1f) {
                consumePayloads(selectedRecipe);
                Unit u = selectedRecipe.output.create(team);
                u.set(this);
                u.add();
                progress = 0f;
            }
        }

        boolean hasPayloads(AssemblerRecipe recipe) {
            if (payloads == null) return false;
            // 统计每种载荷的数量
            for (ObjectIntMap.Entry<UnlockableContent> entry : recipe.payloadRequirements) {
                int count = 0;
                for (int i = 0; i < payloads.length; i++) {
                    if (payloads[i] != null && payloads[i].content() == entry.key) {
                        count++;
                    }
                }
                if (count < entry.value) return false;
            }
            return true;
        }

        void consumePayloads(AssemblerRecipe recipe) {
            for (ObjectIntMap.Entry<UnlockableContent> entry : recipe.payloadRequirements) {
                int toRemove = entry.value;
                for (int i = 0; i < payloads.length && toRemove > 0; i++) {
                    if (payloads[i] != null && payloads[i].content() == entry.key) {
                        payloads[i] = null;
                        toRemove--;
                    }
                }
            }
        }

        // ---------- 视觉 ----------
        @Override
        public void drawSelect() {
            if (configured) {
                Drawf.dashCircle(x, y, customAreaSize * 8f, Pal.accent);
            }
        }

        @Override
        public float progress() { return progress; }
        @Override
        public float warmup() { return warmup; }
    }

    // ==================== 配方类 ====================
    public static class AssemblerRecipe {
        public UnitType output;
        public float time;
        public float areaSize;
        public ObjectIntMap<UnlockableContent> payloadRequirements = new ObjectIntMap<>();

        public AssemblerRecipe(UnitType output, float time, float areaSize, Object... payloads) {
            this.output = output;
            this.time = time;
            this.areaSize = areaSize;
            for (int i = 0; i < payloads.length; i += 2) {
                UnlockableContent type = (UnlockableContent) payloads[i];
                int amount = (Integer) payloads[i + 1];
                payloadRequirements.put(type, amount);
            }
        }
    }
}