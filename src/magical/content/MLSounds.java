package magical.content;

import arc.audio.Sound;
import mindustry.Vars;
import mindustry.mod.Mod;

public class MLSounds {
    public static Sound explosionAfflict;
    public static Sound explosionCleroi;

    public static void load(){
        Mod mod = Vars.mods.getMod(magical.magic.class);
        explosionAfflict = mod.sound("explosionAfflict");
        explosionCleroi = mod.sound("explosionCleroi");
    }
}