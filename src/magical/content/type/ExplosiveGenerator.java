package magical.content;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class PhantomReactor extends PowerGenerator {
    public final int timerFuel = timers++;

    public Color lightColor = Color.valueOf("7f19ea");
    public Color coolColor = new Color(1, 1, 1, 0f);
    public Color hotColor = Color.valueOf("ff9575a3");

    /** 每消耗 1 个燃料的持续时间（tick） */
    public float itemDuration = 120f;
    /** 每帧热量增加系数 */
    public float heating = 0.01f;
    /** 最大热输出（用于进度条） */
    public float heatOutput = 15f;
    /** 无燃料时的自然冷却时间 */
    public float ambientCooldownTime = 60f * 20f;
    /** 冒烟阈值 */
    public float smokeThreshold = 0.3f;
    /** 闪烁阈值 */
    public float flashThreshold = 0.46f;
    /** 每单位冷却液移除的热量 */
    public float coolantPower = 0.5f;

    public Item fuelItem = MLItems.fluorescentFeatherStone;
    public Liquid fuelLiquid = MLLiquids.PhantomSteelSolution;
    public Liquid coolantLiquid = Liquids.water;

    public @Load("@-top") TextureRegion topRegion;
    public @Load("@-lights") TextureRegion lightsRegion;

    public PhantomReactor(String name) {
        super(name);
        itemCapacity = 30;
        liquidCapacity = 100;
        hasItems = true;
        hasLiquids = true;
        rebuildable = false;
        emitLight = true;
        flags = EnumSet.of(BlockFlag.reactor, BlockFlag.generator);
        schematicPriority = -5;
        envEnabled = Env.any;

        explosionShake = 6f;
        explosionShakeDuration = 16f;
        explosionRadius = 19;
        explosionDamage = 1250 * 4;
        explodeEffect = Fx.reactorExplosion;
        explodeSound = Sounds.explosionReactor;
    }

    @Override
    public void setStats() {
        super.setStats();
        if (hasItems) {
            stats.add(Stat.productionTime, itemDuration / 60f, StatUnit.seconds);
        }
        // 冷却液统计
        stats.add(Stat.coolant, StatValues.liquid(coolantLiquid, coolantPower * 60f, true));
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("heat", (PhantomReactorBuild entity) -> new Bar("bar.heat", Pal.lightOrange, () -> entity.heat));
    }

    public class PhantomReactorBuild extends GeneratorBuild {
        public float heat;
        public float heatProgress;
        public float flash;
        public float smoothLight;

        @Override
        public void updateTile() {
            // 1. 检查固体燃料（荧羽石）
            int fuel = items.get(fuelItem);
            float fullness = (float) fuel / itemCapacity;
            productionEfficiency = fullness;

            // 2. 检查液体燃料（幻钢溶液）和冷却水
            boolean hasFuelLiquid = liquids.get(fuelLiquid) >= 0.5f;   // 至少 0.5 单位才能工作
            boolean hasCoolant = liquids.get(coolantLiquid) >= 0.01f; // 只要有水就算有冷却

            if (fuel > 0 && hasFuelLiquid && enabled) {
                // 正常产热
                heat += fullness * heating * Math.min(delta(), 4f);

                // 消耗固体燃料（按时间）
                if (timer(timerFuel, itemDuration / timeScale)) {
                    consume(); // 消耗物品
                }

                // 消耗液体燃料（每帧消耗一点）
                liquids.remove(fuelLiquid, Math.min(liquids.get(fuelLiquid), 0.5f * delta()));
            } else {
                // 无燃料或中断 → 自然冷却
                productionEfficiency = 0f;
                heat = Math.max(0f, heat - Time.delta / ambientCooldownTime);
            }

            // 3. 冷却机制：有水时正常冷却，无水时热量暴增
            if (hasCoolant && heat > 0) {
                float maxUsed = Math.min(liquids.get(coolantLiquid), heat / coolantPower);
                heat -= maxUsed * coolantPower;
                liquids.remove(coolantLiquid, maxUsed);
            } else if (!hasCoolant && heat > 0.1f) {
                // 缺水惩罚：热量加速上升（5 倍速率）
                heat += heating * 5f * Math.min(delta(), 4f);
            }

            // 冒烟效果
            if (heat > smokeThreshold) {
                float smoke = 1.0f + (heat - smokeThreshold) / (1f - smokeThreshold);
                if (Mathf.chance(smoke / 20.0 * delta())) {
                    Fx.reactorsmoke.at(x + Mathf.range(size * tilesize / 2f),
                            y + Mathf.range(size * tilesize / 2f));
                }
            }

            heat = Mathf.clamp(heat);
            heatProgress = heatOutput > 0f ? Mathf.approachDelta(heatProgress, heat * heatOutput * (enabled ? 1f : 0f), 0.01f * delta()) : 0f;

            // 过热爆炸
            if (heat >= 0.999f) {
                Events.fire(Trigger.thoriumReactorOverheat);
                kill();
            }
        }

        @Override
        public float heatFrac() { return heatProgress / heatOutput; }
        @Override
        public float heat() { return heatProgress; }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.heat) return heat;
            return super.sense(sensor);
        }

        @Override
        public boolean shouldExplode() {
            return super.shouldExplode() && (items.get(fuelItem) >= 5 || heat >= 0.5f);
        }

        @Override
        public void drawLight() {
            float fract = productionEfficiency;
            smoothLight = Mathf.lerpDelta(smoothLight, fract, 0.08f);
            Drawf.light(x, y, (90f + Mathf.absin(5, 5f)) * smoothLight, Tmp.c1.set(lightColor).lerp(Color.scarlet, heat), 0.6f * smoothLight);
        }

        @Override
        public void draw() {
            super.draw();

            Draw.color(coolColor, hotColor, heat);
            Fill.rect(x, y, size * tilesize, size * tilesize);

            if (topRegion != null) {
                Draw.color(liquids.current().color);
                Draw.alpha(liquids.currentAmount() / liquidCapacity);
                Draw.rect(topRegion, x, y);
            }

            if (heat > flashThreshold && lightsRegion != null) {
                flash += (1f + ((heat - flashThreshold) / (1f - flashThreshold)) * 5.4f) * Time.delta;
                Draw.color(Color.red, Color.yellow, Mathf.absin(flash, 9f, 1f));
                Draw.alpha(0.3f);
                Draw.rect(lightsRegion, x, y);
            }

            Draw.reset();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(heat);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            heat = read.f();
        }
    }
}