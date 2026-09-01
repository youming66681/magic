package magical.content;
import mindustry.ai.types.FlyingAI;
import mindustry.entities.Units;
import mindustry.gen.Unit;
public class CarrierDroneAI extends FlyingAI{
    public Unit mother;
    public CarrierAbility ability;
    public CarrierDroneAI(){}
    public CarrierDroneAI(Unit drone, Unit mother, CarrierAbility ability){
        this.unit = drone;
        this.mother = mother;
        this.ability = ability;
    }
    @Override
    public void updateUnit(){
        if(mother == null || !mother.isAdded() || mother.dead){
            unit.kill();
            return;
        }
        if(ability == null){
            unit.kill();
            return;
        }
        Unit enemy = Units.closestEnemy(
                unit.team,
                unit.x,
                unit.y,
                ability.engageRange,
                u -> u.isValid() && !u.dead
        );
        if(enemy != null){
            target = enemy;
            super.updateUnit();
            return;
        }
        target = null;
        ability.reclaimDrone(unit);
        unit.remove();
    }
}