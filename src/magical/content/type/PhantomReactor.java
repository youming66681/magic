package magical.content;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class PhantomReactor extends PowerGenerator {
    public final int timerFuel = timers++;

    public Color lightColor = Color.valueOf("7f19ea");
    public Color coolColor = new Color(1, 1, 1, 0f);
    public Color hotColor = Color.valueOf("ff9575a3");

    public float itemDuration = 120f;
    public float heating = 0.01f;
    public float heatOutput = 15f;
    public float ambientCooldownTime = 60f * 20f;
    public float smokeThreshold = 0.3f;
    public float flashThreshold = 0.46f;
    public float coolantPower = 0.5f;

    public Item fuelItem = MLItems.fluorescentFeatherStone;
    public Liquid fuelLiquid = MLLiquids.PhantomSteelSolution;
    public Liquid coolantLiquid = Liquids.water;

    public float fuelLiquidAmount = 0.5f;
    public float coolantAmount = 1f;

    public TextureRegion topRegion;
    public TextureRegion lightsRegion;

    public PhantomReactor(String name) {
        super(name);
        itemCapacity = 30;
        liquidCapacity = 60;
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
        stats.remove(Stat.input);
        stats.add(Stat.input, table -> {
            table.row();
            table.table(Tex.pane, t -> {
                t.left().defaults().left();
                t.add(Core.bundle.format("stat.input")).left().growX().row();
                t.add(StatValues.stack(new ItemStack(fuelItem, 1))).pad(5).row();
                StatValues.liquid(fuelLiquid, fuelLiquidAmount * 60f, true).display(t);
            }).growX().pad(5).row();
            table.table(Tex.pane, t -> {
                t.left().defaults().left();
                t.add(Core.bundle.format("stat.coolant")).left().growX().row();
                StatValues.liquid(coolantLiquid, coolantAmount * 60f, true).display(t);
            }).growX().pad(5).row();
        });
        if (hasItems) {
            stats.add(Stat.productionTime, itemDuration / 60f, StatUnit.seconds);
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("heat", (PhantomReactorBuild entity) -> new Bar("bar.heat", Pal.lightOrange, () -> entity.heat));
    }

    public class PhantomReactorBuild extends GeneratorBuild implements HeatBlock {
        public float heat;
        public float heatProgress;
        public float flash;
        public float smoothLight;

        @Override
        public void updateTile() {
            int fuel = items.get(fuelItem);
            float fullness = (float) fuel / itemCapacity;
            productionEfficiency = fullness;

            boolean hasFuelLiquid = liquids.get(fuelLiquid) >= 0.5f;
            boolean hasCoolant = liquids.get(coolantLiquid) >= 0.01f;

            if (fuel > 0 && hasFuelLiquid && enabled) {
                heat += fullness * heating * Math.min(delta(), 4f);

                if (timer(timerFuel, itemDuration / timeScale)) {
                    consume();
                }

                liquids.remove(fuelLiquid, Math.min(liquids.get(fuelLiquid), 0.5f * delta()));
            } else {
                productionEfficiency = 0f;
                heat = Math.max(0f, heat - Time.delta / ambientCooldownTime);
            }

            if (hasCoolant && heat > 0) {
                float maxUsed = Math.min(liquids.get(coolantLiquid), heat / coolantPower);
                heat -= maxUsed * coolantPower;
                liquids.remove(coolantLiquid, maxUsed);
            } else if (!hasCoolant && heat > 0.1f) {
                heat += heating * 1f * Math.min(delta(), 4f);
            }

            if (heat > smokeThreshold) {
                float smoke = 1.0f + (heat - smokeThreshold) / (1f - smokeThreshold);
                if (Mathf.chance(smoke / 20.0 * delta())) {
                    Fx.reactorsmoke.at(x + Mathf.range(size * tilesize / 2f),
                            y + Mathf.range(size * tilesize / 2f));
                }
            }

            heat = Mathf.clamp(heat);
            heatProgress = heatOutput > 0f ? Mathf.approachDelta(heatProgress, heat * heatOutput * (enabled ? 1f : 0f), 0.01f * delta()) : 0f;

            if (heat >= 0.999f) {
                kill();
                explodeEffect.at(x, y);
                explodeSound.at(x, y);
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