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
import arc.math.Rand;
import arc.math.geom.Position;
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
import arc.math.Mathf;
import arc.math.geom.Vec2;

public class MLFx {
    public static final Rand rand = new Rand();
    public static final Vec2 v = new Vec2();

    public static void load(){}

    public static Effect smallElectricDetonation = new Effect(30, e -> {
        Mathf.randLenVectors(e.id, 8, 2f + e.fin() * 4f, (x, y) -> {
            Fill.square(e.x + x, e.y + y, 0.5f + e.fout() * 2f, 45);
            Draw.color(Color.valueOf("#FEEBB3FF"), e.color, e.finpow());
        });
    });
}