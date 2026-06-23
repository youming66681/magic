package magical.content;

import arc.Events;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.type.UnitType;

public class BossMusicController {

    private static boolean playing = false;
    private static float fade = 1f;

    public static void init(){

        Events.run(EventType.Trigger.update, () -> {

            boolean bossAlive = false;

            for(Unit u : Groups.unit){

                if(isBoss(u)){
                    bossAlive = true;
                    break;
                }
            }

            if(bossAlive && !playing){

                playing = true;
                fade = 1f;

                if(MLMusic.boss1 != null){
                    MLMusic.boss1.setLooping(true);
                    MLMusic.boss1.setVolume(0f);
                    MLMusic.boss1.play();
                }
            }

            if(playing && MLMusic.boss1 != null){

                if(bossAlive){

                    fade = Mathf.lerp(fade, 1f, 0.05f);
                }else{

                    fade = Mathf.lerp(fade, 0f, 0.05f);
                }

                MLMusic.boss1.setVolume(fade);

                if(!bossAlive && fade < 0.01f){

                    MLMusic.boss1.stop();
                    playing = false;
                }
            }
        });
    }

    private static boolean isBoss(Unit u){

        if(u == null) return false;

        UnitType t = u.type;

        if(t == null) return false;

        // 1. 官方 Wave Boss
        if (u.isBoss()) return true;

    }
}