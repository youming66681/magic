package magical.content;

import arc.math.geom.Vec2;
import mindustry.ai.types.FlyingAI;
import mindustry.entities.Units;
import mindustry.gen.Unit;

public class CarrierDroneAI extends FlyingAI {
    public Unit mother;
    public CarrierAbility ability;

    public CarrierDroneAI() {}

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

        Unit enemy = Units.closestEnemy(unit.team, mother.x, mother.y, ability.engageRange,
                u -> u.isValid() && !u.dead);

        if (enemy != null) {
            target = enemy;
            super.updateUnit();
        } else {
            unit.moveAt(new Vec2(mother.x, mother.y));
            if (unit.within(mother, ability.reclaimDistance)) {
                ability.reclaimDrone(unit);
                unit.remove();
            }
        }
    }
}