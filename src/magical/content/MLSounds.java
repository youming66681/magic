package magical.content;

import arc.audio.Sound;
import mindustry.Vars;
import mindustry.mod.Mod;

public class MLSounds {
    public static Sound explosionAfflict;
    public static Sound explosionCleroi;

    public static void alertLoop() {
        if (!Vars.headless) {
            Vars.control.sound.loop(alert2, 2.0F);
        }

    }

    public static void load() {
        try {
            for(Field field : MLSounds.class.getFields()) {
                if (field.getType().equals(Sound.class)) {
                    field.set((Object)null, loadSound(field.getName()));
                }
            }
        } catch (IllegalAccessException e) {
            Log.err(e);
        }

    }

    private static Sound loadSound(String soundName) {
        if (Vars.headless) {
            return new Sound();
        } else {
            String path = "sounds/" + soundName;
            String filePath = Vars.tree.get(path + ".ogg").exists() ? path + ".ogg" : path + ".mp3";
            Sound sound = new Sound();
            AssetDescriptor<?> desc = Core.assets.load(filePath, Sound.class, new SoundLoader.SoundParameter(sound));
            desc.errored = Throwable::printStackTrace;
            return sound;
        }
    }
}