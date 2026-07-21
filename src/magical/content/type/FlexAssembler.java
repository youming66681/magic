package your.mod.package; // 改为你的包名

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
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
 * 单位装配机 – 多等级、多配方、载荷消耗、模块解锁
 * 适用于 Mindustry v7 (build 146~159)
 */
public class UnitAssembler extends Block {

    // 保留原有的纹理注解（159 完全支持）
    public @Load("@-side1") TextureRegion sideRegion1;
    public @Load("@-side2") TextureRegion sideRegion2;

    // 基础属性
    public int areaSize = 11;                // 检测模块范围
    public Sound createSound = Sounds.unitCreateBig;
    public float createSoundVolume = 1f;

    // 用于解锁等级的模块方块（在 Mod 主类中赋值）
    public Block moduleBlock = null;

    // 装配等级列表
    public Seq<AssemblerLevel> levels = new Seq<>();

    public UnitAssembler(String name) {
        super(name);
        update = true;
        solid = true;
        hasItems = false;
        hasPower = true;
        configurable = true;
        payloadCapacity = 8;                 // 载荷槽位数量
        size = 3;
    }

    // ========== 内部建筑类 ==========
    public class UnitAssemblerBuild extends Building implements PayloadAcceptor<Building> {

        public int currentLevel = 0;        // 当前等级索引
        public int currentRecipe = 0;       // 当前配方索引
        public boolean crafting = false;
        public float progress = 0f;         // 进度（tick）

        // 自定义载荷仓库（必须手动同步）
        public Seq<Payload> storedPayloads = new Seq<>();

        /** 计算周围 areaSize 范围内的模块数量 */
        public int countModules() {
            if (moduleBlock == null) return levels.size - 1; // 无限制
            int count = 0;
            int offset = (areaSize - 1) / 2;
            for (int dx = -offset; dx <= offset; dx++) {
                for (int dy = -offset; dy <= offset; dy++) {
                    // 修正：使用 world.buildWorld 获取建筑
                    Building b = world.buildWorld(tile.x + dx, tile.y + dy);
                    if (b != null && b.block == moduleBlock) count++;
                }
            }
            return Math.min(count, levels.size - 1);
        }

        /** 当前可以使用的最高等级索引 */
        public int maxAvailableLevel() {
            return countModules();
        }

        // ---------- 载荷接口 ----------
        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            // 只要任何一个配方需要这种载荷，就允许接收
            for (AssemblerLevel level : levels) {
                for (UnitRecipe recipe : level.recipes) {
                    for (PayloadStack stack : recipe.payloadCost) {
                        if (stack.block != null && payload.block() == stack.block) return true;
                        if (stack.unit != null && payload.unit() != null
                                && payload.unit().type == stack.unit) return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            storedPayloads.add(payload);
            payload.set(null, null);   // 断开原有连接
        }

        @Override
        public Seq<Payload> getPayloads() {
            return storedPayloads;
        }

        public int countPayload(Block block) {
            int c = 0;
            for (Payload p : storedPayloads) { if (p.block() == block) c++; }
            return c;
        }

        public int countPayload(UnitType unit) {
            int c = 0;
            for (Payload p : storedPayloads) {
                if (p.unit() != null && p.unit().type == unit) c++;
            }
            return c;
        }

        public void removePayload(Block block, int amount) {
            int removed = 0;
            for (int i = storedPayloads.size - 1; i >= 0 && removed < amount; i--) {
                Payload p = storedPayloads.get(i);
                if (p.block() == block) {
                    p.remove();
                    storedPayloads.remove(i);
                    removed++;
                }
            }
        }

        public void removePayload(UnitType unit, int amount) {
            int removed = 0;
            for (int i = storedPayloads.size - 1; i >= 0 && removed < amount; i--) {
                Payload p = storedPayloads.get(i);
                if (p.unit() != null && p.unit().type == unit) {
                    p.remove();
                    storedPayloads.remove(i);
                    removed++;
                }
            }
        }

        // ---------- 生产逻辑 ----------
        @Override
        public void updateTile() {
            // 根据模块动态调整等级上限
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
                float speed = 1f;                         // 可加入超速/电力影响
                progress += Time.delta * 60f * speed;     // delta 秒 → tick

                if (progress >= recipe.craftTime) {
                    crafting = false;
                    if (!net.client()) {
                        for (int i = 0; i < recipe.outputCount; i++) {
                            Unit unit = recipe.unit.spawn(team, tile.worldx(), tile.worldy());
                            unit.add();
                        }
                    }
                    createSound.at(x, y, createSoundVolume);
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

        // ---------- 配置界面 ----------
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

            // 当前等级的单位图标
            AssemblerLevel level = levels.get(currentLevel);
            if (level != null) {
                table.table(icons -> {
                    for (int i = 0; i < level.recipes.size; i++) {
                        final int idx = i;
                        UnitRecipe recipe = level.recipes.get(i);
                        TextureRegion icon = recipe.unit.icon(Cicon.medium);
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
            Call.tileConfig(player, tile, data);
        }

        @Override
        public void configureTile(@Nullable Unit player, Object value) {
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
            Draw.rect(recipe.unit.icon(Cicon.medium), x, y, 8, 8);
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
            for (Payload p : storedPayloads) TypeIO.writePayload(write, p);
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
            for (int i = 0; i < count; i++) storedPayloads.add(TypeIO.readPayload(read));
        }
    }

    // ========== 数据结构 ==========
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
        public Block block;
        public UnitType unit;
        public int amount;

        public PayloadStack(Block block, int amount) { this.block = block; this.amount = amount; }
        public PayloadStack(UnitType unit, int amount) { this.unit = unit; this.amount = amount; }
    }
}