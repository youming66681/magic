package magical.content;

import arc.Events;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MLCoordinatedBuild{
    private static boolean enabled=false;
    private static boolean initialized=false;
    private static final Seq<Unit> builders=new Seq<>();
    private static final Seq<BuildPlan> plans=new Seq<>();
    private static final Map<String,Unit> assignedBuilders=new HashMap<>();
    private static final Map<String,BuildPlan> confirmedPlans=new HashMap<>();
    private static int nextBuilder=0;
    private static float updateTimer=0f;
    private static final float updateInterval=10f;
    private MLCoordinatedBuild(){}
    public static void init(){
        if(initialized)return;
        initialized=true;
        Events.run(Trigger.update,MLCoordinatedBuild::update);
    }
    public static void toggle(){
        setEnabled(!enabled);
    }
    public static boolean isEnabled(){
        return enabled;
    }
    public static void setEnabled(boolean value){
        enabled=value;
        updateTimer=0f;
        if(!enabled){
            assignedBuilders.clear();
            confirmedPlans.clear();
        }
    }
    private static void update(){
        if(!enabled||Vars.state==null||Vars.state.isMenu()||Vars.player==null||Vars.player.dead()||Vars.control==null||Vars.control.input==null){
            updateTimer=0f;
            return;
        }
        updateTimer+=Time.delta;
        if(updateTimer<updateInterval)return;
        updateTimer=0f;
        collectBuilders();
        collectCandidatePlans();
        if(builders.isEmpty()||confirmedPlans.isEmpty())return;
        assignPlans();
    }
    private static void collectBuilders(){
        builders.clear();
        if(Vars.player==null)return;
        Groups.unit.each(unit->{
            if(unit==null||!unit.isValid()||unit.dead)return;
            if(unit.team!=Vars.player.team())return;
            if(!unit.canBuild())return;
            builders.add(unit);
        });
        if(nextBuilder>=builders.size)nextBuilder=0;
    }
    private static void collectCandidatePlans(){
        plans.clear();
        Vars.control.input.getSyncedPlans(plans);
        Set<String> seen=new HashSet<>();
        for(BuildPlan plan:plans){
            if(plan==null||plan.breaking||plan.block==null)continue;
            String key=planKey(plan);
            seen.add(key);
            confirmedPlans.putIfAbsent(key,plan.copy());
        }
        confirmedPlans.keySet().retainAll(seen);
        assignedBuilders.keySet().retainAll(seen);
    }
    private static void assignPlans(){
        if(builders.isEmpty()||confirmedPlans.isEmpty())return;
        for(Map.Entry<String,BuildPlan> entry:confirmedPlans.entrySet()){
            String key=entry.getKey();
            BuildPlan plan=entry.getValue();
            if(plan==null)continue;
            Unit assigned=assignedBuilders.get(key);
            if(isAvailableBuilder(assigned)&&hasEquivalentPlan(assigned,plan))continue;
            assignedBuilders.remove(key);
            Unit builder=findBuilderForPlan();
            if(builder==null)return;
            try{
                if(!hasEquivalentPlan(builder,plan)){
                    builder.addBuild(plan.copy());
                }
                assignedBuilders.put(key,builder);
            }catch(Exception ignored){
                assignedBuilders.remove(key);
            }
        }
    }
    private static boolean hasEquivalentPlan(Unit unit,BuildPlan target){
        if(unit==null||target==null)return false;
        for(BuildPlan plan:unit.plans()){
            if(plan==null)continue;
            if(planKey(plan).equals(planKey(target)))return true;
        }
        return false;
    }
    private static boolean isAvailableBuilder(Unit unit){
        return unit!=null&&unit.isValid()&&!unit.dead&&Vars.player!=null&&unit.team==Vars.player.team()&&unit.canBuild();
    }
    private static Unit findBuilderForPlan(){
        if(builders.isEmpty())return null;
        int count=builders.size;
        for(int i=0;i<count;i++){
            if(nextBuilder>=count)nextBuilder=0;
            Unit unit=builders.get(nextBuilder++);
            if(isAvailableBuilder(unit))return unit;
        }
        return null;
    }
    private static String planKey(BuildPlan plan){
        String blockId=plan.block==null?"break":String.valueOf(plan.block.id);
        return plan.x+":"+plan.y+":"+blockId+":"+plan.rotation;
    }
}