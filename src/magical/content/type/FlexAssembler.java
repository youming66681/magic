package magical.content;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
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

    public TextureRegion sideRegion1, sideRegion2;
    public int areaSize = 11;
    public Sound createSound = Sounds.place;
    public float createSoundVolume = 1f;

    /** 解锁模块 */
    public Block moduleBlock = null;

    /** 装配等级列表（实例自定义） */
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

    // 返回自定义建筑（关键：不重写 build，而是使用此方法，并确保建筑类可被实例化）
    // v7 中，方块会通过构造器反射创建建筑，只需保证内部类是 public static 或者在本类内部即可
    // 但实际上 UnitAssembler 使用内部类 UnitBuild，我们要替换它，所以必须重写 createBuilding 或 build
    // 这里采用最简单方式：直接在本类中覆写 build 方法，但签名必须匹配：
    @Override
    public Building build(Tile tile, Team team, int rotation, boolean lastConfig) {
        return new FlexAssemblerBuild();
    }

    // ========== 自定义建筑 ==========
    public class FlexAssemblerBuild extends UnitBuild {

        public int currentLevel = 0;
        public int currentRecipe = 0;
        public boolean crafting = false;
        public float progress = 0f;

        // 自管理的载荷仓库（用于统计和消耗）
        public Seq<Payload> storedPayloads = new Seq<>();

        // ---------- 模块计数 ----------
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

        // ---------- 载荷接收 ----------
        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            // 只要任一配方需要，就接收
            for (AssemblerLevel level : levels)
                for (UnitRecipe r : level.recipes)
                    for (PayloadStack s : r.payloadCost) {
                        if (s.block != null && payload instanceof BlockPayload
                                && ((BlockPayload) payload).block() == s.block) return true;
                        if (s.unit != null && payload instanceof UnitPayload
                                && ((UnitPayload) payload).unit.type == s.unit) return true;
                    }
            return false;
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            // 加入自己的仓库，并解除原绑定
            storedPayloads.add(payload);
            payload.set(null);
        }

        // ---------- 载荷统计 ----------
        public int countPayload(Block block) {
            int c = 0;
            for (Payload p : storedPayloads)
                if (p instanceof BlockPayload && ((BlockPayload) p).block() == block) c++;
            return c;
        }

        public int countPayload(UnitType unit) {
            int c = 0;
            for (Payload p : storedPayloads)
                if (p instanceof UnitPayload && ((UnitPayload) p).unit.type == unit) c++;
            return c;
        }

        // ---------- 载荷移除 ----------
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
                    if (have < s.amount) {
                        can = false;
                        break;
                    }
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
                progress += Time.delta * 60f;
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
            // 调用父类逻辑（电力等）
            super.updateTile();
        }

        public UnitRecipe getCurrentRecipe() {
            if (levels.isEmpty() || currentLevel >= levels.size) return null;
            AssemblerLevel level = levels.get(currentLevel);
            if (level == null || level.recipes.isEmpty()) return null;
            if (currentRecipe >= level.recipes.size) return null;
            return level.recipes.get(currentRecipe);
        }

        // ---------- UI ----------
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
            int count = read.i();
            storedPayloads.clear();
            for (int i = 0; i < count; i++) storedPayloads.add(TypeIO.readPayload(read));
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

        public PayloadStack(Block block, int amount) { this.block = block; this.amount = amount; }
        public PayloadStack(UnitType unit, int amount) { this.unit = unit; this.amount = amount; }
    }
}