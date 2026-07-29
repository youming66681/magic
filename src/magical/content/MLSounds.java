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

    public static void load() {
        if (Vars.headless) return;
        explosionAfflict = loadSoundSafe("explosionAfflict.ogg", Sounds.none);
        explosionCleroi = loadSoundSafe("explosionCleroi.ogg", Sounds.none);
        shootSublimate = loadSoundSafe("shootSublimate.ogg", Sounds.none);
        shootForeshadow = loadSoundSafe("shootForeshadow.ogg", Sounds.none);
        shootAlt = loadSoundSafe("shootAlt.ogg", Sounds.none);
        loopSmelter = loadSoundSafe("loopSmelter.ogg", Sounds.none);
        shootArtillery = loadSoundSafe("shootArtillery.ogg", Sounds.none);
        explosion = loadSoundSafe("explosion.ogg", Sounds.none);
        shootArtillerySmall = loadSoundSafe("shootArtillerySmall.ogg", Sounds.none);
        missile = loadSoundSafe("missile.ogg", Sounds.none);
        plasmadrop = loadSoundSafe("plasmadrop.ogg", Sounds.none);
        laser = loadSoundSafe("laser.ogg", Sounds.none);
        missileLaunch = loadSoundSafe("missileLaunch.ogg", Sounds.none);
        lasercharge2 = loadSoundSafe("lasercharge2.ogg", Sounds.none);
        shootFuse = loadSoundSafe("shootFuse.ogg", Sounds.none);
        plasmaboom = loadSoundSafe("plasmaboom.ogg", Sounds.none);
        pew = loadSoundSafe("pew.ogg", Sounds.none);
        pao = loadSoundSafe("pao.ogg", Sounds.none);
        beam = loadSoundSafe("beam.ogg", Sounds.none);
        JG = loadSoundSafe("JG.ogg", Sounds.none);
        spark = loadSoundSafe("spark.ogg", Sounds.none);
        loopTech = loadSoundSafe("loopTech.ogg", Sounds.none);
    }
    // 添加这个私有辅助方法
    private static Sound loadSoundSafe(String name, Sound fallback) {
        try {
            return new Sound(ML.root.child("sounds").child(name));
        } catch (Exception e) {
            // 声音加载失败，使用安全回退
            return fallback;
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
    }
}