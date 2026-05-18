package magical.content;

import arc.audio.Sound;
import mindustry.Vars;
import mindustry.mod.Mods;

public class EUSounds {
    public static Mods.LoadedMod ML;
    public static Sound explosionAfflict;
    public static Sound ciallo;

public static void load() {
    prismLoop = loadSound("explosionAfflict.ogg");
    ciallo = loadSound("explosionCleroi.ogg");
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