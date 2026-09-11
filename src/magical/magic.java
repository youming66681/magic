// In src/magical/magic.java (after renaming the file to magic.java)
package magical;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import mindustry.Vars;
import mindustry.mod.Mod;
import mindustry.mod.Mods;
import mindustry.gen.Unit;
import arc.audio.Sound;
import arc.math.geom.Vec2;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLTechTree;
import magical.content.MLPlanets;
import magical.content.MLSounds;
import magical.content.MLFx;
import magical.content.MLStatusEffects;
import magical.content.MLSpawnUnits;
import magical.content.MLSectorPresets;
import magical.content.MLLiquids;
import magical.content.UnitFocusEvent;

public class magic extends Mod {
    public static Mods.LoadedMod ML;
    public static final String ModName = "magic";
    public static Mods.LoadedMod mod;
    private float focusTime;
    public magic(){
        Events.on(UnitFocusEvent.class,e -> {
            if(Vars.headless)return;
            if(focusTime > e.time)return;
            focusTime = e.time;
            if(e.message != null && !e.message.isEmpty()){
                Call.infoMessage(e.message);
            }
            Vars.ui.hudGroup.visible = false;
            Vars.control.input.panCamera(
                    new Vec2(e.x,e.y)
            );
            Timer.schedule(() -> {
                Vars.ui.hudGroup.visible = true;
                focusTime = 0f;
            },e.time / 60f);
        });
    }
    public static String name(String add) {
        return ModName + "-" + add;
    }
    @Override
    public void loadContent() {
        mod = Vars.mods.getMod(this.getClass());

        MLStatusEffects.load();
        MLSounds.load();
        MLFx.load();
        MLLiquids.load();
        MLItems.load();
        MLPlanets.load();
        MLSectorPresets.load();
        MLUnitTypes.load();
        MLSpawnUnits.load();
        MLBlocks.load();
        MLTechTree.load();
     }
}