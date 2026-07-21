package magical.content; // 改成你的包名

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
import mindustry.world.blocks.units.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

/**
 * 灵活装配机 – 继承自 UnitAssembler，实现多等级多配方 + 模块解锁
 * 需要 Mindustry v8
 */
public class FlexAssembler extends UnitAssembler {

    // 原有纹理（原版 UnitAssembler 也可能有，这里保留）
    public @Load("@-side1") TextureRegion sideRegion1;
    public @Load("@-side2") TextureRegion sideRegion2;

    public int areaSize = 11;
    public Sound createSound = Sounds.unitBuild;
    public float createSoundVolume = 1f;

    /** 用于解锁等级的模块方块（如 UnitAssemblerModule） */
    public Block moduleBlock = null;

    /** 自定义装配等级列表（替代原版 plans） */
    public Seq<AssemblerLevel> levels = new Seq<>();

    public FlexAssembler(String name) {
        super(name);
        // 关闭物品消耗（我们纯用载荷）
        hasItems = false;
        // 其他属性沿用父类
    }

    // ========== 建筑内部类 ==========
    public class FlexAssemblerBuild extends UnitBuild {   // 继承原版 UnitBuild (UnitAssembler 的内部建筑)

        public int currentLevel = 0;
        public int currentRecipe = 0;
        public boolean crafting = false;
        public float progress = 0f;

        // ----- 模块检测 -----
        public int countModules() {
            if (moduleBlock == null) return levels.size - 1;
            int c = 0;
            int off = (areaSize - 1) / 2;
            for (int dx = -off; dx <= off; dx++)
                for (int dy = -off; dy <= off; dy++) {
                    Building b = world.buildWorld(tile.x + dx, tile.y + dy);
                    if (b != null && b.block == moduleBlock) c++;
                }
            return Math.min(c, levels.size - 1);
        }

        public int maxAvailableLevel() { return countModules(); }

        // ----- 载荷统计与消耗 -----
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
            // 模块变化时自动降级
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
                    if (have < stack.amount) {
                        can = false;
                        break;
                    }
                }
                if (can) {
                    // 消耗载荷
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
                progress += Time.delta * 60f * speed;   // delta秒转tick

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

        // ----- 玩家界面（等级切换 + 单位选择） -----
        @Override
        public void buildConfiguration(Table table) {
            int maxLevel = maxAvailableLevel();

            // 左右箭头切换等级
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

            // 当前等级的单位图标
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

            // 模块数量提示
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

            // 单位图标
            Draw.color(team.color, 0.8f);
            Draw.rect(recipe.unit.uiIcon, x, y, 8, 8);
            Draw.color();

            // 进度条
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

        // ----- 序列化（保留进度、等级、配方、载荷） -----
        @Override
        public void write(Writes write) {
            super.write(write);
            write.b(currentLevel);
            write.b(currentRecipe);
            write.f(progress);
            write.bool(crafting);
            // 载荷由父类 Building 自动保存，无需重复
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            currentLevel = read.ub();
            currentRecipe = read.ub();
            progress = read.f();
            crafting = read.bool();
        }
    }

    // ========== 数据结构 ==========
    public static class UnitRecipe {
        public UnitType unit;
        public float craftTime;           // tick
        public PayloadStack[] payloadCost;
        public int outputCount = 1;

        public UnitRecipe() {}
        public UnitRecipe(UnitType unit, float craftTime, PayloadStack[] cost) {
            this.unit = unit;
            this.craftTime = craftTime;
            this.payloadCost = cost;
        }
    }

    public static class AssemblerLevel {
        public Seq<UnitRecipe> recipes = new Seq<>();
    }

    public static class PayloadStack {
        public Block block;
        public UnitType unit;
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