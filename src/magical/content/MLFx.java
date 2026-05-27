package magical.content;

import arc.math.Interp;
import arc.math.Mathf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.geom.Vec2;
import arc.math.Rand;
import arc.math.Angles;
import arc.func.Cons2;
import mindustry.gen.*;
import mindustry.entities.Effect;
import mindustry.type.*;
import mindustry.content.*;

public class MLFx {
    public static Effect smallElectricDetonation;

    public static final Rand rand = new Rand();
    Vec2 temp = new Vec2();

    public static void load() {
         smallElectricDetonation = new Effect(30f, (e) -> {
            Draw.color(Color.valueOf("97B5EDFF"), e.color, e.fin() + 0.4F);
            e.scaled(6, (i) -> {
                Lines.stroke(4f * i.foutpow());
                Lines.circle(i.x, i.y, i.fin(Interp.circleOut) * 10f * 10F);
            });
            Angles.randLenVectors((long) e.id, 1, 10f * e.finpow(), (x, y) -> {
                Fill.circle(e.x, e.y, 8f * e.fout() * 4f);
            });
             Angles.randLenVectors((long)e.id, 6, e.finpow() * 10f, (x, y) -> {
                 float ang = Mathf.angle(x, y);
                 Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * (float)rand.random(4f, 8f) + 2F);
             });
        });
    }
}