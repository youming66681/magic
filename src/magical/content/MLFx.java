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
            float angle = Mathf.random() * Mathf.PI * 2f;
            float len = 2f + e.fin() * 4f;
            temp.set(Mathf.cos(angle) * len, Mathf.sin(angle) * len);
            Fill.square(e.x + temp.x, e.y + temp.y, 0.5f + e.fout() * 2f, 45);
        }

        Draw.reset();
    });

    public static void load(){}
}