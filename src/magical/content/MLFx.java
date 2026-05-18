package magical.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import mindustry.entities.Effect;

public class MLFx {
    public static final Rand rand = new Rand();

    public static Effect smallElectricDetonation = new Effect(30, e -> {
        Draw.color(Color.valueOf("#FEEBB3FF"), e.color, e.finpow());

        Vec2 temp = new Vec2();
        for(int i = 0; i < 8; i++){
            Mathf.randLenVectors(e.id, i, 2f + e.fin() * 4f, temp);
            Fill.square(e.x + temp.x, e.y + temp.y, 0.5f + e.fout() * 2f, 45);
        }
    });

    public static void load(){}
}