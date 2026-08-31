package magical.content;

import mindustry.ai.types.FlyingAI;
import mindustry.entities.Units;
import mindustry.gen.*;

public class CarrierDroneAI extends FlyingAI {
    public Unit mother;
    public CarrierAbility ability;
    public Unit attackTarget;

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

        if (attackTarget == null || attackTarget.dead || !attackTarget.isAdded() || attackTarget.team() == unit.team ||
                !attackTarget.within(mother, ability.engageRange)) {
            attackTarget = Units.closestEnemy(unit.team, mother.x, mother.y, ability.engageRange,
                    u -> u.isValid() && u.team() != unit.team && !u.dead);
        }

        if (attackTarget != null) {
            unit.moveAt(attackTarget);
            unit.lookAt(attackTarget);
            unit.aim(attackTarget);
            if (unit.canShoot() && unit.within(attackTarget, unit.range())) {
                unit.shoot(attackTarget);
            }
        } else {
            unit.moveAt(mother);
            unit.lookAt(mother);
            if (unit.within(mother, ability.reclaimDistance)) {
                ability.reclaimDrone(unit);
                unit.remove();
            }
        }
    }
}