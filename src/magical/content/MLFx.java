package magical.content;

import arc.math.Mathf;
import arc.math.geom.Vec2;

public class MLFx {
    public static final Rand rand = new Rand();
    public static final Vec2 v = new Vec2();

    public static void load(){}

    public static Effect smallElectricDetonation = new Effect(30, e -> {
        Mathf.randLenVectors(e.id, 8, 2f + e.fin() * 4f, (x, y) -> {
            Fill.square(e.x + x, e.y + y, 0.5f + e.fout() * 2f, 45);
            Draw.color(Color.valueOf("#FEEBB3FF"), e.color, e.finpow());
        });
    });
}