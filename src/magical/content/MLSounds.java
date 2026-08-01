package magical.content;

import magical.magic;
import arc.audio.Sound;
import mindustry.Vars;
import mindustry.mod.Mods;
import arc.Core;
import arc.util.Log;

public class MLSounds {
    public static Mods.LoadedMod ML;
    public static Sound explosionAfflict;
    public static Sound explosionCleroi;
    public static Sound shootSublimate;
    public static Sound shootForeshadow;
    public static Sound shootAlt;
    public static Sound loopSmelter;
    public static Sound shootArtillery;
    public static Sound explosion;
    public static Sound shootArtillerySmall;
    public static Sound missile;
    public static Sound plasmadrop;
    public static Sound laser;
    public static Sound missileLaunch;
    public static Sound lasercharge2;
    public static Sound shootFuse;
    public static Sound plasmaboom;
    public static Sound pew;
    public static Sound pao;
    public static Sound beam;
    public static Sound JG;
    public static Sound spark;
    public static Sound loopTech;
    public static Sound shootArtillerySap;
    public static Sound largeCannon;
    public static Sound laserblast;
    public static Sound lasercharge;

    public static void load() {
        if (Vars.headless) return;

        explosionAfflict = loadSoundSafe("explosionAfflict.ogg");
        explosionCleroi = loadSoundSafe("explosionCleroi.ogg");
        shootSublimate = loadSoundSafe("shootSublimate.ogg");
        shootForeshadow = loadSoundSafe("shootForeshadow.ogg");
        shootAlt = loadSoundSafe("shootAlt.ogg");
        loopSmelter = loadSoundSafe("loopSmelter.ogg");
        shootArtillery = loadSoundSafe("shootArtillery.ogg");
        explosion = loadSoundSafe("explosion.ogg");
        shootArtillerySmall = loadSoundSafe("shootArtillerySmall.ogg");
        missile = loadSoundSafe("missile.ogg");
        plasmadrop = loadSoundSafe("plasmadrop.ogg");
        laser = loadSoundSafe("laser.ogg");
        missileLaunch = loadSoundSafe("missileLaunch.ogg");
        lasercharge2 = loadSoundSafe("lasercharge2.ogg");
        shootFuse = loadSoundSafe("shootFuse.ogg");
        plasmaboom = loadSoundSafe("plasmaboom.ogg");
        pew = loadSoundSafe("pew.ogg");
        pao = loadSoundSafe("pao.ogg");
        beam = loadSoundSafe("beam.ogg");
        JG = loadSoundSafe("JG.ogg");
        spark = loadSoundSafe("spark.ogg");
        loopTech = loadSoundSafe("loopTech.ogg");
        shootArtillerySap = loadSoundSafe("shootArtillerySap.ogg");
        largeCannon = loadSoundSafe("largeCannon.ogg");
        laserblast = loadSoundSafe("laserblast.ogg");
        lasercharge = loadSoundSafe("lasercharge.ogg");
    }

    private static Sound loadSoundSafe(String name) {
        try {
            return new Sound(ML.root.child("sounds").child(name));
        } catch (Throwable e) {
            Log.warn("Failed to load custom sound: @", name, e);
            return new Sound();   // 使用空音效避免崩溃
        }
    }
    static {
        ML = Vars.mods.getMod(magic.class);
        explosionAfflict    = new Sound();
        explosionCleroi     = new Sound();
        shootSublimate      = new Sound();
        shootForeshadow     = new Sound();
        shootAlt            = new Sound();
        loopSmelter         = new Sound();
        shootArtillery      = new Sound();
        explosion           = new Sound();
        shootArtillerySmall = new Sound();
        missile             = new Sound();
        plasmadrop          = new Sound();
        laser               = new Sound();
        missileLaunch       = new Sound();
        lasercharge2        = new Sound();
        shootFuse           = new Sound();
        plasmaboom          = new Sound();
        pew                 = new Sound();
        pao                 = new Sound();
        beam                = new Sound();
        JG                  = new Sound();
        spark               = new Sound();
        loopTech            = new Sound();
        shootArtillerySap   = new Sound();
        largeCannon         = new Sound();
        laserblast          = new Sound();
        lasercharge         = new Sound();
    }
}