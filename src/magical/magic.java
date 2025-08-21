// In src/magical/magic.java (after renaming the file to magic.java)
package magical;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import mindustry.Vars;
import magical.content.MLItems;
import magical.content.MLSounds;

public class magic extends Mod {
    public static final String ModName = "magic-industry";
    public static Mods.LoadedMod mod;
    public static String name(String add){
        return ModName + "-" + add;
    }
    public magic() {}
    @Override
    public void loadContent() {
        mod = Vars.mods.getMod(this.getClass());
        MLItems.load();
        MLSounds.load();
    }
}