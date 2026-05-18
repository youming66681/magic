package magical.content;

import arc.audio.Sound;

public class MLSounds {
    public static Sound explosionAfflict;
    public static Sound explosionCleroi;

    public static void load(){
        explosionAfflict = Vars.tree.get("sounds/explosionAfflict.ogg", Sound.class);
        explosionCleroi = Vars.tree.get("sounds/explosionCleroi.ogg", Sound.class);
    }
}