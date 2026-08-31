package magical.content;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.Vars;

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

        Units.nearby(unit.team, unit.x, unit.y, engageRange, enemy -> {
            if (storedDrones > 0 && enemy.team != unit.team && enemy.isValid() && !enemy.dead) {
                Unit drone = droneType.create(unit.team);
                drone.set(unit.x, unit.y);
                drone.controller(new CarrierDroneAI(drone, unit, this));
                drone.add();
                activeDrones.add(drone);
                storedDrones--;
                return true;
            }
            return false;
        });

        activeDrones.removeAll(d -> !d.isAdded() || d.dead);
    }

    public void reclaimDrone(Unit drone) {
        if (activeDrones.remove(drone)) {
            storedDrones++;
        }
    }

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
    @Override
    public void setBars() {
        carrierMother.addBar("drones", unit -> new Bar(
                () -> Core.bundle.format("bar.carrier-drones") + ": "
                        + ((CarrierAbility) unit.getAbility(CarrierAbility.class)).getTotalDrones()
                        + " / " + ((CarrierAbility) unit.getAbility(CarrierAbility.class)).maxDrones,
                () -> Pal.accent,
                () -> (float) ((CarrierAbility) unit.getAbility(CarrierAbility.class)).getTotalDrones()
                        / ((CarrierAbility) unit.getAbility(CarrierAbility.class)).maxDrones
        ));
    }
}