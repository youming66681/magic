// In src/magical/magic.java (after renaming the file to magic.java)
package magical;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import mindustry.Vars;
import arc.audio.Sound;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLTechTree;
import magical.content.MLPlanets;
//import magical.content.MLSounds;
import magical.content.MLFx;

public class magic extends Mod {

    public static Sound explosionAfflict;
    public static Sound explosionCleroi;

    public static final String ModName = "magic-industry";
    public static Mods.LoadedMod mod;
    public magic() {}
    public static String name(String add) {
        return "modName" + add;
    }
    @Override
    public void loadContent() {
        mod = Vars.mods.getMod(this.getClass());

        explosionAfflict = loadSound("sounds/explosionAfflict.ogg");
        explosionCleroi = loadSound("sounds/explosionCleroi.ogg");
        //MLSounds.load();
        MLFx.load();
        MLItems.load();
        MLBlocks.load();
        MLPlanets.load();
        MLTechTree.load();
        MLUnitTypes.load();
     }
    private Sound loadSound(String path) {
        Sound sound = new Sound();
        Vars.tree.loadSound(path, sound);
        return sound;
    }
}