package magical.content;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Bar;

import static mindustry.Vars.*;

public class CarrierAbility extends Ability{
    public UnitType droneType;
    public int maxDrones;
    public float engageRange;
    public float reclaimDistance = 8f;
    public float spawnInterval;
    private transient ObjectMap<Unit, CarrierData> carriers = new ObjectMap<>();
    public CarrierAbility(UnitType droneType,int maxDrones,float engageRange,float spawnInterval){
        this.droneType = droneType;
        this.maxDrones = maxDrones;
        this.engageRange = engageRange;
        this.spawnInterval = spawnInterval;
    }
    private CarrierData getData(Unit carrier){
        CarrierData data = carriers.get(carrier);
        if(data == null){
            data = new CarrierData(maxDrones);
            carriers.put(carrier,data);
        }
        return data;
    }
    public static Bar createDronesBar(Unit unit){
        CarrierAbility ability = null;
        for(var a : unit.abilities){
            if(a instanceof CarrierAbility ca){
                ability = ca;
                break;
            }
        }
        if(ability == null)return null;
        CarrierAbility finalAbility = ability;
        return new Bar(
                () -> Core.bundle.get("bar.carrier-drones") + ": " + finalAbility.getTotalDrones(unit) + " / " + finalAbility.maxDrones,
                () -> Pal.accent,
                () -> finalAbility.maxDrones <= 0 ? 0f : (float)finalAbility.getTotalDrones(unit) / finalAbility.maxDrones
        );
    }
    @Override
    public void update(Unit unit){
        if(droneType == null || maxDrones <= 0)return;
        if(!unit.isAdded() || unit.dead){
            carriers.remove(unit);
            return;
        }
        CarrierData data = getData(unit);
        data.activeDrones.removeAll(d -> d == null || !d.isAdded() || d.dead);
        if(data.storedDrones < 0){
            data.storedDrones = 0;
        }
        if(data.storedDrones > maxDrones){
            data.storedDrones = maxDrones;
        }
        if(data.storedDrones < maxDrones){
            data.timer += Time.delta * state.rules.unitBuildSpeed(unit.team);
            if(data.timer >= spawnInterval){
                data.timer = 0f;
                data.storedDrones++;
                Fx.producesmoke.at(unit.x,unit.y);
            }
        }else{
            data.timer = 0f;
        }
        Teamc enemy = Units.closestTarget(
                unit.team,
                unit.x,
                unit.y,
                engageRange,
                u -> u != null && u.isValid() && !u.dead,
                b -> b != null && b.isValid() && !b.dead
        );
        if(enemy != null && data.storedDrones > 0 && data.activeDrones.size < maxDrones){
            Unit drone = droneType.create(unit.team);
            if(drone != null){
                drone.set(unit.x,unit.y);
                drone.controller(new CarrierDroneAI(drone,unit,this));
                drone.add();
                data.activeDrones.add(drone);
                data.storedDrones--;
            }
        }
    }
    public void reclaimDrone(Unit carrier,Unit drone){
        if(carrier == null || drone == null)return;
        CarrierData data = carriers.get(carrier);
        if(data == null)return;
        data.activeDrones.remove(drone);
        if(data.storedDrones < maxDrones){
            data.storedDrones++;
        }
        if(data.storedDrones > maxDrones){
            data.storedDrones = maxDrones;
        }
    }
    public int getTotalDrones(Unit carrier){
        CarrierData data = carriers.get(carrier);
        if(data == null)return maxDrones;
        return Math.min(maxDrones,data.storedDrones + data.activeDrones.size);
    }
    public int getStoredDrones(Unit carrier){
        CarrierData data = carriers.get(carrier);
        if(data == null)return maxDrones;
        return Math.min(maxDrones,Math.max(0,data.storedDrones));
    }
    public int getActiveDrones(Unit carrier){
        CarrierData data = carriers.get(carrier);
        if(data == null)return 0;
        return Math.min(maxDrones,Math.max(0,data.activeDrones.size));
    }
    @Override
    public void death(Unit unit){
        CarrierData data = carriers.remove(unit);
        if(data == null)return;
        data.activeDrones.each(d -> {
            if(d != null && d.isAdded() && !d.dead){
                d.kill();
            }
        });
        data.activeDrones.clear();
        data.storedDrones = 0;
        data.timer = 0f;
    }
    @Override
    public String localized(){
        return Core.bundle.get("ability.carrier-name");
    }
    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.row();
        t.add("[lightgray]" + Core.bundle.get("ability.carrier-desc") + "[]").left().row();
        t.add("[lightgray]" + Core.bundle.get("stat.maxdrones") + ":[] " + maxDrones).left().row();
        t.add("[lightgray]" + Core.bundle.get("stat.range") + ":[] " + engageRange / 8f + " tiles").left().row();
        t.add("[lightgray]" + Core.bundle.get("stat.drone") + ":[] " + (droneType == null ? Core.bundle.get("unit.unknown") : droneType.localizedName)).left().row();
    }
    public static class CarrierData{
        public Seq<Unit> activeDrones = new Seq<>();
        public int storedDrones;
        public float timer = 0f;
        public CarrierData(int maxDrones){
            storedDrones = maxDrones;
        }
    }
}