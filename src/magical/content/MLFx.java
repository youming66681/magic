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

    public static void tri(float x, float y, float width, float length, float angle) {
        float wx = Angles.trnsx(angle + 90, width), wy = Angles.trnsy(angle + 90, width);
        Fill.tri(x + wx, y + wy, x - wx, y - wy, Angles.trnsx(angle, length) + x, Angles.trnsy(angle, length) + y);
    }

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
            Draw.reset();
        });
    }
    public static Effect Slash(Color colorExternal, Color colorInternal, float lifetime, float range) {
        return new Effect(lifetime, range * 2, e -> {
            Angles.randLenVectors(e.id, (int) Mathf.clamp(range / 8, 4, 18), range / 8, range * (1 + e.fout(Interp.pow2OutInverse)) / 2f, (x, y) -> {
                float angle = Mathf.angle(x, y);
                float width = e.foutpowdown() * rand.random(range / 8, range / 4) / 2 * e.fout();

                rand.setSeed(e.id);
                float length = rand.random(range / 2, range * 1f) * e.fout();

                Draw.color(colorExternal);
                this.tri(e.x + x, e.y + y, width, range / 3 * e.fout(Interp.pow2In), angle - 180);
                this.tri(e.x + x, e.y + y, width, length, angle);

                this.color(colorInternal);

                width *= e.fout();

                this.tri(e.x + x, e.y + y, width / 2, range / 3 * e.fout(Interp.pow2In) * 0.9f * e.fout(), angle - 180);
                this.tri(e.x + x, e.y + y, width / 2, length / 1.5f * e.fout(), angle);
            });
        });
    }
}