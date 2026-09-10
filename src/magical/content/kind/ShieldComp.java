package mindustry.entities.comp;
import arc.util.*;
import mindustry.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
@Component
abstract class ShieldComp implements Healthc, Posc{
    @Import float health, hitTime, x, y, healthMultiplier, armorOverride;
    @Import boolean dead;
    @Import Team team;
    @Import UnitType type;
    /** Absorbs health damage. */
    float shield;
    /** Subtracts an amount from damage. No need to save. */
    transient float armor;
    /** Shield opacity. */
    transient float shieldAlpha = 0f;
    private float modifyIncomingDamage(float amount){
        if(amount <= 0f) return amount;
        if(type != null && type.abilities != null){
            for(var ability : type.abilities){
                if(ability != null){
                    amount = ability.modifyDamage(self(), amount);
                    if(amount <= 0f){
                        return 0f;
                    }
                }
            }
        }
        return amount;
    }
    @Replace
    @Override
    public void damage(float amount){
        amount = Damage.applyArmor(amount, armorOverride >= 0f ? armorOverride : armor) / healthMultiplier / Vars.state.rules.unitHealth(team);
        amount = modifyIncomingDamage(amount);
        rawDamage(amount);
    }
    @Replace
    @Override
    public void damagePierce(float amount, boolean withEffect){
        float pre = hitTime;
        amount = amount / healthMultiplier / Vars.state.rules.unitHealth(team);
        amount = modifyIncomingDamage(amount);
        rawDamage(amount);
        if(!withEffect){
            hitTime = pre;
        }
    }
    @Replace
    @Override
    public void damageArmorMult(float amount, float armorMult, boolean withEffect){
        float pre = hitTime;
        amount = Damage.applyArmor(amount, armorOverride >= 0f ? armorOverride * armorMult : armor * armorMult) / healthMultiplier / Vars.state.rules.unitHealth(team);
        amount = modifyIncomingDamage(amount);
        rawDamage(amount);
        if(!withEffect){
            hitTime = pre;
        }
    }
    protected void rawDamage(float amount){
        boolean hadShields = shield > 0.0001f;
        if(Float.isNaN(health)) health = 0f;
        if(hadShields){
            shieldAlpha = 1f;
        }
        float shieldDamage = Math.min(Math.max(shield, 0), amount);
        shield -= shieldDamage;
        hitTime = 1f;
        amount -= shieldDamage;
        if(amount > 0 && type.killable){
            health -= amount;
            if(health <= 0 && !dead){
                kill();
            }
            if(hadShields && shield <= 0.0001f){
                Fx.unitShieldBreak.at(x, y, 0, type.shieldColor(self()), this);
            }
        }
    }
    @Override
    public void update(){
        shieldAlpha -= Time.delta / 15f;
        if(shieldAlpha < 0) shieldAlpha = 0f;
    }
}