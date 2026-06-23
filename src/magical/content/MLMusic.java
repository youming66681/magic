package magical.content;

import arc.audio.Music;
import arc.files.Fi;
import mindustry.Vars;
import mindustry.mod.Mods;

public class MLMusic {

    public static Music boss1;
    public static Music boss2;

    private static Mods.LoadedMod mod;

    public static void load(){

        mod = Vars.mods.getMod(magical.magic.class);

        boss1 = loadMusic("boss1.ogg");
        boss2 = loadMusic("boss1.ogg");
    }

    // ===== 核心：完全绕过 asset system =====
    private static Music loadMusic(String file){

        try{

            Fi fi = mod.root.child("assets").child("sounds").child(file);

            Music music = new Music(fi);

            return music;

        }catch(Exception e){

            e.printStackTrace();

            return null;
        }
    }
}