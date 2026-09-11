package magical.content;

import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Groups;
import mindustry.gen.Unit;

public class TargetTeleportAbility extends Ability{
    public Effect teleportEffect;

    public TargetTeleportAbility(Effect teleportEffect){
        this.teleportEffect = teleportEffect;
    }

    @Override
    public void update(Unit unit){
        if(!(unit.target() instanceof Unit target)) return;
        if(!target.isValid()) return;
        if(target.team == unit.team) return;

        float selfRange = unit.type.range;

        if(unit.within(target.x, target.y, selfRange)){
            return;
        }

        if(target.type.range <= selfRange){
            return;
        }

        if(target != null){
            unit.set(target.x, target.y);

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