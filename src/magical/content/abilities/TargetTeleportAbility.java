package magical.content;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.gen.Vars;

public class TargetTeleportAbility extends Ability{
    public float interval;
    public Effect teleportEffect;
    private float timer;

    public TargetTeleportAbility(float interval, Effect teleportEffect){
        this.interval = interval;
        this.teleportEffect = teleportEffect;
    }

    @Override
    public void update(Unit unit){
        timer += Time.delta;
        if(timer < interval)return;
        timer = 0f;

        Unit target = null;
        float closest = Float.MAX_VALUE;

        float selfRange = unit.type.range;

        for(Unit other : Groups.unit){
            if(other.team == unit.team)continue;
            if(!other.isValid())continue;

            float distance = unit.dst(other);

            float targetRange = other.type.range;

            boolean near = distance <= selfRange / 2f;

            boolean higherRange = targetRange > selfRange;

            if(near || higherRange){
                if(distance < closest){
                    closest = distance;
                    target = other;
                }
            }
        }

        if(target != null){

            unit.set(target.x, target.y);

            if(teleportEffect != null){
                teleportEffect.at(unit.x, unit.y);
            }
        }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(abilityStat("interval", interval / 60f));
    }

    @Override
    public String getBundle(){
        return "ability.targetteleport";
    }
}