
package magical.content;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Liquids;
import mindustry.entities.Effect;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.effect.ParticleEffect;
import mindustry.entities.effect.WaveEffect;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.gen.Building;
import mindustry.world.blocks.power.PowerGenerator;
public class NanoCrystalFusionReactor extends PowerGenerator{
    public float itemDuration = 800f;
    public int nanoThreadUse = 4;
    public int crystallineCarbonUse = 4;
    public float heating = 0.0008f;
    public float coolantUse = 0.08f;
    public float coolantPower = 0.0125f;
    public float maxHeat = 1f;
    public float stopHeat = 0.9f;
    public float restartHeat = 0.6f;
    public float sequenceInterval = 240f;
    public Effect runtimeExplosionEffect = new MultiEffect(
            new WaveEffect(){{
                lifetime = 55f;
                sizeFrom = 0f;
                sizeTo = 560f;
                strokeFrom = 0f;
                strokeTo = 22f;
                colorFrom = Color.valueOf("FEEBB3FF");
                colorTo = Color.valueOf("FF8A35FF");
            }},
            new WaveEffect(){{
                lifetime = 40f;
                sizeFrom = 420f;
                sizeTo = 0f;
                strokeFrom = 28f;
                strokeTo = 0f;
                colorFrom = Color.valueOf("FFFFFFFF");
                colorTo = Color.valueOf("FEEBB3FF");
            }},
            new WaveEffect(){{
                lifetime = 65f;
                sizeFrom = 0f;
                sizeTo = 360f;
                strokeFrom = 0f;
                strokeTo = 8f;
                colorFrom = Color.valueOf("FF5A20FF");
                colorTo = Color.valueOf("FEEBB3FF");
            }},
            new ParticleEffect(){{
                particles = 100;
                sizeFrom = 22f;
                sizeTo = 0f;
                length = 420f;
                baseLength = 0f;
                interp = Interp.pow5Out;
                sizeInterp = Interp.pow5In;
                lifetime = 90f;
                colorFrom = Color.valueOf("FFFFFFFF");
                colorTo = Color.valueOf("FF5A20FF");
            }},
            new ParticleEffect(){{
                particles = 120;
                line = true;
                strokeFrom = 9f;
                strokeTo = 0f;
                lenFrom = 120f;
                lenTo = 0f;
                length = 520f;
                baseLength = 0f;
                interp = Interp.pow5Out;
                sizeInterp = Interp.pow5In;
                lifetime = 100f;
                colorFrom = Color.valueOf("FEEBB3FF");
                colorTo = Color.valueOf("FF5A20FF");
            }},
            new ParticleEffect(){{
                particles = 1;
                sizeFrom = 0f;
                sizeTo = 520f;
                length = 0f;
                baseLength = 0f;
                sizeInterp = Interp.pow5Out;
                lifetime = 45f;
                colorFrom = Color.valueOf("FFFFFFFF");
                colorTo = Color.valueOf("FEEBB3FF");
            }}
    );
    public NanoCrystalFusionReactor(String name){
        super(name);
        size = 8;
        health = 6400;
        armor = 8;
        itemCapacity = 40;
        liquidCapacity = 800f;
        hasItems = true;
        hasLiquids = true;
        hasPower = true;
        outputsPower = true;
        powerProduction = 12800f;
        squareSprite = false;
        buildType = NanoCrystalFusionReactorBuild::new;
        addBar("heat", (NanoCrystalFusionReactorBuild build) ->
                new Bar(
                        () -> Core.bundle.format("bar.heat", Mathf.round(build.heat / maxHeat * 100f)),
                        () -> Pal.lightOrange,
                        () -> Mathf.clamp(build.heat / maxHeat)
                )
        );
        addBar("cryofluid", (NanoCrystalFusionReactorBuild build) ->
                new Bar(
                        () -> Core.bundle.format("bar.cryofluid", Mathf.round(build.liquids.get(Liquids.cryofluid) * 10f) / 10f, liquidCapacity),
                        () -> Liquids.cryofluid.color,
                        () -> Mathf.clamp(build.liquids.get(Liquids.cryofluid) / liquidCapacity)
                )
        );
        destroyBullet = new ExplosionBulletType(800, 800){{
            hitSound = MLSounds.laserblast;
            hitShake = 80f;
            hitEffect = new MultiEffect(
                    new WaveEffect(){{
                        lifetime = 40f;
                        sizeFrom = 800f;
                        sizeTo = 0f;
                        strokeFrom = 40f;
                        strokeTo = 0f;
                    }},
                    new WaveEffect(){{
                        lifetime = 30f;
                        sizeFrom = 800f;
                        sizeTo = 0f;
                        strokeFrom = 40f;
                        strokeTo = 0f;
                        startDelay = 10f;
                    }},
                    new WaveEffect(){{
                        lifetime = 20f;
                        sizeFrom = 800f;
                        sizeTo = 0f;
                        strokeFrom = 40f;
                        strokeTo = 0f;
                        startDelay = 20f;
                    }}
            );
            fragBullets = 1;
            fragBullet = new BasicBulletType(){{
                hittable = false;
                absorbable = false;
                collides = false;
                lifetime = 40f;
                height = 0f;
                width = 0f;
                speed = 0f;
                fragBullets = 1;
                fragBullet = new BasicBulletType(){{
                    hittable = false;
                    absorbable = false;
                    collides = false;
                    lifetime = 0f;
                    height = 0f;
                    width = 0f;
                    speed = 0f;
                    splashDamage = 80000f;
                    splashDamageRadius = 800f;
                    makeFire = true;
                    lightning = 180;
                    lightningLength = 100;
                    lightningLengthRand = 100;
                    incendAmount = 100;
                    incendSpread = 100;
                    incendChance = 1f;
                    hitSound = MLSounds.explosionbig;
                    hitSoundVolume = 100f;
                    hitShake = 800f;
                    hitEffect = new MultiEffect(
                            new WaveEffect(){{
                                lifetime = 60f;
                                sizeFrom = 0f;
                                sizeTo = 960f;
                                strokeFrom = 0f;
                                strokeTo = 16f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }},
                            new ParticleEffect(){{
                                particles = 50;
                                sizeFrom = 50f;
                                sizeTo = 0f;
                                length = 640f;
                                baseLength = 0f;
                                interp = Interp.pow10Out;
                                sizeInterp = Interp.pow10In;
                                lifetime = 180f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }},
                            new ParticleEffect(){{
                                particles = 100;
                                line = true;
                                strokeFrom = 8f;
                                strokeTo = 0f;
                                interp = Interp.pow5Out;
                                sizeInterp = Interp.pow5In;
                                lenFrom = 100f;
                                lenTo = 0f;
                                length = 640f;
                                baseLength = 0f;
                                lifetime = 180f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }},
                            new ParticleEffect(){{
                                particles = 1;
                                sizeFrom = 0f;
                                sizeTo = 480f;
                                sizeInterp = Interp.pow5Out;
                                length = 0f;
                                baseLength = 0f;
                                lifetime = 60f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }},
                            new ParticleEffect(){{
                                particles = 1;
                                sizeFrom = 480f;
                                sizeTo = 0f;
                                sizeInterp = Interp.pow10In;
                                length = 0f;
                                baseLength = 0f;
                                startDelay = 60f;
                                lifetime = 120f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }}
                    );
                }};
            }};
        }};
    }
    public class NanoCrystalFusionReactorBuild extends GeneratorBuild{
        public float heat;
        public float cycleProgress;
        public float sequenceCooldown;
        public float sequenceProgress;
        public float effectWarmup;
        public boolean running;
        public boolean sequenceActive;
        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return liquid == Liquids.cryofluid && liquids.get(liquid) < liquidCapacity;
        }
        @Override
        public void updateTile(){
            float delta = delta();
            if(!running && enabled && heat < restartHeat && items.get(MLItems.nanoEnergyThread) >= nanoThreadUse && items.get(MLItems.crystallineCarbon) >= crystallineCarbonUse){
                items.remove(MLItems.nanoEnergyThread, nanoThreadUse);
                items.remove(MLItems.crystallineCarbon, crystallineCarbonUse);
                running = true;
                cycleProgress = 0f;
            }
            if(running){
                if(heat < stopHeat){
                    cycleProgress += delta;
                }
                heat += heating * delta;
                if(cycleProgress >= itemDuration){
                    cycleProgress = 0f;
                    running = false;
                }
            }else{
                heat = Math.max(0f, heat - 0.0002f * delta);
            }
            float cryofluid = liquids.get(Liquids.cryofluid);
            if(heat > 0f && cryofluid > 0f){
                float used = Math.min(cryofluid, coolantUse * delta);
                liquids.remove(Liquids.cryofluid, used);
                heat = Math.max(0f, heat - used * coolantPower);
            }
            heat = Math.min(heat, maxHeat);
            productionEfficiency = enabled && running && heat < stopHeat ? 1f : 0f;
            if(heat >= maxHeat){
                productionEfficiency = 0f;
                kill();
                return;
            }
            effectWarmup = Mathf.approach(effectWarmup, productionEfficiency, 0.025f * delta);
            if(sequenceActive){
                sequenceProgress += delta;
                if(sequenceProgress >= 72f){
                    sequenceActive = false;
                    sequenceProgress = 0f;
                    sequenceCooldown = 0f;
                    if(Vars.net.server()){
                        Call.effect(runtimeExplosionEffect, x, y, 0f, Color.white);
                    }
                }
            }else if(productionEfficiency > 0f){
                sequenceCooldown += delta;
                if(sequenceCooldown >= sequenceInterval){
                    sequenceActive = true;
                    sequenceProgress = 0f;
                }
            }else{
                sequenceCooldown = 0f;
            }
        }
        @Override
        public void draw(){
            super.draw();
            float warmup = effectWarmup;
            float temperature = Mathf.clamp(heat);
            Color coreColor = Color.valueOf("FEEBB3FF").lerp(Color.valueOf("FF5A20FF"), temperature);
            Draw.z(Layer.block + 0.1f);
            if(warmup > 0.01f){
                float radius = 25f;
                float pulse = Mathf.absin(Time.time, 3f, 2f);
                if(sequenceActive){
                    int stage = Math.min(2, (int)(sequenceProgress / 24f));
                    float phase = (sequenceProgress - stage * 24f) / 24f;
                    float startRadius = 38f - stage * 12f;
                    float contraction = phase < 0.65f ? phase / 0.65f : 1f - (phase - 0.65f) / 0.35f;
                    contraction = Interp.pow3In.apply(Mathf.clamp(contraction));
                    radius = Mathf.lerp(startRadius, 4f, contraction);
                    pulse = 0f;
                }
                Draw.color(coreColor);
                Draw.alpha(warmup * 0.22f);
                Fill.circle(x, y, radius + 12f);
                Draw.alpha(warmup * 0.8f);
                Lines.stroke(2.5f * warmup);
                Lines.circle(x, y, radius + pulse);
                Draw.color(Color.valueOf("FEEBB3FF"));
                Lines.stroke(1.5f * warmup);
                Lines.circle(x, y, radius + 8f + pulse);
                Draw.color(coreColor);
                Draw.alpha(warmup * 0.8f);
                Fill.circle(x, y, Math.max(2f, radius * 0.45f));
                Draw.color(Color.white);
                Draw.alpha(warmup * 0.75f);
                Fill.circle(x, y, Math.max(1.5f, radius * 0.18f));
                if(sequenceActive){
                    Draw.color(Color.white);
                    Draw.alpha(warmup * (0.35f + 0.65f * Mathf.absin(Time.time, 12f, 0.5f)));
                    Fill.circle(x, y, 5f + Mathf.absin(Time.time, 8f, 4f));
                }
            }
            Draw.z(Layer.block);
            Draw.color(Color.valueOf("FEEBB3FF"), Color.red, temperature);
            Draw.alpha(heat * 0.25f);
            Fill.rect(x, y, block.size * 8f, block.size * 8f);
            Draw.reset();
        }
        @Override
        public void write(Writes write){
            super.write(write);
            write.f(heat);
            write.f(cycleProgress);
            write.bool(running);
        }
        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            heat = read.f();
            cycleProgress = read.f();
            running = read.bool();
            sequenceCooldown = 0f;
            sequenceProgress = 0f;
            sequenceActive = false;
            effectWarmup = 0f;
        }
        @Override
        public byte version(){
            return 1;
        }
    }
}
