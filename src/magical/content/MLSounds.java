package magical.content;

import mindustry.mod.Mod;
import mindustry.gen.Sounds;
import mindustry.Vars;
import mindustry.audio.SoundControl;

public class MLSounds {
    public static Mods.LoadedMod ML;
    public static Sound explosionAfflict;
    public static Sound explosionCleroi;

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