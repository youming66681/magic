//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package magical.content;

import arc.Core;
import arc.func.Prov;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.util.Nullable;
import java.lang.reflect.Field;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Effect;
import mindustry.gen.Icon;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import mindustry.world.Block;

public class ContentResolver {
    @Nullable
    public static Effect findFx(String name) {
        Object effect = ParserUtils.field(Fx.class, name);
        return effect instanceof Effect ? (Effect)effect : null;
    }

    @Nullable
    public static Item findItem(String id) {
        for(Item item : Vars.content.items()) {
            if (id.equals(item.name)) {
                return item;
            }
        }

        return null;
    }

    @Nullable
    public static Liquid findFluid(String id) {
        for(Liquid fluid : Vars.content.liquids()) {
            if (id.equals(fluid.name)) {
                return fluid;
            }
        }

        return null;
    }

    @Nullable
    public static Block findBlock(String id) {
        for(Block block : Vars.content.blocks()) {
            if (id.equals(block.name)) {
                return block;
            }
        }

        return null;
    }

    @Nullable
    public static UnitType findUnit(String id) {
        for(UnitType unit : Vars.content.units()) {
            if (id.equals(unit.name)) {
                return unit;
            }
        }

        return null;
    }

    @Nullable
    public static UnlockableContent findPayload(String id) {
        UnitType unit = findUnit(id);
        return (UnlockableContent)(unit != null ? unit : findBlock(id));
    }

    @Nullable
    public static Prov<TextureRegion> findIcon(String name) {
        if (name.startsWith("Icon.") && name.length() > 5) {
            try {
                String fieldName = name.substring(5);
                Field field = Icon.class.getField(fieldName.contains("-") ? ParserUtils.kebab2camel(fieldName) : fieldName);
                Object icon = field.get((Object)null);
                TextureRegion tr = ((TextureRegionDrawable)icon).getRegion();
                return () -> tr;
            } catch (IllegalAccessException | NoSuchFieldException var5) {
                return null;
            }
        } else {
            Item item = findItem(name);
            if (item != null) {
                return () -> item.uiIcon;
            } else {
                Liquid fluid = findFluid(name);
                if (fluid != null) {
                    return () -> fluid.uiIcon;
                } else {
                    UnlockableContent payload = findPayload(name);
                    if (payload != null) {
                        return () -> payload.uiIcon;
                    } else {
                        TextureRegion tr = Core.atlas.find(name);
                        return tr.found() ? () -> tr : null;
                    }
                }
            }
        }
    }
}
