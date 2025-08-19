// In src/magical/magic.java (after renaming the file to magic.java)
package magical;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;

public class magic extends Mod {
    public static String magic = "magic";

    @Override
    public void loadContent() {
        MLItems.load();
    }
}