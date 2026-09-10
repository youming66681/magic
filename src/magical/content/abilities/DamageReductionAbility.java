package magical.content;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
public class DamageReductionAbility extends Ability{
    public float reduction;
    public DamageReductionAbility(float reduction){
        this.reduction = Math.max(0f, Math.min(reduction, 0.9999f));
    }
    public float modifyDamage(Unit unit, float amount){
        return amount * (1f - reduction);
    }
    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(abilityStat("damagereduction", Strings.autoFixed(reduction * 100f, 1)));
    }
}