package magical.content;

import arc.Events;
import mindustry.game.EventType.UnitSpawnEvent;
import mindustry.gen.Unit;

public class MLSpawnUnits {

    public static void init(){

        Events.on(UnitSpawnEvent.class,e -> {

            Unit unit = e.unit;

            if(unit.type == MLUnitTypes.Starlight){

                MLFx.smallEnergyBlast.at(unit.x, unit.y);

            }

        });

    }
    public static void load()
}