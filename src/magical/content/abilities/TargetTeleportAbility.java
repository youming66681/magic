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
            Class<?> cls = unit.controller().getClass();

            while(cls != null){
                try{
                    var field = cls.getDeclaredField("target");
                    field.setAccessible(true);

                    Object obj = field.get(unit.controller());

                    if(obj instanceof Unit u){
                        return u;
                    }

                    break;
                }catch(NoSuchFieldException e){
                    cls = cls.getSuperclass();
                }
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

        float x = target.x;
        float y = target.y;

        unit.set(x,y);

        if(teleportEffect != null){
            teleportEffect.at(
                    x,
                    y,
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