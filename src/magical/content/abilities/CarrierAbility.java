package magical.content;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import arc.util.*;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Bar;

import static mindustry.Vars.*;

public class CarrierAbility extends Ability {
    public UnitType droneType;
    public int maxDrones = 4;
    public float engageRange = 80f;
    public float reclaimDistance = 8f;
    public float spawnInterval = 120f;

    private transient Seq<Unit> activeDrones = new Seq<>();
    private transient int storedDrones = 0;
    private transient float timer = 0f;

    public CarrierAbility(UnitType droneType, int maxDrones, float engageRange, float spawnInterval) {
        this.droneType = droneType;
        this.maxDrones = maxDrones;
        this.engageRange = engageRange;
        this.spawnInterval = spawnInterval;
    }

    public static Bar createDronesBar(Unit unit) {
        CarrierAbility ability = null;
        for (var a : unit.abilities) {
            if (a instanceof CarrierAbility ca) {
                ability = ca;
                break;
            }
        }
        if (ability == null) return null;
        CarrierAbility finalAbility = ability;
        return new Bar(
                () -> Core.bundle.format("bar.carrier-drones") + ": " + finalAbility.getTotalDrones() + " / " + finalAbility.maxDrones,
                () -> Pal.accent,
                () -> (float) finalAbility.getTotalDrones() / finalAbility.maxDrones
        );
    }

    @Override
    public void update(Unit unit) {
        if (storedDrones < maxDrones) {
            timer += Time.delta * state.rules.unitBuildSpeed(unit.team);
            if (timer >= spawnInterval) {
                timer = 0f;
                storedDrones++;
                Fx.producesmoke.at(unit.x, unit.y);
            }
        }

        Unit enemy = Units.closestEnemy(unit.team, unit.x, unit.y, engageRange, u -> u.isValid() && !u.dead);
        if (enemy != null && storedDrones > 0) {
            if(droneType == null){
                return;
            }
            Unit drone = droneType.create(unit.team);
            drone.set(unit.x, unit.y);
            drone.controller(new CarrierDroneAI(drone, unit, this));
            drone.add();
            activeDrones.add(drone);
            storedDrones--;
        }

        activeDrones.removeAll(d -> !d.isAdded() || d.dead);
    }

    public void reclaimDrone(Unit drone) {
        if (activeDrones.remove(drone)) {
            storedDrones++;
        }
    }

    public int getTotalDrones() { return storedDrones + activeDrones.size; }
    public int getStoredDrones() { return storedDrones; }
    public int getActiveDrones() { return activeDrones.size; }

    @Override
    public void death(Unit unit) {
        activeDrones.each(Unit::kill);
        activeDrones.clear();
    }

    @Override
    public String localized() {
        return Core.bundle.get("ability.carrier-name");
    }

    @Override
    public void addStats(Table t) {
        super.addStats(t);
        t.row();
        t.add("[lightgray]" + Core.bundle.get("ability.carrier-desc") + "[]").left().row();
        t.add("[lightgray]" + Core.bundle.get("stat.maxdrones") + ":[] " + maxDrones).left().row();
        t.add("[lightgray]" + Core.bundle.get("stat.range") + ":[] " + engageRange / 8f + " tiles").left().row();
        t.add("[lightgray]" + Core.bundle.get("stat.drone") + ":[] " + droneType.localizedName).left().row();
    }
}