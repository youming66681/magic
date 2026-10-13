
package magical.content;
import arc.Core;
import arc.Events;
import arc.math.geom.Vec2;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.ui.Styles;
public class MLCoordinatedBuild{
    private static boolean enabled=false;
    private static boolean initialized=false;
    private static Table hudTable;
    private static ImageButton hudButton;
    private static final Seq<BuildPlan> plans=new Seq<>();
    private static final Seq<Unit> builders=new Seq<>();
    private static final ObjectSet<String> dispatchedPlans=new ObjectSet<>();
    private static final ObjectMap<Unit,Vec2> lastFollowPositions=new ObjectMap<>();
    private static final Vec2 lastPlayerPosition=new Vec2();
    private static final Vec2 followDirection=new Vec2(1f,0f);
    private static boolean hasLastPlayerPosition=false;
    private static int nextBuilder=0;
    private static final float followDistance=100f;
    private static final float followUpdateDistance=24f;
    private static final float followSpacing=24f;
    public static void init(){
        if(initialized)return;
        initialized=true;
        Events.run(Trigger.update,MLCoordinatedBuild::ensureHudButton);
        Events.run(Trigger.update,MLCoordinatedBuild::update);
    }
    public static boolean isEnabled(){
        return enabled;
    }
    public static void toggle(){
        enabled=!enabled;
        if(!enabled){
            lastFollowPositions.clear();
            dispatchedPlans.clear();
            hasLastPlayerPosition=false;
        }
        updateButtonState();
    }
    private static void ensureHudButton(){
        if(Core.scene==null||Vars.ui==null||Vars.state==null)return;
        if(hudTable!=null&&hudTable.parent!=null){
            updateButtonState();
            hudTable.toFront();
            return;
        }
        hudTable=new Table();
        hudTable.setFillParent(true);
        hudTable.top().right();
        hudTable.marginTop(150f);
        hudTable.marginRight(8f);
        hudButton=hudTable.button(Icon.units,Styles.clearNoneTogglei,MLCoordinatedBuild::toggle).size(56f).get();
        hudTable.update(MLCoordinatedBuild::updateButtonState);
        Core.scene.add(hudTable);
        updateButtonState();
    }
    private static void updateButtonState(){
        if(hudTable==null)return;
        hudTable.visible=Vars.state!=null&&!Vars.state.isMenu()&&hasSelectedBuilder();
        if(hudButton!=null)hudButton.setChecked(enabled);
    }
    private static boolean hasSelectedBuilder(){
        if(Vars.player==null||Vars.player.dead())return false;
        if(Vars.control==null||Vars.control.input==null)return false;
        for(Unit unit:Vars.control.input.selectedUnits){
            if(isFriendlyBuilder(unit))return true;
        }
        return false;
    }
    private static boolean isFriendlyBuilder(Unit unit){
        return unit!=null&&unit.isValid()
                &&Vars.player!=null
                &&unit.team==Vars.player.team()
                &&unit.type!=null
                &&unit.type.buildSpeed>0f;
    }
    private static void update(){
        if(Vars.state==null||Vars.state.isMenu())return;
        if(Vars.player==null||Vars.player.dead())return;
        if(Vars.control==null||Vars.control.input==null)return;
        if(!enabled)return;
        updateFollowDirection();
        collectBuilders();
        if(builders.isEmpty()){
            lastFollowPositions.clear();
            return;
        }
        cleanupDispatchedPlans();
        distributePlans();
        cleanupDispatchedPlans();
        followIdleBuilders();
    }
    private static void updateFollowDirection(){
        Unit playerUnit=Vars.player.unit();
        if(playerUnit==null||!playerUnit.isValid())return;
        float dx=playerUnit.x-lastPlayerPosition.x;
        float dy=playerUnit.y-lastPlayerPosition.y;
        if(hasLastPlayerPosition&&dx*dx+dy*dy>0.25f){
            followDirection.set(dx,dy).nor();
        }else if(!hasLastPlayerPosition){
            float angle=(float)Math.toRadians(playerUnit.rotation);
            followDirection.set((float)Math.cos(angle),(float)Math.sin(angle));
        }
        lastPlayerPosition.set(playerUnit.x,playerUnit.y);
        hasLastPlayerPosition=true;
    }
    private static void collectBuilders(){
        builders.clear();
        for(Unit unit:Vars.control.input.selectedUnits){
            if(isFriendlyBuilder(unit)&&!builders.contains(unit,true)){
                builders.add(unit);
            }
        }
        if(nextBuilder>=builders.size)nextBuilder=0;
    }
    private static void distributePlans(){
        if(Vars.player==null||Vars.player.unit()==null)return;
        Seq<BuildPlan> source=Vars.player.unit().plans;
        if(source.isEmpty())return;
        plans.clear();
        for(BuildPlan plan:source){
            if(plan!=null)plans.add(plan.copy());
        }
        if(plans.isEmpty())return;
        for(BuildPlan plan:plans){
            String key=planKey(plan);
            if(dispatchedPlans.contains(key))continue;
            Unit builder=nextBuilder();
            if(builder==null)break;
            builder.addBuild(plan.copy());
            dispatchedPlans.add(key);
        }
        source.clear();
    }
    private static Unit nextBuilder(){
        if(builders.isEmpty())return null;
        if(nextBuilder>=builders.size)nextBuilder=0;
        Unit result=builders.get(nextBuilder);
        nextBuilder=(nextBuilder+1)%builders.size;
        return result;
    }
    private static void cleanupDispatchedPlans(){
        ObjectSet<String> activePlans=new ObjectSet<>();
        for(Unit builder:builders){
            if(builder==null||!builder.isValid())continue;
            for(BuildPlan plan:builder.plans()){
                if(plan!=null)activePlans.add(planKey(plan));
            }
        }
        Seq<String> stale=new Seq<>();
        for(String key:dispatchedPlans){
            if(!activePlans.contains(key))stale.add(key);
        }
        for(String key:stale){
            dispatchedPlans.remove(key);
        }
    }
    private static void followIdleBuilders(){
        if(Vars.player==null||Vars.player.dead())return;
        Unit playerUnit=Vars.player.unit();
        if(playerUnit==null||!playerUnit.isValid())return;
        float backX=-followDirection.x;
        float backY=-followDirection.y;
        float sideX=-backY;
        float sideY=backX;
        for(int i=0;i<builders.size;i++){
            Unit builder=builders.get(i);
            if(builder==null||!builder.isValid())continue;
            if(!builder.plans().isEmpty())continue;
            float sideOffset=(i-(builders.size-1)/2f)*followSpacing;
            float targetX=playerUnit.x+backX*followDistance+sideX*sideOffset;
            float targetY=playerUnit.y+backY*followDistance+sideY*sideOffset;
            float dx=builder.x-targetX;
            float dy=builder.y-targetY;
            if(dx*dx+dy*dy<followUpdateDistance*followUpdateDistance){
                lastFollowPositions.remove(builder);
                continue;
            }
            Vec2 last=lastFollowPositions.get(builder);
            if(last!=null&&last.dst2(targetX,targetY)<followUpdateDistance*followUpdateDistance)continue;
            Vec2 target=new Vec2(targetX,targetY);
            Call.commandUnits(
                    Vars.player,
                    new int[]{builder.id()},
                    null,
                    null,
                    target,
                    false,
                    true
            );
            lastFollowPositions.put(builder,target);
        }
    }
    private static String planKey(BuildPlan plan){
        String blockName=plan.block==null?"null":plan.block.name;
        return plan.x+":"+plan.y+":"+blockName+":"+plan.rotation+":"+plan.breaking;
    }
}
