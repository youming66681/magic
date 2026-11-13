
package magical.content;
import arc.Core;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
public class LowHealthRegenerationAbility extends Ability{
    public float healthThreshold = 0.5f;
    public float duration = 60f * 70f;
    public float healAmount = 1f;
    public float stationaryMultiplier = 3f;
    public float allyDuration = 60f * 70f;
    public float allyRange = 80f;
    public float allyHealAmount = 0.5f;
    private final ObjectMap<Unit, Float> selfTimers = new ObjectMap<>();
    private final ObjectMap<Unit, Float> allyTimers = new ObjectMap<>();
    private final ObjectSet<Unit> triggered = new ObjectSet<>();
    @Override
    public String localized(){
        return Core.bundle.get("ability.low-health-regeneration");
    }
    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + Core.bundle.get("ability.low-health-regeneration.description") + "[]");
        t.row();
    }

    @Override
    public void update(Unit unit){
        if(unit.dead || !unit.isAdded()){
            selfTimers.remove(unit);
            allyTimers.remove(unit);
            triggered.remove(unit);
            return;
        }
        if(unit.health < unit.maxHealth * healthThreshold && !triggered.contains(unit)){
            triggered.add(unit);
            selfTimers.put(unit, duration);
            allyTimers.put(unit, allyDuration);
        }
        Float selfTime = selfTimers.get(unit);
        if(selfTime != null && selfTime > 0f){
            float multiplier = unit.vel.len2() < 0.01f ? stationaryMultiplier : 1f;
            unit.heal(healAmount * multiplier * Time.delta);
            selfTimers.put(unit, Math.max(0f, selfTime - Time.delta));
        }
        Float allyTime = allyTimers.get(unit);
        if(allyTime != null && allyTime > 0f){
            Groups.unit.intersect(
                    unit.x - allyRange,
                    unit.y - allyRange,
                    allyRange * 2f,
                    allyRange * 2f,
                    other -> {
                        if(other == unit || other.dead || !other.isAdded()) return;
                        if(other.team != unit.team) return;
                        if(other.dst2(unit) > allyRange * allyRange) return;
                        other.heal(allyHealAmount * Time.delta);
                    }
            );
            allyTimers.put(unit, Math.max(0f, allyTime - Time.delta));
        }
    }
}
