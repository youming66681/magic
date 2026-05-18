package magical.content;

import arc.audio.Sound;
import mindustry.Vars;

public class MLSounds {
    public static Sound explosionAfflict;
    public static Sound explosionCleroi;

    public static void load(){
        Mod mod = Vars.mods.getMod(magical.Magic.class);
        explosionAfflict = mod.sound("explosionAfflict");
        explosionCleroi = mod.sound("explosionCleroi");
    }
}