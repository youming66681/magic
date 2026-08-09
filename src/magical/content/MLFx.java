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
import mindustry.graphics.Pal;

public class MLFx {
    public static Effect smallElectricDetonation;
    public static Effect squareWaveRot;
    public static Effect beamEffect;
    public static Effect Explosion1;
    public static Effect Explosion2;
    public static Effect Explosion3;
    public static Effect smallEnergyBlast;
    public static Effect smallTeleport;
    public static Effect energyMine;
    public static Effect hugeEnergyExplosion;

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
            Fill.circle(e.x, e.y, 6f * e.foutpow());

            Draw.color(Color.valueOf("ffb347"));
            Draw.alpha(e.fout());
            Lines.stroke(2f * e.foutpow());
            Lines.circle(e.x, e.y, 32f * e.finpow());

            Draw.color(Color.valueOf("ff9248"));
            Angles.randLenVectors(e.id, 18, 32f * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, 1.6f * e.foutpow());
            });

            Draw.color(Color.gray);
            Angles.randLenVectors(e.id + 1, 10, 24f * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, 2.8f * e.foutpow());
            });
        });
        Explosion2 = new Effect(40f, e -> {

            Draw.color(Color.valueOf("ff9b42"));
            Lines.stroke(2.8f * e.foutpow());
            Lines.circle(e.x, e.y, 18f * e.finpow());

            Draw.color(Color.valueOf("ffb35c"), Color.valueOf("ff6a3d"), e.fin());
            Angles.randLenVectors(e.id, 18, 24f * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, 1.8f * e.foutpow()
                );
            });

            Draw.color(Color.gray);
            Angles.randLenVectors(e.id + 1, 12, 20f * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, 2.5f * e.foutpow()
                );
            });

            Draw.color(Color.valueOf("ffd37f"));
            Angles.randLenVectors(e.id + 2, 10, 28f * e.finpow(), (x, y) -> {
                Lines.stroke(1.2f * e.foutpow());
                Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4f * e.foutpow()
                );
            });
        });
        Explosion3 = new Effect(50f, e -> {

            Draw.color(Pal.lightOrange, Color.white, e.fin());
            Lines.stroke(3f * e.fout());
            Lines.circle(e.x, e.y, 32f * e.finpow());

            Draw.color(Pal.lighterOrange, Pal.lightOrange, e.finpow());
            Angles.randLenVectors(e.id, 18, 30f * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, 3f * e.foutpow());
            });

            Draw.color(Color.white, Pal.lightOrange, e.finpow());
            Angles.randLenVectors(e.id + 1, 20, 36f * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, 1.3f * e.foutpow());
            });

            Draw.color(Pal.gray);
            Angles.randLenVectors(e.id + 2, 12, 26f * e.finpow(), (x, y) -> {
                Fill.square(e.x + x, e.y + y, 1.5f * e.foutpow(), Mathf.randomSeed((long) (e.id + x + y), 360f)
                );
            });

            Draw.color(Color.white, Pal.lightOrange, e.fin());
            Lines.stroke(2f * e.foutpow());
            Angles.randLenVectors(e.id + 3, 10, 34f * e.finpow(), (x, y) -> {
                Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 5f * e.foutpow()
                );
            });

            Draw.reset();
        });
        smallEnergyBlast = new Effect(30f, e -> {
            float fin = e.finpow();
            float fout = e.foutpow();
            Color core = Color.valueOf("FEEBB3FF");
            Color glow = Color.valueOf("FFD37FFF");
            Draw.color(glow);
            Lines.stroke(2f * fout);
            Lines.circle(
                    e.x,
                    e.y,
                    8f + 24f * fin
            );
            Draw.color(core);
            Lines.stroke(1.5f * fout);
            Lines.circle(e.x, e.y, 4f + 14f * fin);
            Draw.color(Color.white, core, fin);
            Fill.circle(e.x, e.y, 6f * fout
            );
            Draw.color(core);
            Angles.randLenVectors(e.id, 8, 4f + 18f * fin, (x, y) -> {
                        Fill.circle(e.x + x, e.y + y, 2f * fout);
                    }
            );
            Draw.color(Color.white);
            Lines.stroke(1f * fout);
            Lines.line(e.x - 18f * fout, e.y, e.x + 18f * fout, e.y);
            Lines.line(e.x, e.y - 18f * fout, e.x, e.y + 18f * fout);
        });
        smallTeleport = new Effect(60.0F, 96.0F, (e) -> {
            float fin = e.fin();
            float fout = e.fout();
            float radius = 32.0F;
            Draw.color(Color.valueOf("FEEBB3FF"), Color.white, fin);
            Lines.stroke(3.0F * fout);
            Lines.circle(e.x, e.y, radius * fin);
            Draw.color(Color.valueOf("FEEBB3AA"));
            Lines.stroke(2.0F * fout);
            Lines.circle(e.x, e.y, radius * 0.7F + radius * 0.3F * fin);
            Draw.color(Color.valueOf("FEEBB3FF"));

            for (int i = 0; i < 3; ++i) {
                float angle = e.rotation + (float) i * 120.0F - fin * 360.0F;
                float x1 = e.x + Angles.trnsx(angle, radius * 0.35F);
                float y1 = e.y + Angles.trnsy(angle, radius * 0.35F);
                float x2 = e.x + Angles.trnsx(angle, radius);
                float y2 = e.y + Angles.trnsy(angle, radius);
                Lines.line(x1, y1, x2, y2);
            }

            Draw.color(Color.valueOf("FEEBB3FF"));
            Angles.randLenVectors((long) e.id, 48, radius, (x, y) -> {
                float scale = Mathf.sin(fin * (float) Math.PI);
                Drawf.tri(e.x + x * scale, e.y + y * scale, 3.0F * fout, 12.0F * fout, Mathf.angle(x, y) + fin * 360.0F);
            });
            Draw.color(Color.white);
            Fill.circle(e.x, e.y, 10.0F * fout);
            Draw.color(Color.valueOf("FEEBB3FF"));
            Fill.circle(e.x, e.y, 6.0F * fout);
            Draw.color(Color.valueOf("FEEBB3FF"));

            for (int i = 0; i < 8; ++i) {
                float angle = (float) i * 45.0F + fin * 720.0F;
                float length = 20.0F + 30.0F * Mathf.sin(fin * (float) Math.PI);
                Drawf.tri(e.x, e.y, 2.0F * fout, length * fout, angle);
            }

            Drawf.light(e.x, e.y, 96.0F, Color.valueOf("FEEBB3FF"), fout);
        });
        energyMine = new Effect(60f, e -> {
            float fin = e.finpow();
            float fout = e.foutpow();
            float rot = e.rotation;
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(2f * fout);
            Lines.circle(e.x,e.y,6f + fin * 18f);
            Fill.circle(e.x,e.y,4f * fout);
            Angles.randLenVectors(e.id, 12 ,18f * fin,(x,y)->{Fill.circle(e.x + x, e.y + y, 2f * fout);
            });
        });
        hugeEnergyExplosion = new Effect(120f,e -> {
            float fin = e.fin();
            float fout = e.fout();
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(3f * fout);
            Lines.circle(e.x,e.y,50f * fin);
            Lines.stroke(1.5f * fout);
            Lines.circle(e.x,e.y,35f * fin);
            Fill.circle(e.x,e.y,8f * fout);
            Angles.randLenVectors(e.id,40,50f * fin,(x,y)->{
                Fill.circle(
                        e.x + x,
                        e.y + y,
                        2.5f * fout
                );
            });
            Angles.randLenVectors(e.id,12,50f * fin,(x,y)->{
                Drawf.tri(
                        e.x + x,
                        e.y + y,
                        5f * fout,
                        15f * fout,
                        Mathf.angle(x,y)
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