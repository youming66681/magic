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
    public Item fuelItem = MLItems.fluorescentFeatherStone;
    public Liquid coolantLiquid = MLLiquids.PhantomSteelSolution;
    public Liquid waterLiquid = Liquids.water;
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
        consume(new ConsumeItem(items -> fuelItem, itemAmount));
        consume(new ConsumeLiquid(coolantLiquid, coolantAmount));
        consume(new ConsumeCoolant(waterCoolantAmount, maxHeatEfficiency));

        super.init();
    }
    @Override
    public void setStats() {
        super.setStats();

        stats.remove(Stat.input);
        stats.remove(Stat.coolant);

        stats.add(Stat.input, table -> {
            table.row();
            table.table(Tex.pane, t -> {
                t.left().defaults().left();
                t.add(Core.bundle.format("stat.input")).left();
                t.row();
                t.add(StatValues.stack(new ItemStack(fuelItem, Math.round(itemAmount))));
                t.row();
                t.add(StatValues.liquid(coolantLiquid, coolantAmount * 60f, true));
            }).growX().pad(5).row();
        });

        stats.add(Stat.coolant, table -> {
            table.row();
            table.table(Tex.pane, t -> {
                t.left().defaults().left();
                t.add(Core.bundle.format("stat.coolant")).left();
                t.row();
                t.add(StatValues.liquid(waterLiquid, waterCoolantAmount * 60f, true));
            }).growX().pad(5).row();
        });
    }
    public class ExplosiveGeneratorBuild extends ConsumeGeneratorBuild {
        @Override
        public void updateTile() {
            super.updateTile();
            if (!enabled || net.client()) return;
            boolean hasItem = items.has(fuelItem, itemAmount);
            boolean hasCoolant = liquids.get(coolantLiquid) > 0.001f;
            if (hasItem && hasCoolant && liquids.get(waterLiquid) < 0.001f) {
                explode();
            }
        }

        private void explode() {
            kill();
            explodeEffect.at(x, y);
            Damage.damage(team, x, y, explosionRadius, explosionDamage, false);
        }
    }
}