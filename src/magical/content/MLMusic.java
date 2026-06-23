package magical.content;

import magical.magic;
import arc.audio.Sound;
import mindustry.Vars;
import mindustry.mod.Mods;
import arc.Core;
import arc.audio.Music;

public class MLMusic {
    public static Mods.LoadedMod ML;
    public static Music boss1;
    public static Music boss2;

    public static void load() {
        boss1 = loadMusic("boss1.ogg");
        boss2 = loadMusic("boss1.ogg");
    }

    private static Music loadMusic(String name) {
        return new Music(ML.root.child("music").child(name));
    }

    static {
        ML = Vars.mods.getMod(magic.class);
        boss1 = new Music();
        boss2 = new Music();
    }
}