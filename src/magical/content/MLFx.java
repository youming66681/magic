package magical.content;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.type.*;

public class MLFx {
    public static final Rand rand = new Rand();

    public static Effect smallElectricDetonation = new Effect(22, e -> {
        color(Color.valueOf("FEEBB3FF"));

        e.scaled(6, i -> {
            stroke(3f * i.fout());
            Lines.circle(e.x, e.y, 3f + i.fin() * 15f);
        });

        color(Color.valueOf("FEEBB3FF"));

        randLenVectors(e.id, 6, 2f + 20f * e.finpow(), (x, y) -> {
            Fill.circle(e.x + x, e.y + y, e.fout() * 3f + 0.5f);
        });

        color(Color.valueOf("FEEBB3FF"));

        randLenVectors(e.id + 1, 4, 1f + 20f * e.finpow(), (x, y) -> {
            lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1f + e.fout() * 3f);
        });

        Drawf.light(e.x, e.y, 45f, Color.valueOf("FEEBB3FF"), 0.8f * e.fout());
    }),

    public static void load(){}
}