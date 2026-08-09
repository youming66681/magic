package magical.content.type;
import arc.Core;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;
import magical.content.*;
public class LumifeatherReactor extends NuclearReactor{
    public LumifeatherReactor(String name){
        super(name);
        buildType = LumifeatherReactorBuild::new;
    }
    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.input);
        stats.remove(Stat.coolant);
        stats.add(Stat.input, table -> {
            table.add(StatValues.items(new ItemStack[]{
                            new ItemStack(MLItems.fluorescentFeatherStone, 2)}
            ));
            table.row();
            table.add(StatValues.liquids(
                    new LiquidStack[]{new LiquidStack(MLLiquids.PhantomSteelSolution, 0.4f)}
            ));
        });
        stats.add(Stat.coolant, StatValues.liquids(
                new LiquidStack[]{new LiquidStack(Liquids.water, heating)}
        ));
    }
    public class LumifeatherReactorBuild extends NuclearReactorBuild{
        @Override
        public void updateTile(){
            super.updateTile();
            if(efficiency > 0f && liquids.get(Liquids.water) <= 0.001f){
                    MLFx.hugeEnergyExplosion.at(x,y);
                Damage.damage(x, y, explosionRadius * tilesize, explosionDamage);kill();
            }
        }
    }
}