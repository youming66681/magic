package magical.content;

import magical.magic;
import arc.audio.Sound;
import mindustry.Vars;
import mindustry.mod.Mods;

public class MLSounds {
    public static Mods.LoadedMod ML;
    public static Sound explosionAfflict;
    public static Sound explosionCleroi;

    public static void load() {
        explosionAfflict = loadSound("explosionAfflict.ogg");
        explosionCleroi = loadSound("explosionCleroi.mp3");
    }

    public static Sound loadSound(String name) {
        return new Sound(ML.root.child("sounds").child(name));
    }

    static {
        ML = Vars.mods.getMod(magic.class);
        explosionAfflict = new Sound();
        explosionCleroi = new Sound();
    }
}