package magical.content;

import arc.Core;
import arc.Events;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.ui.Styles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MLCoordinatedBuild{
    private static boolean enabled = false;
    private static boolean initialized = false;
    private static boolean buttonCreated = false;
    private static Table hudButton;
    private static final Seq<Unit> builders = new Seq<>();
    private static final Seq<BuildPlan> plans = new Seq<>();
    private static final Set<String> currentPlans = new HashSet<>();
    private static final Map<String, Unit> assignedBuilders = new HashMap<>();
    private static final Map<String, BuildPlan> confirmedPlans = new HashMap<>();
    private static int nextBuilder = 0;
    private static float updateTimer = 0f;
    private static final float updateInterval = 10f;
    private static float confirmTimer = 0f;
    private static final float confirmDelay = 30f;
    private MLCoordinatedBuild(){}
    public static void init(){
        if(initialized)return;
        initialized = true;
        Events.on(ClientLoadEvent.class, event -> installHudButton());
        Events.run(Trigger.update, MLCoordinatedBuild::update);
    }
    private static void installHudButton(){
        if(buttonCreated || Vars.ui == null || Vars.ui.hudGroup == null)return;
        hudButton = new Table();
        hudButton.setFillParent(true);
        hudButton.top().left().marginTop(65f).marginLeft(8f);
        hudButton.button(Icon.hammer, Styles.clearNoneTogglei, MLCoordinatedBuild::toggle)
                .size(50f)
                .checked(button -> enabled)
                .tooltip(Core.bundle.get("ml-coordinated-build", "协调建造"), true);
        Vars.ui.hudGroup.addChild(hudButton);
        buttonCreated = true;
        updateButtonVisibility();
    }
    public static void toggle(){
        setEnabled(!enabled);
    }
    public static boolean isEnabled(){
        return enabled;
    }
    public static void setEnabled(boolean value){
        enabled = value;
        updateTimer = 0f;
        confirmTimer = 0f;
        if(!enabled){
            assignedBuilders.clear();
            confirmedPlans.clear();
        }
    }
    private static void updateButtonVisibility(){
        if(hudButton == null)return;
        boolean visible = Vars.player != null && Vars.player.unit() != null && Vars.player.unit().isValid() && !Vars.player.unit().dead && Vars.player.unit().team == Vars.player.team();
        hudButton.visible = visible;
        hudButton.touchable = visible ? Touchable.enabled : Touchable.disabled;
        if(!visible && enabled)setEnabled(false);
    }
    private static void update(){
        updateButtonVisibility();
        if(!enabled || Vars.state == null || Vars.state.isMenu() || Vars.player == null || Vars.control == null || Vars.control.input == null){
            updateTimer = 0f;
            confirmTimer = 0f;
            return;
        }
        updateTimer += Time.delta;
        confirmTimer += Time.delta;
        if(updateTimer < updateInterval)return;
        updateTimer = 0f;
        collectBuilders();
        collectCandidatePlans();
        if(builders.isEmpty() || confirmedPlans.isEmpty())return;
        assignPlans();
    }
    private static void collectBuilders(){
        builders.clear();
        if(Vars.player == null)return;
        Groups.unit.each(unit -> {
            if(unit == null || !unit.isValid() || unit.dead)return;
            if(unit.team != Vars.player.team())return;
            if(!unit.canBuild())return;
            builders.add(unit);
        });
        if(nextBuilder >= builders.size)nextBuilder = 0;
    }
    private static void collectCandidatePlans(){
        plans.clear();
        currentPlans.clear();
        Vars.control.input.getSyncedPlans(plans);
        Set<String> seen = new HashSet<>();
        for(BuildPlan plan : plans){
            if(plan == null)continue;
            String key = planKey(plan);
            seen.add(key);
            currentPlans.add(key);
        }
        // 只把持续存在的计划视为确认候选，避免瞬时预览直接派发
        for(BuildPlan plan : plans){
            if(plan == null)continue;
            String key = planKey(plan);
            if(confirmedPlans.containsKey(key))continue;
            confirmedPlans.put(key, plan.copy());
        }
        // 计划消失时清除任务，避免撤销后继续执行
        confirmedPlans.keySet().retainAll(seen);
        assignedBuilders.keySet().retainAll(confirmedPlans.keySet());
    }
    private static void assignPlans(){
        if(builders.isEmpty() || confirmedPlans.isEmpty())return;
        for(Map.Entry<String, BuildPlan> entry : confirmedPlans.entrySet()){
            String key = entry.getKey();
            BuildPlan plan = entry.getValue();
            if(plan == null)continue;
            Unit assigned = assignedBuilders.get(key);
            if(isAvailableBuilder(assigned))continue;
            assignedBuilders.remove(key);
            Unit builder = findBuilderForPlan();
            if(builder == null)return;
            try{
                builder.addBuild(plan.copy());
                assignedBuilders.put(key, builder);
            }catch(Exception ignored){
                assignedBuilders.remove(key);
            }
        }
    }
    private static boolean isAvailableBuilder(Unit unit){
        return unit != null && unit.isValid() && !unit.dead && Vars.player != null && unit.team == Vars.player.team() && unit.canBuild();
    }
    private static Unit findBuilderForPlan(){
        if(builders.isEmpty())return null;
        int count = builders.size;
        for(int i = 0; i < count; i++){
            if(nextBuilder >= count)nextBuilder = 0;
            Unit unit = builders.get(nextBuilder++);
            if(isAvailableBuilder(unit))return unit;
        }
        return null;
    }
    private static String planKey(BuildPlan plan){
        String blockId = plan.block == null ? "break" : String.valueOf(plan.block.id);
        return plan.x + ":" + plan.y + ":" + blockId + ":" + plan.rotation;
    }
}