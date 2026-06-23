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
import arc.audio.Sound;
package magical.content;

import magical.content.BossMusicController;
import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLTechTree;
import magical.content.MLPlanets;
import magical.content.MLSounds;
import magical.content.MLFx;

public class magic extends Mod {
    public static Mods.LoadedMod ML;
    public static final String ModName = "magic-industry";
    public static Mods.LoadedMod mod;
    public magic() {}
    public static String name(String add) {
        return "modName" + add;
    }
    @Override
    public void loadContent() {
        mod = Vars.mods.getMod(this.getClass());

        MLMusic.load();
        MLSounds.load();
        MLFx.load();
        MLItems.load();
        MLBlocks.load();
        MLPlanets.load();
        MLTechTree.load();
        MLUnitTypes.load();
     }
    @Override
    public void init(){

        BossMusicController.init();
    }
}