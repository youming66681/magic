package magical.content;

import arc.Core;
import arc.audio.Music;

public class MLMusic {

    public static Music boss1;

    public static void load(){

        boss1 = Core.assets.get("boss1", Music.class);
    }
}