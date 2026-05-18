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
    public static final Vec2 v = new Vec2();

    public static Effect smallElectricDetonation = new Effect(30, e -> {
        Draw.color(Color.valueOf("#FEEBB3FF"), e.color, e.finpow());
        for(int i = 0; i < 8; i++){
        randLenVectors(e.id, i, 2f + e.fin() * 4f, (x, y) -> {
            Fill.square(e.x + x, e.y + y, 0.5f + e.fout() * 2f, 45);
             }
        });
    });
    public static void load(){}
}