package magical.content;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.struct.IntMap;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import java.util.Arrays;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.effect.MultiEffect;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import mindustry.type.UnitType;
import mindustry.ui.Fonts;

public class MLFx {
    public static final Rand rand = new Rand();
    public static final Vec2 v = new Vec2();

    public static Effect smallElectricDetonation = new Effect(30, e -> {
        Mathf.randLenVectors(e.id, 8, 2f + e.fin() * 4f, (x, y) -> {
            Draw.color(Color.valueOf("#FEEBB3FF"), e.color, e.finpow());
            Lines.stroke(e.fout() * 1f);
            Lines.circle(e.x, e.y, e.finpow() * 4f);
            Fill.square(e.x + x, e.y + y, 1.0f + e.fout() * 1.5f, 45);
        });
    });
}