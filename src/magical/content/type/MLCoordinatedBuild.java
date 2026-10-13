package magical.content;

import arc.Core;
import arc.Events;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.ui.Styles;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MLCoordinatedBuild{
    private static boolean enabled = false;
    private static boolean initialized = false;
    private static Table commandTable;
    private static TextButton injectedButton;
    private static final Seq<Unit> builders = new Seq<>();
    private static final Seq<BuildPlan> plans = new Seq<>();
    private static final Set<String> currentPlans = new HashSet<>();
    private static final Map<String, Unit> assignedBuilders = new HashMap<>();
    private static int nextBuilder = 0;
    private static float updateTimer = 0f;
    private static final float updateInterval = 10f;
    private static final String BUTTON_TEXT = "协调建造";
    private MLCoordinatedBuild(){}
    public static void init(){
        if(initialized)return;
        initialized = true;
        Events.run(Trigger.update, MLCoordinatedBuild::update);
        Events.run(Trigger.update, MLCoordinatedBuild::installCommandButton);
        Events.on(ClientLoadEvent.class, event -> installCommandButton());
    }
    public static void toggle(){
        enabled = !enabled;
        updateTimer = 0f;
        if(injectedButton != null)injectedButton.setChecked(enabled);
    }
    public static boolean isEnabled(){
        return enabled;
    }
    public static void setEnabled(boolean value){
        enabled = value;
        updateTimer = 0f;
        if(injectedButton != null)injectedButton.setChecked(enabled);
    }
    private static void installCommandButton(){
        if(Core.scene == null || Core.scene.root == null)return;
        if(injectedButton != null && injectedButton.parent != null)return;
        commandTable = findCommandTable(Core.scene.root);
        if(commandTable == null)return;
        injectedButton = commandTable.button(BUTTON_TEXT, Styles.clearTogglet, MLCoordinatedBuild::toggle).size(48f).get();
        injectedButton.update(() -> injectedButton.setChecked(enabled));
    }
    private static Table findCommandTable(Group group){
        if(group == null)return null;
        for(Element element : group.getChildren()){
            if(element instanceof Table table && containsUnitCommandButton(table))return table;
            if(element instanceof Group childGroup){
                Table found = findCommandTable(childGroup);
                if(found != null)return found;
            }
        }
        return null;
    }
    private static boolean containsUnitCommandButton(Table table){
        if(table == null)return false;
        boolean hasMove = false;
        boolean hasAssist = false;
        for(Element child : table.getChildren()){
            if(child instanceof TextButton button){
                String text = button.getLabel().getText().toString();
                if(text.equals(Core.bundle.get("command.move", "Move")))hasMove = true;
                if(text.equals(Core.bundle.get("command.assist", "Assist")))hasAssist = true;
            }
        }
        return hasMove && hasAssist;
    }
    private static void update(){
        if(!enabled || Vars.state == null || Vars.state.isMenu() || Vars.player == null || Vars.control == null || Vars.control.input == null){
            updateTimer = 0f;
            return;
        }
        updateTimer += Time.delta;
        if(updateTimer < updateInterval)return;
        updateTimer = 0f;
        collectBuilders();
        collectPlans();
        if(builders.isEmpty() || plans.isEmpty())return;
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
    private static void collectPlans(){
        plans.clear();
        currentPlans.clear();
        Vars.control.input.getSyncedPlans(plans);
        for(BuildPlan plan : plans){
            if(plan == null)continue;
            currentPlans.add(planKey(plan));
        }
        assignedBuilders.keySet().retainAll(currentPlans);
    }
    private static void assignPlans(){
        if(builders.isEmpty() || plans.isEmpty())return;
        for(BuildPlan plan : plans){
            if(plan == null)continue;
            String key = planKey(plan);
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
