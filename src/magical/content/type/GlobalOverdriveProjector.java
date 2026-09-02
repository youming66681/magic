package magical.content;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.OverdriveProjector;

import static mindustry.Vars.*;

public class GlobalOverdriveProjector extends OverdriveProjector{
    public GlobalOverdriveProjector(String name){
        super(name);
    }

    @Override
    public class OverdriveBuild extends OverdriveProjector.OverdriveBuild{
        @Override
        public void updateTile(){
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            heat = Mathf.lerpDelta(heat, efficiency > 0 ? 1f : 0f, 0.08f);
            charge += heat * Time.delta;
            if(hasBoost){
                phaseHeat = Mathf.lerpDelta(phaseHeat, optionalEfficiency, 0.1f);
            }
            if(charge >= reload){
                charge = 0f;
                float boost = realBoost();
                Groups.build.each(other -> {
                    if(other != null && other.isAdded() && other.team == team && other.block.canOverdrive){
                        other.applyBoost(boost, reload + 1f);
                    }
                });
            }
            if(efficiency > 0){
                useProgress += delta();
            }
            if(useProgress >= useTime){
                consume();
                useProgress %= useTime;
            }
        }
        @Override
        public void drawSelect(){
            Drawf.dashCircle(x, y, 128f, baseColor);
        }
    }
}