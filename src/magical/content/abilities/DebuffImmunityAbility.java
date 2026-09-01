package magical.content;

import arc.scene.ui.layout.Table;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;

public class DebuffImmunityAbility extends Ability{
    public float threshold = 0.20f;
    public DebuffImmunityAbility(float threshold){
        this.threshold = threshold;
    }
    @Override
    public void init(UnitType type){
        super.init(type);
        threshold = Math.max(0f,Math.min(threshold,0.9999f));
        for(StatusEffect effect : StatusEffect.all){
            if(effect == null)continue;
            if(isImmune(effect) && !type.immunities.contains(effect)){
                type.immunities.add(effect);
            }
        }
    }
    @Override
    public void update(Unit unit){
        if(unit == null || !unit.isAdded() || unit.dead)return;
        for(int i = unit.statuses.size - 1;i >= 0;i--){
            var entry = unit.statuses.get(i);
            if(entry == null || entry.effect == null)continue;
            if(isImmune(entry.effect)){
                unit.statuses.remove(i);
            }
        }
    }
    private boolean isImmune(StatusEffect effect){
        if(effect == null)return false;
        float limit = threshold;
        boolean hasNegative = false;
        if(effect.speedMultiplier < 1f){
            hasNegative = true;
            if(1f - effect.speedMultiplier > limit)return false;
        }
        if(effect.reloadMultiplier < 1f){
            hasNegative = true;
            if(1f - effect.reloadMultiplier > limit)return false;
        }
        if(effect.buildSpeedMultiplier < 1f){
            hasNegative = true;
            if(1f - effect.buildSpeedMultiplier > limit)return false;
        }
        if(effect.healthMultiplier < 1f){
            hasNegative = true;
            if(1f - effect.healthMultiplier > limit)return false;
        }
        if(effect.damageMultiplier > 1f){
            hasNegative = true;
            if(effect.damageMultiplier - 1f > limit)return false;
        }
        if(effect.dragMultiplier > 1f){
            hasNegative = true;
            if(effect.dragMultiplier - 1f > limit)return false;
        }
        return hasNegative;
    }
    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(abilityStat("threshold",(int)(threshold * 100f) + "%"));
        t.row();
    }
}