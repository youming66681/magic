package magical.content.abilities;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
public class DamageLimitAbility extends Ability{
    public float interval;
    public float damagePercent;
    private float timer;
    private float damageTaken;
    private float lastHealth;
    public DamageLimitAbility(){
        this(300f,0.90f);
    }
    public DamageLimitAbility(float interval,float damagePercent){
        this.interval = Math.max(1f,interval);
        this.damagePercent = Math.max(0f,Math.min(1f,damagePercent));
        this.timer = 0f;
        this.damageTaken = 0f;
        this.lastHealth = -1f;
    }
    @Override
    public void created(Unit unit){
        timer = 0f;
        damageTaken = 0f;
        lastHealth = unit.health;
    }
    @Override
    public void update(Unit unit){
        if(unit == null || !unit.isAdded())return;
        if(unit.dead)return;
        timer += Time.delta;
        if(timer >= interval){
            timer %= interval;
            damageTaken = 0f;
            lastHealth = unit.health;
            return;
        }
        if(lastHealth < 0f){
            lastHealth = unit.health;
            return;
        }
        float currentHealth = unit.health;
        if(currentHealth < lastHealth){
            float damage = lastHealth - currentHealth;
            float maxDamage = unit.maxHealth() * damagePercent;
            float remaining = Math.max(0f,maxDamage - damageTaken);
            if(damage > remaining){
                float excess = damage - remaining;
                unit.health = Math.min(unit.maxHealth(),unit.health + excess);
                damage = remaining;
            }
            damageTaken += damage;
        }
        lastHealth = unit.health;
    }
    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(abilityStat("interval",formatSeconds(interval)));
        t.row();
        t.add(abilityStat("damage-percent",(int)(damagePercent * 100f) + "%"));
        t.row();
    }
    private String formatSeconds(float ticks){
        float seconds = ticks / 60f;
        if(seconds == (int)seconds){
            return (int)seconds + "秒";
        }
        return String.format("%.1f秒",seconds);
    }
}