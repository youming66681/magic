package magic;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;

public class magic extends Mod{
    public static String ModName = "magic";
    public SFireMod() {}
    @Override
    public void loadContent(){
        MLItems.load();
    }
}