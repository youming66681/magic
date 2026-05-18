package magical.content;

import mindustry.mod.Mod;
import mindustry.Vars;
import mindustry.audio.SoundControl;

public class MLSounds {
    // 自定义声音变量
    public static Sound explosionAfflict;
    public static Sound explosionCleroi;

    // 加载方法
    public static void load(){
        Mod mod = Vars.mods.getMod(magical.Magic.class);
        explosionAfflict = mod.sound("explosionAfflict");
        explosionCleroi = mod.sound("explosionCleroi");
    }
}