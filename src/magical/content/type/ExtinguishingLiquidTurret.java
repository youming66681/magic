package magical.content;

import arc.math.Mathf;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.entities.Fires;
import mindustry.type.Liquid;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.ContinuousLiquidTurret;

public class ExtinguishingLiquidTurret extends ContinuousLiquidTurret{
    public ObjectMap<Liquid, Float> extinguishIntensities = new ObjectMap<>();
    public float extinguishInterval = 10f;
    public ExtinguishingLiquidTurret(String name){
        super(name);
        targetGround = true;
        targetAir = false;
    }
    public ExtinguishingLiquidTurret extinguishIntensity(Liquid liquid, float intensity){
        extinguishIntensities.put(liquid, intensity);
        return this;
    }
    public float getExtinguishIntensity(Liquid liquid){
        return extinguishIntensities.get(liquid, 0f);
    }
    public class ExtinguishingLiquidTurretBuild extends ContinuousLiquidTurretBuild{
        private float extinguishTimer;
        @Override
        public void updateTile(){
            super.updateTile();
            if(!enabled || efficiency <= 0f || liquids.current() == null) return;
            Liquid liquid = liquids.current();
            if(!liquid.canExtinguish()) return;
            float intensity = getExtinguishIntensity(liquid);
            if(intensity <= 0f) return;
            extinguishTimer += delta();
            if(extinguishTimer < extinguishInterval) return;
            extinguishTimer = 0f;
            float range2 = range * range;
            int rangeTiles = Mathf.ceil(range / Vars.tilesize);
            int centerX = tile.x;
            int centerY = tile.y;
            Tile nearest = null;
            float nearestDst = Float.MAX_VALUE;
            for(int tx = centerX - rangeTiles; tx <= centerX + rangeTiles; tx++){
                for(int ty = centerY - rangeTiles; ty <= centerY + rangeTiles; ty++){
                    Tile target = Vars.world.tile(tx, ty);
                    if(target == null || !Fires.has(tx, ty)) continue;
                    float dst = Mathf.dst2(x, y, target.worldx(), target.worldy());
                    if(dst > range2 || dst >= nearestDst) continue;
                    nearestDst = dst;
                    nearest = target;
                }
            }
            if(nearest == null) return;
            Fires.extinguish(nearest, intensity);
            extinguishNearby(nearest, intensity, range2);
        }
        private void extinguishNearby(Tile center, float intensity, float range2){
            int[][] offsets = {{1,0},{-1,0},{0,1},{0,-1}};
            for(int[] offset : offsets){
                Tile nearby = Vars.world.tile(center.x + offset[0], center.y + offset[1]);
                if(nearby == null || !Fires.has(nearby.x, nearby.y)) continue;
                if(Mathf.dst2(x, y, nearby.worldx(), nearby.worldy()) > range2) continue;
                Fires.extinguish(nearby, intensity);
            }
        }
    }
}