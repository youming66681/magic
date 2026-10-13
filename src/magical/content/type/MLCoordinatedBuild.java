package magical.content;

import arc.Core;
import arc.Events;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.ui.Button;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Log;
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
    private static ImageButton injectedButton;
    private static Button commandButton;
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
    public static void buildButton(Table table){
        table.button(new TextureRegionDrawable(Core.atlas.find("MLCoordinatedBuild")), MLCoordinatedBuild::toggle)
                .size(48f)
                .checked(button -> enabled);
    }
    private static void installCommandButton(){
        if(Core.scene == null || Core.scene.root == null)return;
        if(injectedButton != null && injectedButton.isAdded()){
            if(commandButton == null || !commandButton.isAdded()){
                injectedButton.remove();
                injectedButton = null;
                commandButton = null;
            }else{
                positionButton();
                return;
            }
        }
        commandButton = null;
        findCommandButton(Core.scene.root);
        if(commandButton == null)return;
        injectedButton = new ImageButton(Styles.clearTogglet);
        injectedButton.getImage().setDrawable(new TextureRegionDrawable(Core.atlas.find("MLCoordinatedBuild")));
        injectedButton.clicked(MLCoordinatedBuild::toggle);
        injectedButton.update(() -> {
            injectedButton.setChecked(enabled);
            positionButton();
        });
        commandButton.getParent().addChild(injectedButton);
        positionButton();
    }
    private static void positionButton(){
        if(injectedButton == null || commandButton == null)return;
        if(!injectedButton.isAdded() || !commandButton.isAdded())return;
        injectedButton.setVisible(commandButton.isVisible());
        injectedButton.setSize(commandButton.getWidth(), commandButton.getHeight());
        injectedButton.setPosition(commandButton.getX() + commandButton.getWidth() + 4f, commandButton.getY());
    }
    private static void findCommandButton(Group group){
        if(group == null)return;
        for(Element element : group.getChildren()){
            if(element instanceof Button button){
                if(button instanceof arc.scene.ui.TextButton textButton){
                    if(textButton.getText().toString().equals(Core.bundle.get("@command"))){
                        commandButton = button;
                        return;
                    }
                }
            }
            if(element instanceof Group child){
                findCommandButton(child);
                if(commandButton != null)return;
            }
        }
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
