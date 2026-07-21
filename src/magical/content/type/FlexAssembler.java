package magical.content.type;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class FlexAssembler extends Block {

    // 手动加载纹理（避免 @Load 问题）
    public TextureRegion sideRegion1;
    public TextureRegion sideRegion2;

    public int areaSize = 11;
    public Sound createSound = Sounds.unitBuild;
    public float createSoundVolume = 1f;

    /** 模块方块 */
    public Block moduleBlock = null;

    /** 装配等级列表 */
    public Seq<AssemblerLevel> levels = new Seq<>();

    public FlexAssembler(String name) {
        super(name);
        update = true;
        solid = true;
        hasItems = false;
        hasPower = true;
        configurable = true;
        size = 3;
    }

    @Override
    public void load() {
        super.load();
        // 手动加载纹理
        sideRegion1 = Core.atlas.find(name + "-side1");
        sideRegion2 = Core.atlas.find(name + "-side2");

    }

    // ========== 建筑内部类 ==========
    public class FlexAssemblerBuild extends Building implements PayloadAcceptor<Building> {

        public int currentLevel = 0;
        public int currentRecipe = 0;
        public boolean crafting = false;
        public float progress = 0f;

        // 自定义载荷仓库（因为 Building 没有自带的 payloads 字段！）
        public Seq<Payload> payloads = new Seq<>();

        // ----- 模块检测 -----
        public int countModules() {
            if (moduleBlock == null) return levels.size - 1;
            int c = 0;
            int off = (areaSize - 1) / 2;
            // tile 在 Building 中可以直接用 this.tile (或直接 tile，因为继承了父类的 protected 字段)
            for (int dx = -off; dx <= off; dx++)
                for (int dy = -off; dy <= off; dy++) {
                    // 注意：world.buildWorld 在 v8 中可用，v7 中可能是 world.locateBuilding 或 world.tile(x,y).build
                    // 但根据先前的报错，v8 存在 world.buildWorld，我们仍用
                    Building b = world.buildWorld(tile.x + dx, tile.y + dy);
                    if (b != null && b.block == moduleBlock) c++;
                }
            return Math.min(c, levels.size - 1);
        }

        public int maxAvailableLevel() { return countModules(); }

        // ----- 载荷接口 -----
        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            // 检查是否任何配方需要
            for (AssemblerLevel level : levels) {
                for (UnitRecipe r : level.recipes) {
                    for (PayloadStack s : r.payloadCost) {
                        if (s.block != null && payload instanceof BlockPayload
                                && ((BlockPayload) payload).block() == s.block) return true;
                        if (s.unit != null && payload instanceof UnitPayload
                                && ((UnitPayload) payload).unit.type == s.unit) return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            // payload.set 在 v8 中需要两个 float 参数？不，Payload.set(Building) 也存在，但有多个重载
            // 稳妥起见：payload.set(null); 存在吗？ 我们先尝试 payload.detach() 或直接移除原方块所有权
            payload.attached(source); // 防止原来的方块丢失
            payloads.add(payload);
        }

        // 重写 getPayloads 以返回自定义容器
        @Override
        public Seq<Payload> getPayloads() {
            return payloads;
        }

        // 统计
        public int countPayload(Block block) {
            int c = 0;
            for (Payload p : payloads) {
                if (p instanceof BlockPayload && ((BlockPayload) p).block() == block) c++;
            }
            return c;
        }

        public int countPayload(UnitType unit) {
            int c = 0;
            for (Payload p : payloads) {
                if (p instanceof UnitPayload && ((UnitPayload) p).unit.type == unit) c++;
            }
            return c;
        }

        // 移除
        public void removePayload(Block block, int amount) {
            int removed = 0;
            for (int i = payloads.size - 1; i >= 0 && removed < amount; i--) {
                Payload p = payloads.get(i);
                if (p instanceof BlockPayload && ((BlockPayload) p).block() == block) {
                    p.remove();
                    payloads.remove(i);
                    removed++;
                }
            }
        }

        public void removePayload(UnitType unit, int amount) {
            int removed = 0;
            for (int i = payloads.size - 1; i >= 0 && removed < amount; i--) {
                Payload p = payloads.get(i);
                if (p instanceof UnitPayload && ((UnitPayload) p).unit.type == unit) {
                    p.remove();
                    payloads.remove(i);
                    removed++;
                }
            }
        }

        // ----- 生产逻辑 -----
        @Override
        public void updateTile() {
            int max = maxAvailableLevel();
            if (currentLevel > max) {
                currentLevel = max;
                currentRecipe = 0;
                crafting = false;
                progress = 0f;
            }
            UnitRecipe recipe = getCurrentRecipe();
            if (recipe == null) return;

            if (!crafting) {
                boolean can = true;
                for (PayloadStack stack : recipe.payloadCost) {
                    int have = (stack.block != null) ? countPayload(stack.block) : countPayload(stack.unit);
                    if (have < stack.amount) { can = false; break; }
                }
                if (can) {
                    for (PayloadStack stack : recipe.payloadCost) {
                        if (stack.block != null) removePayload(stack.block, stack.amount);
                        else removePayload(stack.unit, stack.amount);
                    }
                    crafting = true;
                    progress = 0f;
                }
            }

            if (crafting) {
                float speed = 1f;
                progress += Time.delta * 60f * speed;
                if (progress >= recipe.craftTime) {
                    crafting = false;
                    if (!net.client()) {
                        for (int i = 0; i < recipe.outputCount; i++) {
                            Unit unit = recipe.unit.spawn(team, tile.worldx(), tile.worldy());
                            unit.add();
                        }
                    }
                    createSound.at(x, y);
                    progress = 0f;
                }
            }
        }

        public UnitRecipe getCurrentRecipe() {
            if (levels.isEmpty() || currentLevel >= levels.size) return null;
            AssemblerLevel level = levels.get(currentLevel);
            if (level == null || level.recipes.isEmpty()) return null;
            if (currentRecipe >= level.recipes.size) return null;
            return level.recipes.get(currentRecipe);
        }

        // ----- 玩家界面 -----
        @Override
        public void buildConfiguration(Table table) {
            int maxLevel = maxAvailableLevel();

            table.table(row -> {
                row.button(Icon.left, Styles.cleari, () -> {
                    int prev = currentLevel - 1;
                    if (prev < 0) prev = maxLevel;
                    selectLevelRecipe(prev, 0);
                }).size(40);

                row.add("[accent]Tier " + (currentLevel + 1)).padLeft(10).padRight(10);

                row.button(Icon.right, Styles.cleari, () -> {
                    int next = currentLevel + 1;
                    if (next > maxLevel) next = 0;
                    selectLevelRecipe(next, 0);
                }).size(40);
            }).row();

            AssemblerLevel level = levels.get(currentLevel);
            if (level != null) {
                table.table(icons -> {
                    for (int i = 0; i < level.recipes.size; i++) {
                        final int idx = i;
                        UnitRecipe recipe = level.recipes.get(i);
                        TextureRegion icon = recipe.unit.uiIcon;
                        icons.button(new TextureRegionDrawable(icon), Styles.cleari, 48, () -> {
                            selectLevelRecipe(currentLevel, idx);
                        }).size(50).pad(4);
                    }
                });
            }

            table.row();
            table.add("Modules: " + countModules() + " / " + (levels.size - 1))
                    .style(Styles.outlineLabel).padTop(4);
        }

        public void selectLevelRecipe(int levelIdx, int recipeIdx) {
            if (levelIdx < 0 || levelIdx >= levels.size) return;
            if (recipeIdx < 0 || recipeIdx >= levels.get(levelIdx).recipes.size) return;
            if (levelIdx > maxAvailableLevel()) return;
            int data = (levelIdx << 8) | recipeIdx;
            Call.tileConfig(player, this, data);
        }

        @Override
        public void configureTile(Unit player, Object value) {
            if (value instanceof Integer data) {
                int levelIdx = (data >> 8) & 0xFF;
                int recipeIdx = data & 0xFF;
                if (levelIdx >= 0 && levelIdx < levels.size && levelIdx <= maxAvailableLevel()) {
                    AssemblerLevel level = levels.get(levelIdx);
                    if (recipeIdx >= 0 && recipeIdx < level.recipes.size) {
                        currentLevel = levelIdx;
                        currentRecipe = recipeIdx;
                        crafting = false;
                        progress = 0f;
                    }
                }
            }
        }

        // ----- 绘制 -----
        @Override
        public void draw() {
            super.draw();
            UnitRecipe recipe = getCurrentRecipe();
            if (recipe == null) return;

            Draw.color(team.color, 0.8f);
            Draw.rect(recipe.unit.uiIcon, x, y, 8, 8);
            Draw.color();

            if (crafting) {
                float f = progress / recipe.craftTime;
                float barW = block.size * tilesize - 8;
                float barH = 4;
                Draw.color(Color.gray);
                Fill.rect(x, y + block.size * tilesize / 2f - 6, barW, barH);
                Draw.color(Color.green);
                Fill.rect(x - barW/2f + f * barW/2f, y + block.size * tilesize / 2f - 6, f * barW, barH);
                Draw.color();
            }
        }

        // ----- 同步 -----
        @Override
        public void write(Writes write) {
            super.write(write);
            write.b(currentLevel);
            write.b(currentRecipe);
            write.f(progress);
            write.bool(crafting);
            // 载荷序列化
            write.i(payloads.size);
            for (Payload p : payloads) {
                TypeIO.writePayload(write, p);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            currentLevel = read.ub();
            currentRecipe = read.ub();
            progress = read.f();
            crafting = read.bool();
            int count = read.i();
            payloads.clear();
            for (int i = 0; i < count; i++) {
                payloads.add(TypeIO.readPayload(read));
            }
        }
    }

    // ========== 数据结构 ==========
    public static class UnitRecipe {
        public UnitType unit;
        public float craftTime;
        public PayloadStack[] payloadCost;
        public int outputCount = 1;

        public UnitRecipe() {}
        public UnitRecipe(UnitType unit, float craftTime, PayloadStack[] payloadCost) {
            this.unit = unit;
            this.craftTime = craftTime;
            this.payloadCost = payloadCost;
        }
    }

    public static class AssemblerLevel {
        public Seq<UnitRecipe> recipes = new Seq<>();
    }

    public static class PayloadStack {
        public Block block;
        public UnitType unit;
        public int amount;

        public PayloadStack(Block block, int amount) { this.block = block; this.amount = amount; }
        public PayloadStack(UnitType unit, int amount) { this.unit = unit; this.amount = amount; }
    }
}