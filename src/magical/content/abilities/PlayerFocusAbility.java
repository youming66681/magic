package magical.content.abilities;

import arc.scene.ui.layout.Table;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import arc.Events;
import magical.content.event.UnitFocusEvent;
import mindustry.gen.Call;
import mindustry.gen.Groups;

public class PlayerFocusAbility extends Ability{

    public boolean triggered;

    @Override
    public void created(Unit unit){
        if(triggered)return;
        triggered = true;

        Events.fire(new UnitFocusEvent(unit));

        Groups.player.each(player -> {
            if(player.con != null){
                Call.infoMessage(
                        player.con,
                        "[accent]"
                                + unit.type.localizedName
                                + " 号已介入战局！"
                );
            }
        });
    }

    @Override
    public String getBundle(){
        return "ability.playerfocus";
    }
}