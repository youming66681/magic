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
import magical.blocks.ABaseCore;
//import magical.content.MLSounds;

public class magic extends Mod {
    public static final ABaseCore aBaseCore = new ABaseCore();

    public static final String ModName = "magic-industry";
    public static Mods.LoadedMod mod;
    public magic() {}
    @Override
    public void loadContent() {
        mod = Vars.mods.getMod(this.getClass());

        //MLSounds.load();
        MLBlocks.load();
        MLPlanets.load();
        MLTechTree.load();
        MLUnitTypes.load();
     }
}