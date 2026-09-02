package magical.content;

import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import arc.scene.ui.layout.Table;

public class DebuffImmunityAbility extends Ability{
    public float threshold = 0.20f;
    public DebuffImmunityAbility(float threshold){
        this.threshold = Math.max(0f,Math.min(threshold,0.9999f));
    }
    @Override
    public void init(UnitType type){
        super.init(type);
        for(StatusEffect effect : Vars.content.statusEffects()){
            if(effect == null)continue;
            if(isImmune(effect) && !type.immunities.contains(effect)){
                type.immunities.add(effect);
            }
        }
    }
    private boolean isImmune(StatusEffect effect){
        if(effect == null)return false;
        boolean hasNegative = false;
        if(effect.speedMultiplier < 1f){
            hasNegative = true;
            if(1f - effect.speedMultiplier > threshold)return false;
        }
        if(effect.reloadMultiplier < 1f){
            hasNegative = true;
            if(1f - effect.reloadMultiplier > threshold)return false;
        }
        if(effect.buildSpeedMultiplier < 1f){
            hasNegative = true;
            if(1f - effect.buildSpeedMultiplier > threshold)return false;
        }
        if(effect.healthMultiplier < 1f){
            hasNegative = true;
            if(1f - effect.healthMultiplier > threshold)return false;
        }
        if(effect.dragMultiplier < 1f){
            hasNegative = true;
            if(1f - effect.dragMultiplier > threshold)return false;
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