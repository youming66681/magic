package magical.content;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.type.*;
import arc.graphics.Color;
import arc.math.Interp;
import mindustry.type.StatusEffect;
import mindustry.entities.effect.ParticleEffect;

import static mindustry.Vars.*;

public class MLStatusEffects{
    public static StatusEffect blitz;

    public static void load(){
        blitz = new StatusEffect("闪击"){{

            damageMultiplier = 1.2f;
            speedMultiplier = 2.4f;
            reloadMultiplier = 1.2f;
            healthMultiplier = 0.8f;
            effectChance = 0.4f;
            effect = new ParticleEffect(){{
                particles = 1;
                length = 40f;
                lifetime = 60f;
                spin = 4f;
                interp = Interp.fastSlow;
                region = "magic-十字星";
                sizeFrom = 0f;
                sizeTo = 4f;
                colorFrom = Color.valueOf("FEEBB3FF");
                colorTo = Color.valueOf("FEEBB3FF");
                cone = 360f;
            }};
        }};
    }
    }
}
