package magical.content;

import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.type.Weapon;

public class TargetTeleportAbility extends Ability{
    public Effect teleportEffect;
    public TargetTeleportAbility(Effect teleportEffect){
        this.teleportEffect = teleportEffect;
    }
    private Unit getTarget(Unit unit){
        try{
            Object controller = unit.controller();

            var field = controller.getClass().getSuperclass().getDeclaredField("target");
            field.setAccessible(true);

            Object target = field.get(controller);

            if(target instanceof Unit u){
                return u;
            }
        }catch(Exception ignored){
        }

        return null;
    }
    @Override
    public void update(Unit unit){
        Unit target = getTarget(unit);
        if(target == null)return;
        if(!target.isValid())return;
        if(target.team == unit.team)return;
        float selfRange = unit.type.range;
        if(unit.within(target.x,target.y,selfRange))return;
        if(target.type.range <= selfRange)return;
        unit.set(target.x,target.y);
            if(teleportEffect != null){
                teleportEffect.at(
                        unit.x,
                        unit.y,
                        unit.rotation,
                        unit.team.color,
                        unit.type
                );
            }
        }

    @Override
    public void addStats(Table t){
        super.addStats(t);
    }

    @Override
    public String getBundle(){
        return "ability.targetteleport";
    }
}