package magical.content;
import arc.math.Mathf;
import mindustry.Vars;
import mindustry.entities.Fires;
import mindustry.type.Liquid;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.ContinuousLiquidTurret;
public class ExtinguishingLiquidTurret extends ContinuousLiquidTurret{
    public float extinguishInterval = 5f;
    public float baseExtinguishIntensity = 100f;
    public ExtinguishingLiquidTurret(String name){
        super(name);
        targetGround = true;
        targetAir = false;
    }
    public float getExtinguishIntensity(Liquid liquid){
        if(liquid == null) return 0f;
        float temperatureFactor = Mathf.clamp(1f - liquid.temperature);
        float flammabilityFactor = Mathf.clamp(1f - liquid.flammability);
        return baseExtinguishIntensity * temperatureFactor * flammabilityFactor;
    }
    public class ExtinguishingLiquidTurretBuild extends ContinuousLiquidTurretBuild{
        private float extinguishTimer;
        @Override
        public void updateTile(){
            super.updateTile();
            if(!enabled || efficiency <= 0f || liquids.current() == null) return;
            float intensity = getExtinguishIntensity(liquids.current());
            if(intensity <= 0f) return;
            extinguishTimer += delta();
            if(extinguishTimer < extinguishInterval) return;
            extinguishTimer = 0f;
            int rangeTiles = Mathf.ceil(range / Vars.tilesize);
            float range2 = range * range;
            int centerX = tile.x;
            int centerY = tile.y;
            for(int tx = centerX - rangeTiles; tx <= centerX + rangeTiles; tx++){
                for(int ty = centerY - rangeTiles; ty <= centerY + rangeTiles; ty++){
                    Tile target = Vars.world.tile(tx, ty);
                    if(target == null || !Fires.has(tx, ty)) continue;
                    if(Mathf.dst2(x, y, target.worldx(), target.worldy()) > range2) continue;
                    Fires.extinguish(target, intensity);
                }
            }
        }
    }
}