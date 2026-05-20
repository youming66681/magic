package magical.content;

import mindustry.gen.*;
import arc.*;
import arc.graphics.*;
import arc.math.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.type.*;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.geom.Vec2;
import mindustry.entities.Effect;
import arc.math.Interp;
import arc.math.Mathf;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines;

public class MLFx {
    public static final Rand rand = new Rand();
    Vec2 temp = new Vec2();

    public static Effect smallElectricDetonation = new Effect(30f, (e) -> {
        Draw.color(Color.valueOf("97B5EDFF"), e.color, e.fin() + 0.4F);
        e.scaled(6, (i) -> {
            Lines.stroke(3f * i.foutpow());
            Lines.circle(i.x, i.y, i.fin(Interp.circleOut) * rad * 15F);
        });
        Angles.randLenVectors((long)e.id, 6, 21f * e.finpow(), (x, y) -> {
            Fill.circle(e.x, e.y, rad * e.fout() * 4f);
        });
        Angles.randLenVectors((long)e.id, 6, 20f * e.finpow(), (x, y) -> {
            Lines.lineAngle(e.x + x, e.y + y, ang * e.fout() * Mathf.random(4f, 8f) + 2f);
        });
    });

    public static void load(){}
}