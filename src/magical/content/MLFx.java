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
    public static Effect Explosion1;
    public static Effect Explosion2;

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
            Angles.randLenVectors((long) e.id, 6, e.finpow() * 20f, (x, y) -> {
                float ang = Mathf.angle(x, y);
                Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * (float) rand.random(4f, 8f) + 2F);
            });
        });
        squareWaveRot = new Effect(14, 40f, e -> {
            rand.setSeed(e.id);
            Draw.color(Color.valueOf("FEEBB3FF"), e.color, rand.random(0.8f, 1.5f) * e.fin());
            Lines.stroke(rand.random(0.6f, 0.9f) + e.fout() * 2);
            float rot = rand.random(45f, 180f) * e.fin();
            float rotation = rand.random(0f, 1f) > 0.5f ? rot : -rot;
            Lines.square(e.x, e.y, e.fin() * rand.random(4f, 10f) + 4f, e.rotation + rand.random(360f) + rotation);
            Drawf.light(e.x, e.y, 21f, e.color, e.fout() * 0.7f);
        });
        beamEffect = new Effect(30f, e -> {
            Draw.color(Color.valueOf("FEEBB3FF"), Color.valueOf("FEEBB3FF"), e.fin());
            Lines.stroke(Mathf.lerp(9f, 0f, e.fin()));
            Lines.lineAngle(e.x, e.y, e.rotation, 20f);
        });
        Explosion1 = new Effect(30f, e -> {
            Draw.color(Color.white, Color.valueOf("ffb347"), e.fin());
            Fill.circle(e.x, e.y, 6f * e.fout());

            Draw.color(Color.valueOf("ffb347"));
            Draw.alpha(e.fout());
            Lines.stroke(2f * e.fout());
            Lines.circle(e.x, e.y, 32f * e.fin()); // 24 = 3格

            Draw.color(Color.valueOf("ff9248"));
            Angles.randLenVectors(e.id, 18, 32f * e.fin(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, 1.6f * e.fout());
            });

            Draw.color(Color.gray);
            Angles.randLenVectors(e.id + 1, 10, 24f * e.fin(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, 2.8f * e.fout());
            });
        });
        Explosion2 = new Effect(35f, 80f, e -> {

            Draw.color(Color.white, Color.valueOf("ffb35c"), e.fin());
            Fill.circle(e.x, e.y, 3f + e.fin() * 10f);

            Draw.color(Color.valueOf("ff9b42"));
            Lines.stroke(2.8f * e.fout());
            Lines.circle(e.x, e.y, 18f * e.fin());

            Draw.color(Color.valueOf("ffb35c"), Color.valueOf("ff6a3d"), e.fin());
            Angles.randLenVectors(e.id, 18, 24f * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, 1.8f * e.fout()
                );
            });

            Draw.color(Color.gray);
            Angles.randLenVectors(e.id + 1, 12, 20f * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, 2.5f * e.fout()
                );
            });

            Draw.color(Color.valueOf("ffd37f"));
            Angles.randLenVectors(e.id + 2, 10, 28f * e.finpow(), (x, y) -> {
                Lines.stroke(1.2f * e.fout());
                Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4f * e.fout()
                );
            });
        });
    }
    public static Effect Slash(Color colorSlash, float len, float width){
        return new Effect(30f, e -> {

            Draw.color(colorSlash);
            Drawf.tri(e.x, e.y, width * e.fout(), len, e.rotation);
            Drawf.tri(e.x, e.y, width * e.fout(), len, e.rotation + 180f);

        });
    }
}