package magical.content;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.type.*;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.geom.Vec2;
import mindustry.entities.Effect;

public class MLFx {
    public static final Rand rand = new rand();

    public static Effect smallElectricDetonation = new Effect(22f, e -> {
        Draw.color(Color.valueOf("97B5EDFF"));

        e.scaled(6, i -> {
            Lines.stroke(3f * i.fout());
            Lines.circle(e.x, e.y, 3f + i.fin() * 15f);
        });

        Draw.color(Color.valueOf("97B5EDFF"));

        rand(e.id, 6, 2f + 20f * e.finpow(), (x, y) -> {
            Fill.circle(e.x + x, e.y + y, e.fout() * 3f + 0.5f);
        });

        Draw.color(Color.valueOf("97B5EDFF"));

        rand(e.id + 1, 4, 1f + 20f * e.finpow(), (x, y) -> {
            lineAngle(e.x + x, e.y + y, mathf.angle(x, y), 1f + e.fout() * 3f);
        });

        Drawf.light(e.x, e.y, 45f, Color.valueOf("97B5EDFF"), 0.8f * e.fout());
    });

    public static void load(){}
}