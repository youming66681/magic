package magical.content;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
public class PercentAreaDamageAbility extends Ability{
    public float range;
    public float percent;
    public Effect hitEffect;
    private float timer;
    public PercentAreaDamageAbility(float range,float percent,Effect hitEffect){
        this.range=range;
        this.percent=percent;
        this.hitEffect=hitEffect;
    }
    @Override
    public void update(Unit unit){
        timer+=Time.delta;
        if(timer<60f)return;
        timer=0f;
        Groups.unit.each(target->{
            if(target.team==unit.team)return;
            if(!target.within(unit.x,unit.y,range))return;
            float damage=target.health()*percent;
            target.damagePierce(damage);
            if(hitEffect!=null){
                hitEffect.at(target.x,target.y);
            }
        });
    }
    @Override
    public String getBundle(){
        return "ability.percentareadamage";
    }
    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(abilityStat("range",range));
        t.row();
        t.add(abilityStat("percent",(int)(percent*100)));
    }
}