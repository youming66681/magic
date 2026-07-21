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

public class FlexAssembler extends UnitAssembler {

    public TextureRegion sideRegion1;
    public TextureRegion sideRegion2;

    public int areaSize = 11;
    public Sound createSound = Sounds.unitBuild;
    public float createSoundVolume = 1f;

    /** 用于解锁等级的模块方块 */
    public Block moduleBlock = null;

    /** 自定义装配等级列表 */
    public Seq<AssemblerLevel> levels = new Seq<>();

    public FlexAssembler(String name) {
        super(name);
        hasItems = false;
    }

    @Override
    public void load() {
        super.load();
        sideRegion1 = Core.atlas.find(name + "-side1");
        sideRegion2 = Core.atlas.find(name + "-side2");
    }

    // 重写以返回我们的自定义建筑
    @Override
    public Building build(Tile tile, Team team, int rotation, boolean lastConfig) {
        return new FlexAssemblerBuild();
    }

    // ========== 自定义建筑 ==========
    public class FlexAssemblerBuild extends UnitAssemblerBuild {

        public int currentLevel = 0;
        public int currentRecipe = 0;
        public boolean crafting = false;
        public float progress = 0f;

        // 模块计数
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

        // 载荷统计（使用父类 payloads 字段）
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

        // 移除载荷
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

        @Override
        public void updateTile() {
            // 模块数量变化时自动降级
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
                for (PayloadStack s : recipe.payloadCost) {
                    int have = (s.block != null) ? countPayload(s.block) : countPayload(s.unit);
                    if (have < s.amount) { can = false; break; }
                }
                if (can) {
                    for (PayloadStack s : recipe.payloadCost) {
                        if (s.block != null) removePayload(s.block, s.amount);
                        else removePayload(s.unit, s.amount);
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

            super.updateTile(); // 保留原版一些逻辑（如电力消耗）
        }

        public UnitRecipe getCurrentRecipe() {
            if (levels.isEmpty() || currentLevel >= levels.size) return null;
            AssemblerLevel level = levels.get(currentLevel);
            if (level == null || level.recipes.isEmpty()) return null;
            if (currentRecipe >= level.recipes.size) return null;
            return level.recipes.get(currentRecipe);
        }

        // 配置界面
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

        // 绘制
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

        // 序列化
        @Override
        public void write(Writes write) {
            super.write(write);
            write.b(currentLevel);
            write.b(currentRecipe);
            write.f(progress);
            write.bool(crafting);
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
        public float craftTime;
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