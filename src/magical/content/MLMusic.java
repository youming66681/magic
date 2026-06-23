package magical.content;

import arc.Core;
import arc.audio.Music;

public class MLMusic{

    public static Music boss1;
    public static Music boss2;

    public static void load(){
        boss1 = Core.assets.load("music/boss1.ogg", Music.class).loaded;
        boss2 = Core.assets.load("music/boss1.ogg", Music.class).loaded;
    }
}