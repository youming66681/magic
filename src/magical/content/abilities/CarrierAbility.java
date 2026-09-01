package magical.content;
import arc.Core;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import static mindustry.Vars.*;
public class CarrierAbility extends Ability{
    public UnitType droneType;
    public int maxDrones = 4;
    public float engageRange = 80f;
    public float reclaimDistance = 12f;
    public float spawnInterval = 120f;
    private transient Seq<Unit> activeDrones = new Seq<>();
    private transient int storedDrones = 0;
    private transient float timer = 0f;
    public CarrierAbility(UnitType droneType, int maxDrones, float engageRange, float spawnInterval){
        this.droneType = droneType;
        this.maxDrones = maxDrones;
        this.engageRange = engageRange;
        this.spawnInterval = spawnInterval;
    }
    @Override
    public void update(Unit unit){
        if(activeDrones == null){
            activeDrones = new Seq<>();
        }
        activeDrones.removeAll(d -> d == null || d.dead);
        if(getTotalDrones() < maxDrones){
            timer += Time.delta * state.rules.unitBuildSpeed(unit.team);
            if(timer >= spawnInterval){
                timer = 0f;
                if(getTotalDrones() < maxDrones){
                    storedDrones++;
                    if(storedDrones > maxDrones){
                        storedDrones = maxDrones;
                    }
                    Fx.producesmoke.at(unit.x, unit.y);
                }
            }
        }else{
            timer = 0f;
        }
        Unit enemy = Units.closestEnemy(
                unit.team,
                unit.x,
                unit.y,
                engageRange,
                u -> u.isValid() && !u.dead
        );
        if(enemy != null && storedDrones > 0){
            if(droneType == null){
                return;
            }
            if(getTotalDrones() >= maxDrones){
                return;
            }
            Unit drone = droneType.create(unit.team);
            if(drone == null){
                return;
            }
            drone.set(unit.x, unit.y);
            drone.controller(new CarrierDroneAI(drone, unit, this));
            drone.add();
            activeDrones.add(drone);
            storedDrones--;
        }
    }
    @Override
    public void displayBars(Unit unit, Table bars){
        bars.add(
                new Bar(
                        () -> Core.bundle.format(
                                "bar.carrier-drones",
                                getTotalDrones(),
                                maxDrones
                        ),
                        () -> Pal.accent,
                        () -> maxDrones <= 0 ? 0f : Math.min(1f, (float)getTotalDrones() / maxDrones)
                )
        ).row();
    }
    public void reclaimDrone(Unit drone){
        if(activeDrones == null){
            activeDrones = new Seq<>();
        }
        if(activeDrones.remove(drone)){
            storedDrones = Math.min(storedDrones + 1, maxDrones);
        }
    }
    public int getTotalDrones(){
        return storedDrones + (activeDrones == null ? 0 : activeDrones.size);
    }
    public int getStoredDrones(){
        return storedDrones;
    }
    public int getActiveDrones(){
        return activeDrones == null ? 0 : activeDrones.size;
    }
    @Override
    public void death(Unit unit){
        if(activeDrones == null){
            return;
        }
        activeDrones.each(d -> {
            if(d != null && d.isAdded() && !d.dead){
                d.kill();
            }
        });
        activeDrones.clear();
        storedDrones = 0;
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
        t.add("[lightgray]" + Core.bundle.get("stat.drone") + ":[] " + droneType.localizedName).left().row();
    }
}