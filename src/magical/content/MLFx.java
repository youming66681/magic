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
import arc.util.Tmp;

public class MLFx {
    public static Effect smallElectricDetonation;
    public static Effect largeElectricDetonation;
    public static Effect squareWaveRot;
    public static Effect beamEffect;
    public static Effect Explosion1;
    public static Effect Explosion2;
    public static Effect Explosion3;
    public static Effect smallEnergyBlast;
    public static Effect smallTeleport;
    public static Effect middleTeleport;
    public static Effect LargeTeleport;
    public static Effect energyMine;
    public static Effect EnergyExplosion;
    public static Effect EnergyExplosion2;
    public static Effect Explosion4;

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
        largeElectricDetonation = new Effect(60f, 100f, e -> {
            float fin = e.finpow();
            float fout = e.foutpow();
            Color color = Color.valueOf("97B5EDFF");
            Draw.color(color);
            Lines.stroke(4f * fout);
            Lines.circle(e.x, e.y, 80f * fin);
            Lines.stroke(2f * fout);
            Lines.circle(e.x, e.y, 55f * fin);
            Lines.stroke(1f * fout);
            Lines.circle(e.x, e.y, 30f * fin);
            Draw.alpha(fout);
            Fill.circle(e.x, e.y, 12f * fout);
            Angles.randLenVectors(e.id, 48, 80f * fin, (x, y) -> {
                float len = Mathf.len(x, y);
                if(len < 1f)return;
                float nx = x / len;
                float ny = y / len;
                Lines.stroke(Mathf.random(1f, 3f) * fout);
                Lines.line(
                        e.x + x,
                        e.y + y,
                        e.x + x + nx * Mathf.random(8f, 24f),
                        e.y + y + ny * Mathf.random(8f, 24f)
                );
            });
            Draw.reset();
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
        middleTeleport = new Effect(60f, e -> {
            float fin = e.fin();
            float fout = e.fout();
            float finpow = e.finpow();
            float foutpow = e.foutpow();
            float rot = e.rotation;
            Color blue = Color.valueOf("FEEBB3FF");
            Color gold = Color.valueOf("FEEBB3FF");
            Color white = Color.white;
            if(e.time < 12f){
                float flash = 1f - e.time / 12f;
                Draw.color(white, gold, flash);
                Fill.circle(e.x, e.y, 8f + 18f * flash);
            }
            Draw.color(blue);
            Lines.stroke(3f * fout);
            Lines.circle(e.x, e.y, 12f + 32f * fin);
            Draw.color(gold);
            Lines.stroke(2f * fout);
            Lines.circle(e.x, e.y, 8f + 24f * finpow);
            float runeRadius = 34f * fin;
            Draw.color(blue);
            Lines.stroke(1.5f * fout);
            for(int i = 0; i < 8; i++){
                float angle = rot + i * 45f + e.time * (2f + i * 0.15f);
                float x1 = e.x + Mathf.cosDeg(angle) * (runeRadius - 5f);
                float y1 = e.y + Mathf.sinDeg(angle) * (runeRadius - 5f);
                float x2 = e.x + Mathf.cosDeg(angle) * (runeRadius + 5f);
                float y2 = e.y + Mathf.sinDeg(angle) * (runeRadius + 5f);
                Lines.line(x1, y1, x2, y2);
            }
            Draw.color(gold);
            for(int i = 0; i < 12; i++){
                float angle = rot + i * 30f - e.time * 3f;
                float radius = 20f + Mathf.sin(e.time * 0.08f + i) * 8f;
                float px = e.x + Mathf.cosDeg(angle) * radius;
                float py = e.y + Mathf.sinDeg(angle) * radius;
                Fill.circle(px, py, 1.5f * fout);
            }
            int amount = 16;
            for(int i = 0; i < amount; i++){
                float angle = rot + i * (360f / amount) + e.time * 1.5f;
                float start = 18f + fin * 10f;
                float length = 8f + finpow * 28f;
                float end = start + length;
                float x1 = e.x + Mathf.cosDeg(angle) * start;
                float y1 = e.y + Mathf.sinDeg(angle) * start;
                float x2 = e.x + Mathf.cosDeg(angle) * end;
                float y2 = e.y + Mathf.sinDeg(angle) * end;
                Draw.color(i % 2 == 0 ? gold : blue);
                Lines.stroke((i % 2 == 0 ? 2f : 1f) * fout);
                Lines.line(x1, y1, x2, y2);
            }
            Angles.randLenVectors(e.id, 28, 38f * fin, (x, y) -> {
                Draw.color(Mathf.randomSeed(e.id + (int)(x * 10f + y)) > 0.5f ? gold : blue);
                Fill.circle(e.x + x, e.y + y, 1.2f * fout);
            });
            float coreSize = 5f + 8f * Mathf.absin(e.time, 8f, 1f);
            Draw.color(blue);
            Fill.circle(e.x, e.y, coreSize * fout);
            Draw.color(white);
            Fill.circle(e.x, e.y, coreSize * 0.45f * fout);
            if(e.time > 42f){
                float end = (e.time - 42f) / 18f;
                float shrink = 1f - end;
                Draw.color(gold);
                Lines.stroke(3f * shrink);
                Lines.circle(e.x, e.y, 40f * shrink);
                Draw.color(blue);
                Lines.stroke(1.5f * shrink);
                Lines.circle(e.x, e.y, 28f * shrink);
            }
        });
        LargeTeleport = new Effect(150f, 160f, e -> {
            float fin = e.fin();
            float fout = e.fout();
            float finpow = e.finpow();
            float foutpow = e.foutpow();
            float time = e.time;
            float radius = 8f + 120f * finpow;
            float fade = Math.min(foutpow * 1.5f, 1f);
            Draw.color(Color.valueOf("FEEBB3FF"));
            Fill.circle(e.x, e.y, 24f * fade);
            Draw.color(Color.valueOf("FEEBB3FF"));
            Fill.circle(e.x, e.y, 16f * fade);
            Draw.color(Color.white);
            Fill.circle(e.x, e.y, 7f * fade);
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(3f * fade);
            float cross = 18f + 20f * fin;
            Lines.line(e.x - cross, e.y, e.x + cross, e.y);
            Lines.line(e.x, e.y - cross, e.x, e.y + cross);
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(3f * fade);
            Lines.arc(e.x, e.y, 30f + 8f * fin, 0.65f, e.rotation + time * 3f);
            Lines.arc(e.x, e.y, 30f + 8f * fin, 0.65f, e.rotation + time * 3f + 180f);
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(5f * fade);
            Lines.arc(e.x, e.y, 46f + 16f * fin, 0.45f, e.rotation - time * 2.2f);
            Lines.arc(e.x, e.y, 46f + 16f * fin, 0.45f, e.rotation - time * 2.2f + 180f);
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(2.5f * fade);
            Lines.arc(e.x, e.y, 68f * finpow, 0.28f, e.rotation + time * 1.4f);
            Lines.arc(e.x, e.y, 68f * finpow, 0.28f, e.rotation + time * 1.4f + 120f);
            Lines.arc(e.x, e.y, 68f * finpow, 0.28f, e.rotation + time * 1.4f + 240f);
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(4f * fade);
            Lines.circle(e.x, e.y, radius);
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(2f * fade);
            Lines.circle(e.x, e.y, radius - 7f);
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(3f * fade);
            for(int i = 0; i < 12; i++){
                float rotation = e.rotation + time * 1.8f + i * 30f;
                float start = radius * 0.78f;
                float end = radius * 0.96f;
                float x1 = e.x + Angles.trnsx(rotation, start);
                float y1 = e.y + Angles.trnsy(rotation, start);
                float x2 = e.x + Angles.trnsx(rotation, end);
                float y2 = e.y + Angles.trnsy(rotation, end);
                Lines.line(x1, y1, x2, y2);
            }
            Draw.color(Color.valueOf("FEEBB3FF"));
            for(int i = 0; i < 16; i++){
                float rotation = e.rotation - time * 2.5f + i * 22.5f;
                float spikeRadius = 78f * finpow;
                float x = e.x + Angles.trnsx(rotation, spikeRadius);
                float y = e.y + Angles.trnsy(rotation, spikeRadius);
                Drawf.tri(x, y, 5f + 7f * fade, 20f + 34f * fin, rotation);
            }
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(2.5f * fade);
            for(int i = 0; i < 24; i++){
                float rotation = e.rotation + i * 15f + time * 1.2f;
                float inner = 35f + 15f * fin;
                float outer = radius * (0.72f + 0.18f * Mathf.sin(time * 0.08f + i));
                float x1 = e.x + Angles.trnsx(rotation, inner);
                float y1 = e.y + Angles.trnsy(rotation, inner);
                float x2 = e.x + Angles.trnsx(rotation, outer);
                float y2 = e.y + Angles.trnsy(rotation, outer);
                Lines.line(x1, y1, x2, y2);
            }
            Angles.randLenVectors(e.id, 50, radius * 0.9f, (x, y) -> {
                float size = (1.5f + 3.5f * Mathf.random()) * fade;
                Draw.color(Color.valueOf("FEEBB3FF"));
                Fill.circle(e.x + x, e.y + y, size);
            });
            Angles.randLenVectors(e.id + 1, 35, radius * 0.7f, (x, y) -> {
                float size = (1f + 2.5f * Mathf.random()) * fade;
                Draw.color(Color.valueOf("FEEBB3FF"));
                Fill.circle(e.x + x, e.y + y, size);
            });
            Angles.randLenVectors(e.id + 2, 20, radius * 0.95f, (x, y) -> {
                float angle = Angles.angle(x, y);
                float len = 8f + 18f * fin;
                float x2 = e.x + x + Angles.trnsx(angle, len);
                float y2 = e.y + y + Angles.trnsy(angle, len);
                Draw.color(Color.valueOf("FEEBB3FF"));
                Lines.stroke(2f * fade);
                Lines.line(e.x + x, e.y + y, x2, y2);
            });
            Draw.color(Color.white);
            for(int i = 0; i < 8; i++){
                float rotation = e.rotation + time * 5f + i * 45f;
                float length = 20f + 16f * fin;
                float width = 3f * fade;
                Drawf.tri(e.x + Angles.trnsx(rotation, length), e.y + Angles.trnsy(rotation, length), width, 22f * fade, rotation);
            }
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(2f * fade);
            float pulse = radius + Mathf.sin(time * 0.18f) * 6f;
            Lines.circle(e.x, e.y, pulse);
            Draw.color(Color.white, fade);
            Fill.circle(e.x, e.y, (5f + 12f * Mathf.absin(time, 6f, 1f)) * fade);
        });
        energyMine = new Effect(60f, e -> {
            float fin = e.finpow();
            float fout = e.foutpow();
            float rot = e.rotation;
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(2f * fout);
            Lines.circle(e.x, e.y, 6f + fin * 18f);
            Fill.circle(e.x, e.y, 4f * fout);
            Angles.randLenVectors(e.id, 12, 18f * fin, (x, y) -> {
                Fill.circle(e.x + x, e.y + y, 2f * fout);
            });
        });
        Explosion4 = new Effect(45f, e -> {
            float fin = e.finpow();
            float fout = e.foutpow();
            float radius = 48f * fin;
            Draw.color(Color.valueOf("ff9b42"));
            Lines.stroke(4f * fout);
            Lines.circle(e.x, e.y, radius);
            Draw.color(Color.valueOf("ff9b42"));
            Lines.stroke(2f * fout);
            Lines.circle(e.x, e.y, radius * 0.72f);
            Draw.color(Color.valueOf("ff9b42"));
            Fill.circle(e.x, e.y, 8f * fout);
            Draw.color(Color.valueOf("ff9b42"));
            Angles.randLenVectors(e.id, 28, radius, (x, y) -> {
                float len = Mathf.len(x, y);
                if(len > 0.001f){Fill.circle(e.x + x, e.y + y, 3f * fout);
                }
            });
            Draw.color(Color.valueOf("ff9b42"));
            Angles.randLenVectors(e.id + 1, 16, radius * 0.8f, (x, y) -> {Lines.line(e.x, e.y, e.x + x, e.y + y);
            });
            Draw.color(Color.valueOf("ff9b42"));
            Lines.stroke(1.5f * fout);
            Angles.randLenVectors(e.id + 2, 12, radius * 0.95f, (x, y) -> {
                float angle = Angles.angle(x, y);
                float len = 8f * fout;
                Lines.lineAngle(e.x + x, e.y + y, angle, len);
            });
        });
        EnergyExplosion = new Effect(45f, e -> {
            float fin = e.finpow();
            float fout = e.foutpow();
            float radius = 48f * fin;
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(4f * fout);
            Lines.circle(e.x, e.y, radius);
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(2f * fout);
            Lines.circle(e.x, e.y, radius * 0.72f);
            Fill.circle(e.x, e.y, 8f * fout);
            Angles.randLenVectors(e.id, 24, radius, (x, y) -> {
                float len = Mathf.len(x, y);
                float scale = 3f * fout;
                Fill.circle(e.x + x, e.y + y, scale);
            });
            Angles.randLenVectors(e.id + 1, 12, 48f * fin, (x, y) -> {
                float angle = Mathf.angle(x, y);
                Drawf.tri(e.x + x, e.y + y, 5f * fout, 18f * fout, angle);
            });
        });
        EnergyExplosion2 = new Effect(45f, 90f, e -> {
            float fin = e.finpow();
            float fout = e.foutpow();
            float radius = 8f + 56f * fin;
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(5f * fout);
            Lines.circle(e.x, e.y, radius);
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(2.5f * fout);
            Lines.circle(e.x, e.y, radius * 0.75f);
            Fill.circle(e.x, e.y, 10f * fout);
            Draw.color(Color.white);
            Fill.circle(e.x, e.y, 5f * fout);
            Angles.randLenVectors(e.id, 24, radius, (x, y) -> {
                float len = 12f + 32f * fin;
                float width = (3f + 5f * fout);
                Draw.color(Color.valueOf("FEEBB3FF"));
                Fill.poly(e.x + x, e.y + y, 4, width, Angles.angle(x, y));
            });
            Angles.randLenVectors(e.id + 1, 16, 48f * fin, (x, y) -> {
                Draw.color(Color.valueOf("FEEBB3FF"));
                Fill.circle(e.x + x, e.y + y, 2.5f * fout);
            });
            Draw.color(Color.valueOf("FEEBB3FF"));
            Lines.stroke(3f * fout);
            for(int i = 0; i < 8; i++){
                float angle = i * 45f + e.rotation;
                float len = 18f + 46f * fin;
                float x1 = e.x + Angles.trnsx(angle, 8f);
                float y1 = e.y + Angles.trnsy(angle, 8f);
                float x2 = e.x + Angles.trnsx(angle, len);
                float y2 = e.y + Angles.trnsy(angle, len);
                Lines.line(x1, y1, x2, y2);
            }
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