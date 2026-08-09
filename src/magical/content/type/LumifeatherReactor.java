package magical.content;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;
import magical.content.*;

public class LumifeatherReactor extends NuclearReactor{

    public float explosionRadius = 25f;
    public float explosionDamage = 5000f;

    public LumifeatherReactor(String name){
        super(name);
        buildType = LumifeatherReactorBuild::new;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.input);
        stats.add(
                Stat.input,
                StatValues.items(new ItemStack(MLItems.fluorescentFeatherStone, 2))
        );
        stats.add(
                Stat.input,
                StatValues.liquids(
                        1f, new LiquidStack(MLLiquids.PhantomSteelSolution, 0.4f))
        );
        stats.add(
                Stat.input,
                StatValues.liquids(1f, new LiquidStack(Liquids.water, heating))
        );
    }
    public class LumifeatherReactorBuild extends NuclearReactorBuild{
        @Override
        public void updateTile(){
            super.updateTile();
            if(efficiency > 0f && liquids.get(Liquids.water) <= 0.001f){
                Fx.massiveExplosion.at(x,y);
                Damage.damage(x, y, explosionRadius, explosionDamage);
                kill();
            }
        }
    }
}