package magical.content; // 改成你的包名

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
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
 * 灵活装配机 – 直接继承原版 UnitAssembler
 * 每个实例可独立配置多等级、多配方、模块解锁、载荷消耗
 */
public class FlexAssembler extends UnitAssembler {

    // 原版纹理（如果原版没有这些也可以留着，不会报错）
    public @Load("@-side1") TextureRegion sideRegion1;
    public @Load("@-side2") TextureRegion sideRegion2;

    // 自定义属性
    public int areaSize = 11;                // 模块检测范围
    public Sound createSound = Sounds.unitBuild;   // 使用原版音效
    public float createSoundVolume = 1f;

    /** 用于解锁等级的模块方块（如 UnitAssemblerModule） */
    public Block moduleBlock = null;

    /** 装配等级列表（由外部实例填充） */
    public Seq<AssemblerLevel> levels = new Seq<>();

    public FlexAssembler(String name) {
        super(name);
        // 关闭物品消耗（我们纯用载荷）
        hasItems = false;
    }

    @Override
    public void load() {
        super.load();
        // 如果某个实例忘记配置配方，给出警告
    }

    /**
     * 重写建筑工厂，返回我们自定义的建筑
     * 注意：原版 UnitAssembler 使用内部类 UnitAssemblerBuild，我们继承它
     */
    @Override
    public Building build(Tile tile, Team team, int rotation, boolean lastConfig) {
        return new FlexAssemblerBuild();
    }

    // ========== 自定义建筑（继承原版 UnitAssemblerBuild，保持所有原版特性） ==========
    public class FlexAssemblerBuild extends UnitAssemblerBuild {

        public int currentLevel = 0;
        public int currentRecipe = 0;
        public boolean crafting = false;
        public float progress = 0f;

        // 原版 UnitAssemblerBuild 内部已有 payloads 仓库，我们直接使用
        // 但为了安全，也可以使用自定义仓库，这里我们用父类自带的 payloads

        // ---- 模块计数 ----
        public int countModules() {
            if (moduleBlock == null) return levels.size - 1; // 无模块时解锁全部等级
            int c = 0;
            int off = (areaSize - 1) / 2;
            for (int dx = -off; dx <= off; dx++) {
                for (int dy = -off; dy <= off; dy++) {
                    Building b = world.buildWorld(tile.x + dx, tile.y + dy);
                    if (b != null && b.block == moduleBlock) c++;
                }
            }
            return Math.min(c, levels.size - 1);
        }

        public int maxAvailableLevel() {
            return countModules();
        }

        // ---- 载荷统计（使用父类 payloads 字段） ----
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

        // ---- 载荷消耗（从 payloads 中移除并销毁）----
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

        // ---- 生产逻辑 ----
        @Override
        public void updateTile() {
            // 模块数量变化时，自动降级
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
                    int have;
                    if (s.block != null) have = countPayload(s.block);
                    else have = countPayload(s.unit);
                    if (have < s.amount) {
                        can = false;
                        break;
                    }
                }

                if (can) {
                    // 消耗载荷
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
                    // 生成单位（只限服务端）
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

            // 千万要调用父类的 updateTile()，保留原版 UnitAssembler 的动画/电力/液体等逻辑
            super.updateTile();
        }

        public UnitRecipe getCurrentRecipe() {
            if (levels.isEmpty() || currentLevel >= levels.size) return null;
            AssemblerLevel lv = levels.get(currentLevel);
            if (lv == null || lv.recipes.isEmpty()) return null;
            if (currentRecipe >= lv.recipes.size) return null;
            return lv.recipes.get(currentRecipe);
        }

        // ---- 玩家配置界面（等级切换 + 单位选择）----
        @Override
        public void buildConfiguration(Table table) {
            int maxLevel = maxAvailableLevel();

            // 第一行：左右箭头切换等级
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

            // 第二行：当前等级的所有单位图标（可选择其中一个）
            AssemblerLevel lv = levels.get(currentLevel);
            if (lv != null) {
                table.table(icons -> {
                    for (int i = 0; i < lv.recipes.size; i++) {
                        final int idx = i;
                        UnitRecipe recipe = lv.recipes.get(i);
                        TextureRegion icon = recipe.unit.uiIcon;
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
            Call.tileConfig(player, this, data);
        }

        @Override
        public void configureTile(Unit player, Object value) {
            if (value instanceof Integer data) {
                int levelIdx = (data >> 8) & 0xFF;
                int recipeIdx = data & 0xFF;
                if (levelIdx >= 0 && levelIdx < levels.size && levelIdx <= maxAvailableLevel()) {
                    AssemblerLevel lv = levels.get(levelIdx);
                    if (recipeIdx >= 0 && recipeIdx < lv.recipes.size) {
                        currentLevel = levelIdx;
                        currentRecipe = recipeIdx;
                        crafting = false;
                        progress = 0f;
                    }
                }
            }
        }

        // ---- 自定义绘制（在原版绘制之上额外显示单位图标和进度条）----
        @Override
        public void draw() {
            super.draw();   // 原版绘制（包含动画、灯光等）
            UnitRecipe recipe = getCurrentRecipe();
            if (recipe == null) return;

            // 绘制当前选中的单位图标（淡色）
            Draw.color(team.color, 0.8f);
            Draw.rect(recipe.unit.uiIcon, x, y, 8, 8);
            Draw.color();

            // 生产进度条
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

        // ---- 序列化（保存等级、配方、进度、载荷）----
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

    // ========== 数据结构（不变）==========
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