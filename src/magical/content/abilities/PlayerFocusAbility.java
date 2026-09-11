package magical.content;

import arc.scene.ui.layout.Table;
import arc.util.Timer;
import arc.Events;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Unit;

public class PlayerFocusAbility extends Ability{

    public float delay;
    public float focusTime;

    private boolean triggered;

    public PlayerFocusAbility(float delay,float focusTime){
        this.delay = delay;
        this.focusTime = focusTime;
        display = false;
    }

    @Override
    public void created(Unit unit){
        if(triggered)return;
        triggered = true;

        Timer.schedule(() -> {

            Groups.player.each(player -> {
                if(player.con != null){
                    Call.infoMessage(
                            player.con,
                            "[accent]" + unit.type.localizedName + " 号已介入战局！"
                    );
                }
            });

            Events.fire(new UnitFocusEvent(unit, focusTime));

        }, delay);
    }

    @Override
    public void addStats(Table t){
    }

    @Override
    public String getBundle(){
        return "ability.playerfocus";
    }
}