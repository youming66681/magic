package magical.content;

import mindustry.ai.types.FlyingAI;
import mindustry.entities.Units;
import mindustry.gen.*;

public class CarrierDroneAI extends FlyingAI {
    public Unit mother;
    public CarrierAbility ability;

    public CarrierDroneAI(Unit drone, Unit mother, CarrierAbility ability) {
        this.unit = drone;
        this.mother = mother;
        this.ability = ability;
    }

    @Override
    public void updateUnit() {
        if (mother == null || !mother.isAdded() || mother.dead) {
            unit.kill();
            return;
        }

        if (target == null || target.dead || !target.isAdded() || target.team() == unit.team ||
                !target.within(mother, ability.engageRange)) {
            target = Units.closestEnemy(unit.team, mother.x, mother.y, ability.engageRange,
                    u -> u.isValid() && u.team() != unit.team && !u.dead);
        }

        if (target != null) {
            super.updateUnit();
        } else {
            moveTo(mother, ability.reclaimDistance);
            if (unit.within(mother, ability.reclaimDistance)) {
                ability.reclaimDrone(unit);
                unit.remove();
            }
        }
    }

    @Override
    public void updateTargeting() {
    }
}