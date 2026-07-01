//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package magical.content;

import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.scene.utils.Elem;
import arc.util.Nullable;
import arc.util.Scaling;
import java.util.HashMap;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;
import mindustry.ui.Styles;

public abstract class RecipeSwitchStyle {
    public static HashMap<String, RecipeSwitchStyle> all = new HashMap();
    public static RecipeSwitchStyle simple = new RecipeSwitchStyle("simple") {
        public void build(MultiCrafter b, MultiCrafter.MultiCrafterBuild c, Table table) {
            Table t = new Table();
            t.background(Tex.whiteui);
            t.setColor(Pal.darkerGray);

            for(int i = 0; i < b.resolvedRecipes.size; ++i) {
                Recipe recipe = (Recipe)b.resolvedRecipes.get(i);
                ImageButton button = new ImageButton(Styles.clearTogglei);
                Image img;
                if (recipe.icon != null) {
                    img = new Image((TextureRegion)recipe.icon.get());
                    if (recipe.iconColor != null) {
                        img.setColor(recipe.iconColor);
                    }
                } else {
                    img = getDefaultIcon(b, c, recipe.output);
                }

                button.replaceImage(img);
                button.getImageCell().scaling(Scaling.fit).size(40.0F);
                button.changed(() -> c.configure(i));
                button.update(() -> button.setChecked(c.curRecipeIndex == i));
                t.add(button).grow().margin(10.0F);
                if (i != 0 && i % 3 == 0) {
                    t.row();
                }
            }

            table.add(t).grow();
        }
    };
    public static RecipeSwitchStyle number = new RecipeSwitchStyle("number") {
        public void build(MultiCrafter b, MultiCrafter.MultiCrafterBuild c, Table table) {
            Table t = new Table();

            for(int i = 0; i < b.resolvedRecipes.size; ++i) {
                Recipe recipe = (Recipe)b.resolvedRecipes.get(i);
                TextButton button = Elem.newButton("" + i, Styles.togglet, () -> c.configure(i));
                if (recipe.iconColor != null) {
                    button.setColor(recipe.iconColor);
                }

                button.update(() -> button.setChecked(c.curRecipeIndex == i));
                t.add(button).size(50.0F);
                if (i != 0 && i % 3 == 0) {
                    t.row();
                }
            }

            table.add(t).grow();
        }
    };
    public static RecipeSwitchStyle transform = new RecipeSwitchStyle("transform") {
        public void build(MultiCrafter b, MultiCrafter.MultiCrafterBuild c, Table table) {
            Table t = new Table();

            for(int i = 0; i < b.resolvedRecipes.size; ++i) {
                if (i != 0 && i % 2 == 0) {
                    t.row();
                }

                Recipe recipe = (Recipe)b.resolvedRecipes.get(i);
                ImageButton button = new ImageButton(Styles.clearTogglei);
                Table bt = new Table();
                Image in = getDefaultIcon(b, c, recipe.input);
                bt.add(in).pad(6.0F);
                bt.image(Icon.right).pad(6.0F);
                Image out = getDefaultIcon(b, c, recipe.output);
                bt.add(out).pad(6.0F);
                button.replaceImage(bt);
                button.changed(() -> c.configure(i));
                button.update(() -> button.setChecked(c.curRecipeIndex == i));
                t.add(button).grow().pad(8.0F).margin(10.0F);
            }

            table.add(t).grow();
        }
    };
    public static RecipeSwitchStyle detailed = new RecipeSwitchStyle("detailed") {
        public void build(MultiCrafter b, MultiCrafter.MultiCrafterBuild c, Table table) {
            for(int i = 0; i < b.resolvedRecipes.size; ++i) {
                Recipe recipe = (Recipe)b.resolvedRecipes.get(i);
                Table t = new Table();
                t.background(Tex.whiteui);
                t.setColor(Pal.darkestGray);
                b.buildIOEntry(t, recipe, true);
                t.image(Icon.right);
                b.buildIOEntry(t, recipe, false);
                ImageButton button = new ImageButton(Styles.clearTogglei);
                button.changed(() -> c.configure(i));
                button.update(() -> button.setChecked(c.curRecipeIndex == i));
                button.replaceImage(t);
                table.add(button).pad(5.0F).margin(10.0F).grow();
                table.row();
            }

        }
    };

    public static RecipeSwitchStyle get(@Nullable String name) {
        if (name == null) {
            return transform;
        } else {
            RecipeSwitchStyle inMap = (RecipeSwitchStyle)all.get(name.toLowerCase());
            return inMap == null ? transform : inMap;
        }
    }

    public RecipeSwitchStyle(String name) {
        all.put(name.toLowerCase(), this);
    }

    public abstract void build(MultiCrafter var1, MultiCrafter.MultiCrafterBuild var2, Table var3);

    public static Image getDefaultIcon(MultiCrafter b, MultiCrafter.MultiCrafterBuild c, IOEntry entry) {
        if (entry.icon != null) {
            Image img = new Image((TextureRegion)entry.icon.get());
            if (entry.iconColor != null) {
                img.setColor(entry.iconColor);
            }

            return img;
        } else {
            ItemStack[] items = entry.items;
            LiquidStack[] fluids = entry.fluids;
            boolean outputPower = entry.power > 0.0F;
            boolean outputHeat = entry.heat > 0.0F;
            PayloadStack[] paylods = entry.payloads;
            if (items.length > 0) {
                return new Image(items[0].item.uiIcon);
            } else if (fluids.length > 0) {
                return new Image(fluids[0].liquid.uiIcon);
            } else if (outputPower) {
                Image img = new Image(Icon.power.getRegion());
                img.setColor(Pal.power);
                return img;
            } else if (outputHeat) {
                Image img = new Image(Icon.waves.getRegion());
                img.setColor(b.heatColor);
                return img;
            } else {
                return paylods.length > 0 ? new Image(paylods[0].item.uiIcon) : new Image(Icon.cancel.getRegion());
            }
        }
    }
}
