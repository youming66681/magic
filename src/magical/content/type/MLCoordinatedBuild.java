package magical.content;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Unit;
import java.util.HashSet;
import java.util.Set;

public class MLCoordinatedBuild{
    private static boolean enabled = false;
    private static boolean initialized = false;
    private static final Seq<Unit> builders = new Seq<>();
    private static final Seq<BuildPlan> plans = new Seq<>();
    private static final Set<String> assignedPlans = new HashSet<>();
    private static final Set<String> currentPlans = new HashSet<>();
    private static int nextBuilder = 0;
    private static float updateTimer = 0f;
    private MLCoordinatedBuild(){}
    public static void init(){
        if(initialized)return;
        initialized = true;
        Events.run(Trigger.update, MLCoordinatedBuild::update);
    }
    public static void toggle(){
        enabled = !enabled;
    }
    public static boolean isEnabled(){
        return enabled;
    }
    public static void buildButton(Table table){
        TextureRegion region = Core.atlas.find("MLCoordinatedBuild");
        table.button(new Image(region), MLCoordinatedBuild::toggle).size(48f);
    }
    private static void update(){
        if(!enabled || Vars.state == null || Vars.state.isMenu() || Vars.player == null || Vars.control == null || Vars.control.input == null)return;
        updateTimer += Time.delta;
        if(updateTimer < 10f)return;
        updateTimer = 0f;
        collectBuilders();
        if(builders.isEmpty())return;
        collectPlans();
        if(plans.isEmpty())return;
        assignPlans();
    }
    private static void collectBuilders(){
        builders.clear();
        if(!Vars.control.input.commandMode)return;
        for(Unit unit : Vars.control.input.selectedUnits){
            if(unit == null || !unit.isValid() || unit.dead)continue;
            if(unit.team != Vars.player.team())continue;
            if(!unit.type.playerControllable)continue;
            if(!unit.canBuild())continue;
            builders.add(unit);
        }
    }
    private static void collectPlans(){
        plans.clear();
        currentPlans.clear();
        Vars.control.input.getSyncedPlans(plans);
        for(BuildPlan plan : plans){
            if(plan == null)continue;
            currentPlans.add(planKey(plan));
        }
        assignedPlans.retainAll(currentPlans);
    }
    private static void assignPlans(){
        if(builders.isEmpty() || plans.isEmpty())return;
        for(BuildPlan plan : plans){
            if(plan == null)continue;
            String key = planKey(plan);
            if(assignedPlans.contains(key))continue;
            Unit builder = findBuilderForPlan();
            if(builder == null)return;
            builder.addBuild(plan.copy());
            assignedPlans.add(key);
        }
    }
    private static Unit findBuilderForPlan(){
        if(builders.isEmpty())return null;
        int count = builders.size;
        for(int i = 0; i < count; i++){
            if(nextBuilder >= count)nextBuilder = 0;
            Unit unit = builders.get(nextBuilder++);
            if(unit != null && unit.isValid() && !unit.dead && unit.team == Vars.player.team() && unit.canBuild()){
                return unit;
            }
        }
        return null;
    }
    private static String planKey(BuildPlan plan){
        String blockId = plan.block == null ? "break" : String.valueOf(plan.block.id);
        return plan.x + ":" + plan.y + ":" + blockId + ":" + plan.rotation;
    }
}
