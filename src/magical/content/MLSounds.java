package magical.content;

import magical.magic;
import arc.audio.Sound;
import mindustry.Vars;
import mindustry.mod.Mods;
import arc.Core;

public class MLSounds {
    public static Mods.LoadedMod ML;
    public static Sound explosionAfflict;
    public static Sound explosionCleroi;
    public static Sound shootSublimate;
    public static Sound shootForeshadow;
    public static Sound shootAlt;
    public static Sound loopSmelter;
    public static Sound boss1;
    public static Sound boss2


    public static void load() {
        explosionAfflict = loadSound("explosionAfflict.ogg");
        explosionCleroi = loadSound("explosionCleroi.ogg");
        shootSublimate = loadSound("shootSublimate.ogg");
        shootForeshadow = loadSound("shootForeshadow.ogg");
        shootAlt = loadSound("shootAlt.ogg");
        loopSmelter = loadSound("loopSmelter.ogg");
        boss1 = loadSound("boss1.ogg");
        boss2 = loadSound("boss2.ogg");
    }

    private static Sound loadSound(String name) {
        return new Sound(ML.root.child("sounds").child(name));
    }

    static {
        ML = Vars.mods.getMod(magic.class);
        explosionAfflict = new Sound();
        explosionCleroi = new Sound();
        shootSublimate = new Sound();
        shootForeshadow = new Sound();
        shootAlt = new Sound();
        loopSmelter = new Sound();
        boss1 = new Sound();
        boss2 = new Sound();
    }
}