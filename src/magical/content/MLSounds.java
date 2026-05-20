package magical.content;

import mindustry.audio.Sound;
import mindustry.type.*;
import mindustry.content.*;

public class MLSounds {
    public static Sound explosionAfflict;
    public static Sound explosionCleroi;

    public static void load(){
        explosionAfflict = new Sound("explosionAfflict");
        explosionCleroi = new Sound("explosionCleroi");
    }
}