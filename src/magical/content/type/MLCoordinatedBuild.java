package magical.content;

import arc.Core;
import arc.Events;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.ui.Styles;
import java.lang.reflect.Field;

public class MLCoordinatedBuild{
    private static boolean enabled=false;
    private static boolean initialized=false;
    private static Table hudTable;
    private static ImageButton hudButton;
    private static final Seq<BuildPlan> plans=new Seq<>();
    private static final Seq<Unit> builders=new Seq<>();
    private static final ObjectSet<String> dispatchedPlans=new ObjectSet<>();
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
        if(!enabled){
            dispatchedPlans.clear();
        }
        updateButtonState();
    }
    private static void ensureHudButton(){
        if(Vars.ui==null||Vars.ui.hudGroup==null)return;
        if(hudTable!=null&&hudTable.parent!=null)return;
        hudTable=new Table();
        hudTable.setFillParent(true);
        hudTable.top().right();
        hudTable.marginTop(150f);
        hudTable.marginRight(8f);
        hudButton=hudTable.button(Icon.units,Styles.clearNoneTogglei,MLCoordinatedBuild::toggle).size(48f).get();
        hudButton.addListener(new arc.scene.ui.Tooltip(t -> t.background(Styles.black6).add(Core.bundle.get("ml-coordinated-build", "协调建造：自动分配建造计划")).style(Styles.outlineLabel)));
        hudTable.update(MLCoordinatedBuild::updateButtonState);
        Vars.ui.hudGroup.addChild(hudTable);
        updateButtonState();
    }
    private static void updateButtonState(){
        if(hudTable == null)return;
        hudTable.visible = Vars.state != null && !Vars.state.isMenu();
        if(hudButton != null){
            hudButton.setChecked(enabled);
        }
    }
    private static boolean hasSelectedBuilder(){
        if(Vars.player==null||Vars.player.dead())return false;
        if(Vars.control==null||Vars.control.input==null)return false;
        Object input=Vars.control.input;
        Class<?> type=input.getClass();
        while(type!=null){
            try{
                Field field=type.getDeclaredField("selectedUnits");
                field.setAccessible(true);
                Object selected=field.get(input);
                if(selected instanceof Iterable<?>){
                    for(Object object:(Iterable<?>)selected){
                        if(object instanceof Unit){
                            Unit unit=(Unit)object;
                            if(isFriendlyBuilder(unit))return true;
                        }
                    }
                }
                return false;
            }catch(NoSuchFieldException ignored){
                type=type.getSuperclass();
            }catch(Exception ignored){
                return false;
            }
        }
        return false;
    }
    private static boolean isFriendlyBuilder(Unit unit){
        return unit!=null
                &&unit.isValid()
                &&Vars.player!=null
                &&unit.team==Vars.player.team()
                &&unit.type!=null
                &&unit.type.buildSpeed>0f;
    }
    private static void update(){
        if(!enabled||Vars.state==null||Vars.state.isMenu())return;
        if(Vars.player==null||Vars.player.dead())return;
        if(Vars.control==null||Vars.control.input==null)return;
        collectBuilders();
        if(builders.isEmpty())return;
        plans.clear();
        Vars.control.input.getSyncedPlans(plans);
        for(BuildPlan plan:plans){
            if(plan==null)continue;
            String key=planKey(plan);
            if(dispatchedPlans.contains(key))continue;
            Unit builder=nextBuilder();
            if(builder==null)break;
            builder.addBuild(plan.copy());
            dispatchedPlans.add(key);
        }
    }
    private static void collectBuilders(){
        builders.clear();
        Groups.unit.each(unit->{
            if(isFriendlyBuilder(unit)){
                builders.add(unit);
            }
        });
        if(nextBuilder>=builders.size)nextBuilder=0;
    }
    private static Unit nextBuilder(){
        if(builders.isEmpty())return null;
        if(nextBuilder>=builders.size)nextBuilder=0;
        Unit result=builders.get(nextBuilder);
        nextBuilder=(nextBuilder+1)%builders.size;
        return result;
    }
    private static String planKey(BuildPlan plan){
        String blockName=plan.block==null?"null":plan.block.name;
        return plan.x+":"+plan.y+":"+blockName+":"+plan.rotation+":"+plan.breaking;
    }
}
