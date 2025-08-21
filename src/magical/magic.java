// In src/magical/magic.java (after renaming the file to magic.java)
package magical;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import mindustry.Vars;
import magica.content.MLItems;

public class magic extends Mod {
    public static String ModName = "magic";
    public static Mods.LoadedMod mod;

    @Override
    public void loadContent() {
        mod = Vars.mods.getMod(this.getClass());
        MLItems.load();
    }
}