package magical.content;

import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.ai.types.AIController;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;

public class TargetTeleportAbility extends Ability{
    public Effect teleportEffect;

    public TargetTeleportAbility(Effect teleportEffect){
        this.teleportEffect = teleportEffect;
    }

    @Override
    public void update(Unit unit){
        if(!(unit.controller() instanceof AIController ai))return;

        Teamc target = ai.target;

        if(!(target instanceof Unit enemy))return;
        if(!enemy.isValid())return;
        if(enemy.team == unit.team)return;

        float selfRange = unit.type.range;

        if(unit.within(enemy.x, enemy.y, selfRange))return;

        if(enemy.type.range <= selfRange)return;

        unit.set(enemy.x, enemy.y);

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