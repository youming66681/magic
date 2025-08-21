package magica.conten;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.*;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.content.*;
import mindustry.entities.Effect;
import mindustry.entities.UnitSorts;
import mindustry.entities.abilities.MoveEffectAbility;
import mindustry.entities.abilities.StatusFieldAbility;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.HaloPart;
import mindustry.entities.part.RegionPart;
import mindustry.entities.part.ShapePart;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.*;
import mindustry.type.unit.MissileUnitType;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.liquid.ArmoredConduit;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.blocks.liquid.LiquidBridge;
import mindustry.world.blocks.liquid.LiquidRouter;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import static magical.magic.name;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.lineAngle;
import static arc.graphics.g2d.Lines.stroke;
import static arc.math.Angles.*;
import static arc.math.Mathf.random;
import static arc.math.Mathf.sqr;
import static mindustry.type.ItemStack.*;

public class MLBlocks {
    public static Block hg = new OreBlock(MLItems.hg) {{
        oreDefault = true;
        oreThreshold = 0.8f;
        oreScale = 21.0f;
    }};
    public static Block sm = new OreBlock(Items.graphite) {{
        oreDefault = true;
        oreThreshold = 0.8f;
        oreScale = 21.0f;
    }};
    public static Block ys = new OreBlock(MLItems.ys) {{
        oreDefault = true;
        oreThreshold = 0.9f;
        oreScale = 23.0f;
    }};
    public static void load(){}
}