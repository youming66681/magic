package magical.content;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Interp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Liquids;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.effect.ParticleEffect;
import mindustry.entities.effect.WaveEffect;
import mindustry.gen.*;
import mindustry.world.blocks.power.PowerGenerator;
import magical.content.MLItems;
import magical.content.MLSounds;
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
        powerProduction = 12800f / 60f;
        squareSprite = false;
        buildType = NanoCrystalFusionReactorBuild::new;
        explosionRadius = 100;
        explosionDamage = 80000f;
        explosionShake = 80f;
        explosionShakeDuration = 30f;
        explodeSound = MLSounds.explosionbig;
        explodeEffect = new MultiEffect(
                new WaveEffect(){{
                    lifetime = 60f;
                    sizeFrom = 0f;
                    sizeTo = 960f;
                    strokeFrom = 0f;
                    strokeTo = 16f;
                    colorFrom = Color.valueOf("FEEBB3FF");
                    colorTo = Color.valueOf("FEEBB3FF");
                }},
                new WaveEffect(){{
                    lifetime = 40f;
                    sizeFrom = 800f;
                    sizeTo = 0f;
                    strokeFrom = 40f;
                    strokeTo = 0f;
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
                }}
        );
    }
    public class NanoCrystalFusionReactorBuild extends GeneratorBuild{
        public float heat;
        public float cycleProgress;
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
            }
        }
        @Override
        public void draw(){
            super.draw();
            Draw.color(Color.valueOf("FEEBB3FF"), Color.red, heat);
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