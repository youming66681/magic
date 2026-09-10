package magical.content;

import arc.scene.ui.layout.Table;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.type.UnitType;

public class DamageReductionAbility extends Ability{

    public float reduction;

    private transient float lastHealth;

    public DamageReductionAbility(float reduction){
        this.reduction = Math.max(0f, Math.min(reduction, 0.99f));
    }

    @Override
    public void init(UnitType type){
    }

    @Override
    public void created(Unit unit){
        lastHealth = unit.health;
    }

    @Override
    public void update(Unit unit){

        if(unit.dead){
            return;
        }

        float current = unit.health;

        float damage = lastHealth - current;

        if(damage > 0){

            float restore = damage * reduction;

            unit.health = Math.min(
                    unit.maxHealth(),
                    unit.health + restore
            );
        }

        lastHealth = unit.health;
    }

    @Override
    public void addStats(Table t){
        t.add(abilityStat("damageReduction",(int)(reduction * 100)));
    }

}