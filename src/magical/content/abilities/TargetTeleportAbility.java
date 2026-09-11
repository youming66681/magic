package magical.content;

import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Groups;
import mindustry.gen.Unit;

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
        float selfRange = unit.type.range;

        Unit target = null;
        float closest = Float.MAX_VALUE;

        for(Unit other : Groups.unit){
            if(other.team == unit.team)continue;
            if(!other.isValid())continue;

            float distance = unit.dst(other);

            // 自己索敌范围内，不传送
            if(distance <= selfRange)continue;

            // 目标索敌范围必须大于自身
            if(other.type.range <= selfRange)continue;

            // 寻找最近符合条件的目标
            if(distance < closest){
                closest = distance;
                target = other;
            }
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
        t.add(abilityStat("interval", interval / 60f));
    }

    @Override
    public String getBundle(){
        return "ability.targetteleport";
    }
}