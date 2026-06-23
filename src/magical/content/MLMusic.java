package magical.content;

import arc.Core;
import arc.audio.Music;

public class MLMusic {

    public static Music boss1;

    public static void load(){

        Core.assets.load(
                "music/boss1.ogg",
                Music.class
        ).loaded = music -> boss1 = music;
    }