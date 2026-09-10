package magical.content.abilities;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
public class DamageReductionAbility extends Ability{
    public float reduction;
    private float multiplier;
    public DamageReductionAbility(float reduction){
        this.reduction = Math.max(0f, Math.min(reduction, 0.9999f));
        this.multiplier = 1f / (1f - this.reduction);
    }
    @Override
    public void update(Unit unit){
        unit.healthMultiplier *= multiplier;
    }
    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(abilityStat("damagereduction", Strings.autoFixed(reduction * 100f, 1)));
    }
}
