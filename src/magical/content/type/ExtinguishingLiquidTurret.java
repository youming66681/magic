package magical.content;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import mindustry.Vars;
import mindustry.entities.Fires;
import mindustry.type.Liquid;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.ContinuousLiquidTurret;
public class ExtinguishingLiquidTurret extends ContinuousLiquidTurret{
    public float extinguishInterval = 5f;
    public float puddleSize = 6f;
    public ExtinguishingLiquidTurret(String name){
        super(name);
        targetGround = true;
        targetAir = false;
    }
    public boolean canExtinguish(Liquid liquid){
        return liquid != null && liquid.temperature <= 0.5f && liquid.flammability < 0.3f;
    }
    public float getExtinguishIntensity(){
        return 400f * puddleSize / 6f;
    }
    public class ExtinguishingLiquidTurretBuild extends ContinuousLiquidTurretBuild{
        private float extinguishTimer;
        @Override
        public void updateTile(){
            super.updateTile();
            if(!enabled || efficiency <= 0f || liquids.current() == null) return;
            Liquid liquid = liquids.current();
            if(!canExtinguish(liquid)) return;
            extinguishTimer += delta();
            if(extinguishTimer < extinguishInterval) return;
            extinguishTimer = 0f;
            float beamLength = Math.max(0f, lastLength);
            float range2 = beamLength * beamLength;
            int rangeTiles = Mathf.ceil(beamLength / Vars.tilesize);
            for(int tx = tile.x - rangeTiles; tx <= tile.x + rangeTiles; tx++){
                for(int ty = tile.y - rangeTiles; ty <= tile.y + rangeTiles; ty++){
                    Tile target = Vars.world.tile(tx, ty);
                    if(target == null || !Fires.has(tx, ty)) continue;
                    if(Mathf.dst2(x, y, target.worldx(), target.worldy()) > range2) continue;
                    Fires.extinguish(target, getExtinguishIntensity());
                    for(Point2 p : Geometry.d4){
                        Tile nearby = Vars.world.tile(target.x + p.x, target.y + p.y);
                        if(nearby != null && Fires.has(nearby.x, nearby.y)){
                            if(Mathf.dst2(x, y, nearby.worldx(), nearby.worldy()) <= range2){
                                Fires.extinguish(nearby, getExtinguishIntensity());
                            }
                        }
                    }
                }
            }
        }
    }
}