package magical.content;

import arc.graphics.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class ExplosiveGenerator extends ConsumeGenerator {
    public Item fuelItem = MLItems.荧羽石;
    public Liquid coolantLiquid = MLLiquids.幻钢溶液;
    public Liquid waterLiquid = Liquids.water;
    public float explosionRadius = 4 * 8f;
    public float explosionDamage = 100f;
    public Effect explodeEffect = Fx.explosion;

    public ExplosiveGenerator(String name) {
        super(name);
        hasItems = true;
        hasLiquids = true;
        hasPower = true;
        outputsPower = true;
        destructible = true;
        powerProduction = 200f;
    }

    @Override
    public void init() {
        super.init();
        consumeItem(fuelItem, itemAmount);
        consumeLiquid(coolantLiquid, coolantAmount);
        consumeLiquid(waterLiquid, waterAmount);
    }

    public class ExplosiveGeneratorBuild extends ConsumeGeneratorBuild {

        @Override
        public void updateTile() {
            super.updateTile();
            if (!enabled || net.client()) return;
            boolean canRun = consItem.efficiency(this) > 0.001f && consLiquids(coolantLiquid).efficiency(this) > 0.001f;
            boolean waterLacking = liquids.get(waterLiquid) < waterAmount * efficiencyScale();
            if (canRun && waterLacking) {
                explode();
            }
        }

        private void explode() {
            kill();
            explodeEffect.at(x, y);
            Damage.damage(team, x, y, explosionRadius, explosionDamage, false);
        }

        private ConsumeLiquid consLiquids(Liquid liquid) {
            for (Consume c : consumers) {
                if (c instanceof ConsumeLiquid cl && cl.liquid == liquid) {
                    return cl;
                }
            }
            return null;
        }
    }
}