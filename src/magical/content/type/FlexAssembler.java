package magical.content;   // 替换为你的真实包名

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

/**
 * 灵活单位装配机 – 多等级多配方 + 模块解锁 + 载荷消耗
 * 适配 Mindustry v7 build 159
 */
public class FlexAssembler extends Block {

    public @Load("@-side1") TextureRegion sideRegion1;
    public @Load("@-side2") TextureRegion sideRegion2;

    public int areaSize = 11;
    public Sound createSound = Sounds.unitBuild;    // 159 正确音效名
    public float createSoundVolume = 1f;

    /** 用于解锁等级的模块方块（在 MLBlocks 中赋值） */
    public Block moduleBlock = null;

    /** 所有装配等级 */
    public Seq<AssemblerLevel> levels = new Seq<>();

    public FlexAssembler(String name) {
        super(name);
        update = true;
        solid = true;
        hasItems = false;
        hasPower = true;
        configurable = true;
        payloadCapacity = 8;
        size = 3;
    }

    // ========== 建筑内部类 ==========
    public class FlexAssemblerBuild extends Building implements PayloadAcceptor<Building> {

        public int currentLevel = 0;
        public int currentRecipe = 0;
        public boolean crafting = false;
        public float progress = 0f;

        public Seq<Payload> storedPayloads = new Seq<>();

        // ---------- 模块计数 ----------
        public int countModules() {
            if (moduleBlock == null) return levels.size - 1;
            int count = 0;
            int offset = (areaSize - 1) / 2;
            for (int dx = -offset; dx <= offset; dx++) {
                for (int dy = -offset; dy <= offset; dy++) {
                    Building b = world.buildWorld(tile.x + dx, tile.y + dy);
                    if (b != null && b.block == moduleBlock) count++;
                }
            }
            return Math.min(count, levels.size - 1);
        }

        public int maxAvailableLevel() {
            return countModules();
        }

        // ---------- 载荷接口 ----------
        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            for (AssemblerLevel level : levels) {
                for (UnitRecipe recipe : level.recipes) {
                    for (PayloadStack stack : recipe.payloadCost) {
                        if (stack.block != null && payload instanceof BlockPayload
                                && ((BlockPayload) payload).block() == stack.block) return true;
                        if (stack.unit != null && payload instanceof UnitPayload
                                && ((UnitPayload) payload).unit.type == stack.unit) return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            // 立即接管载荷，放入自己的容器
            payload.set(this);
            storedPayloads.add(payload);
        }

        @Override
        public Seq<Payload> getPayloads() {
            return storedPayloads;
        }

        /** 统计方块载荷数量 */
        public int countPayload(Block block) {
            int c = 0;
            for (Payload p : storedPayloads) {
                if (p instanceof BlockPayload && ((BlockPayload) p).block() == block) c++;
            }
            return c;
        }

        /** 统计单位载荷数量 */
        public int countPayload(UnitType unit) {
            int c = 0;
            for (Payload p : storedPayloads) {
                if (p instanceof UnitPayload && ((UnitPayload) p).unit.type == unit) c++;
            }
            return c;
        }

        /** 移除指定数量的方块载荷 */
        public void removePayload(Block block, int amount) {
            int removed = 0;
            for (int i = storedPayloads.size - 1; i >= 0 && removed < amount; i--) {
                Payload p = storedPayloads.get(i);
                if (p instanceof BlockPayload && ((BlockPayload) p).block() == block) {
                    p.remove();
                    storedPayloads.remove(i);
                    removed++;
                }
            }
        }

        /** 移除指定数量的单位载荷 */
        public void removePayload(UnitType unit, int amount) {
            int removed = 0;
            for (int i = storedPayloads.size - 1; i >= 0 && removed < amount; i--) {
                Payload p = storedPayloads.get(i);
                if (p instanceof UnitPayload && ((UnitPayload) p).unit.type == unit) {
                    p.remove();
                    storedPayloads.remove(i);
                    removed++;
                }
            }
        }

        // ---------- 生产逻辑 ----------
        @Override
        public void updateTile() {
            // 动态调整可用的最大等级
            int maxLevel = maxAvailableLevel();
            if (currentLevel > maxLevel) {
                currentLevel = maxLevel;
                currentRecipe = 0;
                crafting = false;
                progress = 0f;
            }

            UnitRecipe recipe = getCurrentRecipe();
            if (recipe == null) return;

            if (!crafting) {
                boolean canCraft = true;
                for (PayloadStack stack : recipe.payloadCost) {
                    int have;
                    if (stack.block != null) have = countPayload(stack.block);
                    else have = countPayload(stack.unit);
                    if (have < stack.amount) {
                        canCraft = false;
                        break;
                    }
                }

                if (canCraft) {
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
                    createSound.at(this);
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

        // ---------- 玩家界面 ----------
        @Override
        public void buildConfiguration(Table table) {
            int maxLevel = maxAvailableLevel();

            // 等级切换栏
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

            // 单位图标选择
            AssemblerLevel level = levels.get(currentLevel);
            if (level != null) {
                table.table(icons -> {
                    for (int i = 0; i < level.recipes.size; i++) {
                        final int idx = i;
                        UnitRecipe recipe = level.recipes.get(i);
                        TextureRegion icon = recipe.unit.uiIcon;   // 159 兼容写法
                        icons.button(new TextureRegionDrawable(icon), Styles.cleari, 48, () -> {
                            selectLevelRecipe(currentLevel, idx);
                        }).size(50).pad(4);
                    }
                });
            }

            // 模块提示
            table.row();
            table.add("Modules: " + countModules() + " / " + (levels.size - 1))
                    .style(Styles.outlineLabel).padTop(4);
        }

        public void selectLevelRecipe(int levelIdx, int recipeIdx) {
            if (levelIdx < 0 || levelIdx >= levels.size) return;
            if (recipeIdx < 0 || recipeIdx >= levels.get(levelIdx).recipes.size) return;
            if (levelIdx > maxAvailableLevel()) return;
            int data = (levelIdx << 8) | recipeIdx;
            // 159 的 tileConfig 接受 Building
            Call.tileConfig(player, this, data);
        }

        @Override
        public void configureTile(Unit player, Object value) {
            if (value instanceof Integer data) {
                int levelIdx = (data >> 8) & 0xFF;
                int recipeIdx = data & 0xFF;
                if (levelIdx >= 0 && levelIdx < levels.size) {
                    if (levelIdx > maxAvailableLevel()) return;
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

        // ---------- 绘制 ----------
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
                float barW = size * tilesize - 8;
                float barH = 4;
                Draw.color(Color.gray);
                Fill.rect(x, y + size * tilesize / 2f - 6, barW, barH);
                Draw.color(Color.green);
                Fill.rect(x - barW/2f + f * barW/2f, y + size * tilesize / 2f - 6, f * barW, barH);
                Draw.color();
            }
        }

        // ---------- 序列化 ----------
        @Override
        public void write(Writes write) {
            super.write(write);
            write.b(currentLevel);
            write.b(currentRecipe);
            write.f(progress);
            write.bool(crafting);
            write.i(storedPayloads.size);
            for (Payload p : storedPayloads) {
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
            storedPayloads.clear();
            int count = read.i();
            for (int i = 0; i < count; i++) {
                storedPayloads.add(TypeIO.readPayload(read));
            }
        }
    }

    // ========== 内部数据结构 ==========
    public static class UnitRecipe {
        public UnitType unit;
        public float craftTime;
        public PayloadStack[] payloadCost;
        public int outputCount = 1;
    }

    public static class AssemblerLevel {
        public Seq<UnitRecipe> recipes = new Seq<>();
    }

    public static class PayloadStack {
        public Block block;      // 若需要方块载荷
        public UnitType unit;    // 若需要单位载荷
        public int amount;

        public PayloadStack(Block block, int amount) {
            this.block = block;
            this.amount = amount;
        }

        public PayloadStack(UnitType unit, int amount) {
            this.unit = unit;
            this.amount = amount;
        }
    }
}