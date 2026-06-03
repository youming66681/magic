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
import mindustry.graphics.Drawf;

public class MLFx {
    public static Effect smallElectricDetonation;
    public static Effect squareWaveRot;
    public static Effect beamEffect;

    public static final Rand rand = new Rand();
    Vec2 temp = new Vec2();

    public static void load() {
         smallElectricDetonation = new Effect(30f, (e) -> {
            Draw.color(Color.valueOf("97B5EDFF"), e.color, e.fin() + 0.4F);
            e.scaled(6, (i) -> {
                Lines.stroke(6f * i.foutpow());
                Lines.circle(i.x, i.y, i.fin(Interp.circleOut) * 3f * 6F);
            });
            Angles.randLenVectors((long) e.id, 1, 8f * e.finpow(), (x, y) -> {
                Fill.circle(e.x, e.y, 2f * e.fout() * 4f);
            });
             Angles.randLenVectors((long)e.id, 6, e.finpow() * 20f, (x, y) -> {
                 float ang = Mathf.angle(x, y);
                 Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * (float)rand.random(4f, 8f) + 2F);
             });
        });
        squareWaveRot =  new Effect(14, 40f, e -> {
            rand.setSeed(e.id);
            Draw.color(Color.valueOf("FEEBB3FF"), e.color, rand.random(0.8f, 1.5f) * e.fin());
            Lines.stroke(rand.random(0.4f, 0.8f) + e.fout() * 2);
            float rot = rand.random(45f, 180f) * e.fin();
            float rotation = rand.random(0f, 1f) > 0.5f ? rot : -rot;
            Lines.square(e.x, e.y, e.fin() * rand.random(4f, 10f) + 4f, e.rotation + rand.random(360f) + rotation);
            Drawf.light(e.x, e.y, 14f, e.color, e.fout() * 0.7f);
        });
        beamEffect = new Effect(30f, e -> {
            Draw.color(Color.valueOf("FEEBB3FF"), Color.valueOf("FEEBB3FF"), e.fin());
            Lines.stroke(Mathf.lerp(9f, 0f, e.fin()));
            float len = 15f;
            Lines.lineAngle(e.x, e.y, e.rotation, len);
            Draw.reset();
        });
    }
}