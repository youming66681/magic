
package magical.content;
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
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.effect.ParticleEffect;
import mindustry.entities.effect.WaveEffect;
import mindustry.gen.*;
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
    public Effect reactorEffect = new Effect(45f, e -> {
        Draw.color(Color.valueOf("FEEBB3FF"), Color.valueOf("FF5A20FF"), e.fin());
        Lines.stroke(3f * e.fout());
        Lines.circle(e.x, e.y, 8f + e.fin() * 28f);
        Draw.color(Color.valueOf("FEEBB3FF"));
        Fill.circle(e.x, e.y, 4f * e.fout());
        Draw.reset();
    });
    public NanoCrystalFusionReactor(String name) {
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
        powerProduction = 12800f / 60f;
        squareSprite = false;
        buildType = NanoCrystalFusionReactorBuild::new;
        destroyBullet = new ExplosionBulletType(800, 800) {{
            hitSound = MLSounds.laserblast;
            hitShake = 80f;
            hitEffect = new MultiEffect(
                    new WaveEffect() {{
                        lifetime = 40f;
                        sizeFrom = 800f;
                        sizeTo = 0f;
                        strokeFrom = 40;
                        strokeTo = 0;
                    }},
                    new WaveEffect() {{
                        lifetime = 30f;
                        sizeFrom = 800f;
                        sizeTo = 0f;
                        strokeFrom = 40;
                        strokeTo = 0;
                        startDelay = 10;
                    }},
                    new WaveEffect() {{
                        lifetime = 20f;
                        sizeFrom = 800f;
                        sizeTo = 0f;
                        strokeFrom = 40;
                        strokeTo = 0;
                        startDelay = 20;
                    }}
            );
            fragBullets = 1;
            fragBullet = new BasicBulletType() {{
                hittable = false;
                absorbable = false;
                collides = false;
                lifetime = 40f;
                height = 0;
                width = 0;
                speed = 0;
                fragBullets = 1;
                fragBullet = new BasicBulletType() {{
                    hittable = false;
                    absorbable = false;
                    collides = false;
                    lifetime = 0f;
                    height = 0;
                    width = 0;
                    speed = 0;
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
                    hitSoundVolume = 100;
                    hitShake = 800f;
                    hitEffect = new MultiEffect(
                            new WaveEffect() {{
                                lifetime = 60f;
                                sizeFrom = 0f;
                                sizeTo = 960f;
                                strokeFrom = 0f;
                                strokeTo = 16f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }},
                            new ParticleEffect() {{
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
                            new ParticleEffect() {{
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
                            new ParticleEffect() {{
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
                            new ParticleEffect() {{
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
        public float effectTimer;
        public float effectWarmup;
        public boolean running;
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
            if(heat > 0f && liquids.get(Liquids.cryofluid) > 0f){
                float used = Math.min(liquids.get(Liquids.cryofluid), coolantUse * delta);
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
            effectWarmup = Mathf.approachDelta(effectWarmup, productionEfficiency, 0.025f, delta);
            if(effectWarmup > 0.05f){
                effectTimer += delta * effectWarmup;
                if(effectTimer >= 18f){
                    effectTimer = 0f;
                    if(Vars.net.server()){
                        Call.effect(
                                reactorEffect,
                                x + Mathf.range(22f),
                                y + Mathf.range(22f),
                                0f,
                                Color.white
                        );
                    }
                }
            }else{
                effectTimer = 0f;
            }
        }
        @Override
        public void draw(){
            super.draw();
            float warmup = effectWarmup;
            float temperature = Mathf.clamp(heat);
            Color coreColor = Color.valueOf("FEEBB3FF").lerp(Color.valueOf("FF5A20FF"), temperature);
            if(warmup > 0.01f){
                Draw.z(0.5f);
                Draw.color(coreColor);
                Draw.alpha(warmup * 0.25f);
                Fill.circle(x, y, 38f);
                Draw.alpha(warmup * 0.8f);
                Lines.stroke(2f * warmup);
                Lines.circle(x, y, 32f + Mathf.absin(Time.time, 5f, 3f));
                Draw.color(Color.valueOf("FEEBB3FF"));
                Lines.stroke(1.5f * warmup);
                Draw.arc(x, y, 43f, 0.65f, Time.time * 2f);
                Draw.arc(x, y, 43f, 0.65f, -Time.time * 2f);
                Draw.color(coreColor);
                Draw.alpha(warmup * (0.55f + Mathf.absin(Time.time, 3f, 0.3f)));
                Fill.circle(x, y, 17f + Mathf.absin(Time.time, 4f, 3f));
                Draw.color(Color.white);
                Draw.alpha(warmup * 0.7f);
                Fill.circle(x, y, 7f);
                Draw.reset();
            }
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
        }
        @Override
        public byte version(){
            return 1;
        }
    }
}
