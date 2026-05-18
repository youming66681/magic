// In src/magical/magic.java (after renaming the file to magic.java)
package magical;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import mindustry.Vars;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLTechTree;
import magical.content.MLPlanets;
import magical.content.MLSounds;
import magical.content.MLFx;

public class magic extends Mod {

    public static final String ModName = "magic-industry";
    public static Mods.LoadedMod mod;
    public magic() {}
    public static String name(String add) {
        return "modName" + add;
    }
    @Override
    public void loadContent() {
        mod = Vars.mods.getMod(this.getClass());

        MLSounds.load();
        MLFx.load();
        MLItems.load();
        MLBlocks.load();
        MLPlanets.load();
        MLTechTree.load();
        MLUnitTypes.load();
     }
}