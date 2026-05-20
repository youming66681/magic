package magical.content;

import arc.audio.Sound;
import mindustry.type.*;
import mindustry.content.*;
import arc.files.Fi;
import arc.Core;

public class MLSounds {
    public static Sound explosionAfflict;
    public static Sound explosionCleroi;

    public static void load(){
        explosionAfflict = new Sound(("sounds/explosionAfflict"));
        explosionCleroi = new Sound(("sounds/explosionCleroi"));
    }
}