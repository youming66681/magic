package magical.content;

import arc.Events;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Unit;

public class BossMusicController {

    private static boolean playing = false;

    public static void init(){

        Events.run(EventType.Trigger.update, () -> {

            boolean bossAlive = false;

            for(Unit unit : Groups.unit){

                if(unit.isBoss()){
                    bossAlive = true;
                    break;
                }
            }

            if(bossAlive && !playing){

                playing = true;

                if(MLMusic.boss1 != null){

                    MLMusic.boss1.setLooping(true);

                    MLMusic.boss1.play();
                }
            }

            if(!bossAlive && playing){

                playing = false;

                if(MLMusic.boss1 != null){

                    MLMusic.boss1.stop();
                }
            }
        });
    }
}