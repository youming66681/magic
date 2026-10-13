package magical.content;

import arc.Core;
import arc.Events;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.ai.types.BuilderAI;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.ui.Styles;

public class MLCoordinatedBuild{
    private static boolean enabled=false;
    private static boolean initialized=false;
    private static Table hudTable;
    private static ImageButton hudButton;
    private static final Seq<Unit> builders=new Seq<>();
    private static final ObjectSet<String> dispatchedPlans=new ObjectSet<>();
    private static final Seq<String> stalePlans=new Seq<>();
    private static int nextBuilder=0;
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
                &&unit!=Vars.player.unit()
                &&unit.team==Vars.player.team()
                &&unit.type!=null
                &&unit.type.buildSpeed>0f;
    }
    private static void update(){
        if(Vars.state==null||Vars.state.isMenu())return;
        if(Vars.player==null||Vars.player.dead())return;
        if(Vars.control==null||Vars.control.input==null)return;
        if(!enabled)return;
        collectBuilders();
        if(builders.isEmpty())return;
        cleanupDispatchedPlans();
        distributePlans();
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
        var source=Vars.player.unit().plans;
        if(source.isEmpty())return;
        int checked=0;
        int maxChecks=source.size;
        while(!source.isEmpty()&&!builders.isEmpty()&&checked<maxChecks){
            boolean assigned=false;
            for(int i=0;i<builders.size;i++){
                if(source.isEmpty())return;
                Unit builder=nextBuilder();
                if(builder==null)return;
                if(!builder.plans().isEmpty())continue;
                BuildPlan plan=source.first();
                if(plan==null){
                    source.removeFirst();
                    checked++;
                    continue;
                }
                String key=planKey(plan);
                if(dispatchedPlans.contains(key)){
                    source.removeFirst();
                    checked++;
                    continue;
                }
                builder.addBuild(plan.copy());
                if(builder.controller() instanceof BuilderAI ai){
                    ai.following=null;
                    ai.assistFollowing=null;
                }
                dispatchedPlans.add(key);
                source.removeFirst();
                assigned=true;
                break;
            }
            if(!assigned)break;
            checked=0;
            maxChecks=source.size;
        }
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
        Groups.unit.each(unit->{
            if(unit==null||!unit.isValid())return;
            for(BuildPlan plan:unit.plans()){
                if(plan!=null)activePlans.add(planKey(plan));
            }
        });
        stalePlans.clear();
        for(String key:dispatchedPlans){
            if(!activePlans.contains(key))stalePlans.add(key);
        }
        for(String key:stalePlans){
            dispatchedPlans.remove(key);
        }
    }
    private static String planKey(BuildPlan plan){
        String blockName=plan.block==null?"break":plan.block.name;
        return plan.x+":"+plan.y+":"+blockName+":"+plan.rotation+":"+plan.breaking;
    }
}
