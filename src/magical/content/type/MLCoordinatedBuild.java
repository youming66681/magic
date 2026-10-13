
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
import java.util.HashSet;
import java.util.Set;
public class MLCoordinatedBuild{
    private static boolean enabled = false;
    private static boolean initialized = false;
    private static boolean buttonInstalled = false;
    private static Table commandTable;
    private static final Seq<Unit> builders = new Seq<>();
    private static final Seq<BuildPlan> plans = new Seq<>();
    private static final Set<String> assignedPlans = new HashSet<>();
    private static final Set<String> currentPlans = new HashSet<>();
    private static int nextBuilder = 0;
    private static float updateTimer = 0f;
    private static final float updateInterval = 10f;
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
    }
    public static boolean isEnabled(){
        return enabled;
    }
    private static void installCommandButton(){
        if(Core.scene == null || Core.scene.root == null)return;
        if(buttonInstalled && commandTable != null && commandTable.parent != null)return;
        buttonInstalled = false;
        commandTable = findCommandTable(Core.scene.root);
        if(commandTable == null)return;
        commandTable.button("协同建造", Styles.clearTogglet, MLCoordinatedBuild::toggle)
                .size(155f, 48f)
                .checked(button -> enabled)
                .row();
        buttonInstalled = true;
    }
    private static Table findCommandTable(Group group){
        if(group == null)return null;
        for(Element element : group.getChildren()){
            if(element instanceof Table table && containsCommandButton(table))return table;
            if(element instanceof Group childGroup){
                Table found = findCommandTable(childGroup);
                if(found != null)return found;
            }
        }
        return null;
    }
    private static boolean containsCommandButton(Table table){
        for(Element child : table.getChildren()){
            if(child instanceof TextButton textButton){
                String text = textButton.getLabel().getText().toString();
                String command = Core.bundle.get("command");
                if(text.equals(command) || text.equals("@command"))return true;
            }
        }
        return false;
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
        if(builders.isEmpty())return;
        collectPlans();
        if(plans.isEmpty())return;
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
            if(unit != null && unit.isValid() && !unit.dead && unit.team == Vars.player.team() && unit.canBuild())return unit;
        }
        return null;
    }
    private static String planKey(BuildPlan plan){
        String blockId = plan.block == null ? "break" : String.valueOf(plan.block.id);
        return plan.x + ":" + plan.y + ":" + blockId + ":" + plan.rotation;
    }
}
