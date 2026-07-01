//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package magical.content;

import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Effect;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.Icon;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;
import mindustry.world.Block;

public class MultiCrafterParser {
    private static final String[] inputAlias = new String[]{"input", "in", "i"};
    private static final String[] outputAlias = new String[]{"output", "out", "o"};
    private String curBlock = "";
    private int recipeIndex = 0;
    private final Seq<String> errors = new Seq();
    private final Seq<String> warnings = new Seq();
    private static final Prov<TextureRegion> NotFound = () -> Icon.cancel.getRegion();
    private static final Effect[] EffectType = new Effect[0];

    public Seq<Recipe> parse(Block meta, Object o) {
        this.curBlock = genName(meta);

        try {
            o = ParserUtils.parseJsonToObject(o);
        } catch (Exception e) {
            this.error("Can't convert Seq in preprocess " + String.valueOf(o), e);
            o = Collections.emptyList();
        }

        Seq<Recipe> recipes = new Seq(Recipe.class);
        this.recipeIndex = 0;
        if (o instanceof List) {
            for(Object recipeMapObj : (List)o) {
                Map recipeMap = (Map)recipeMapObj;
                this.parseRecipe(recipeMap, recipes);
                ++this.recipeIndex;
            }
        } else {
            if (!(o instanceof Map)) {
                throw new RecipeParserException("Unsupported recipe list from <" + String.valueOf(o) + ">");
            }

            Map recipeMap = (Map)o;
            this.parseRecipe(recipeMap, recipes);
        }

        return recipes;
    }

    private void parseRecipe(Map recipeMap, Seq<Recipe> to) {
        try {
            Recipe recipe = new Recipe();
            Object inputsRaw = findValueByAlias(recipeMap, inputAlias);
            if (inputsRaw == null) {
                this.warn("Recipe has no input, please ensure it's expected.");
            }

            recipe.input = this.parseIOEntry("input", inputsRaw);
            Object outputsRaw = findValueByAlias(recipeMap, outputAlias);
            if (outputsRaw == null) {
                this.warn("Recipe has no output, please ensure it's expected");
            }

            recipe.output = this.parseIOEntry("output", outputsRaw);
            Object craftTimeObj = recipeMap.get("craftTime");
            recipe.craftTime = ParserUtils.parseFloat(craftTimeObj);
            Object iconObj = recipeMap.get("icon");
            if (iconObj instanceof String) {
                recipe.icon = this.findIcon((String)iconObj);
            }

            Object iconColorObj = recipeMap.get("iconColor");
            if (iconColorObj instanceof String) {
                recipe.iconColor = Color.valueOf((String)iconColorObj);
            }

            Object fxObj = recipeMap.get("craftEffect");
            Effect fx = parseFx(fxObj);
            if (fx != null) {
                recipe.craftEffect = fx;
            }

            if (recipe.input.isEmpty() && recipe.output.isEmpty()) {
                this.warn("Recipe is completely empty.");
            }

            to.add(recipe);
        } catch (Exception e) {
            this.error("Can't load a recipe", e);
        }

    }

    private IOEntry parseIOEntry(String meta, @Nullable Object ioEntry) {
        IOEntry res = new IOEntry();
        if (ioEntry == null) {
            return res;
        } else {
            if (ioEntry instanceof Map) {
                Map ioRawMap = (Map)ioEntry;
                Object items = ioRawMap.get("items");
                if (items != null) {
                    if (items instanceof List) {
                        this.parseItems((List)items, res);
                    } else if (items instanceof String) {
                        this.parseItemPair((String)items, res);
                    } else {
                        if (!(items instanceof Map)) {
                            throw new RecipeParserException("Unsupported type of items at " + meta + " from <" + String.valueOf(items) + ">");
                        }

                        this.parseItemMap((Map)items, res);
                    }
                }

                Object fluids = ioRawMap.get("fluids");
                if (fluids != null) {
                    if (fluids instanceof List) {
                        this.parseFluids((List)fluids, res);
                    } else if (fluids instanceof String) {
                        this.parseFluidPair((String)fluids, res);
                    } else {
                        if (!(fluids instanceof Map)) {
                            throw new RecipeParserException("Unsupported type of fluids at " + meta + " from <" + String.valueOf(fluids) + ">");
                        }

                        this.parseFluidMap((Map)fluids, res);
                    }
                }

                Object powerObj = ioRawMap.get("power");
                res.power = ParserUtils.parseFloat(powerObj);
                Object heatObj = ioRawMap.get("heat");
                res.heat = ParserUtils.parseFloat(heatObj);
                Object payloads = ioRawMap.get("payloads");
                if (payloads != null) {
                    if (payloads instanceof List) {
                        this.parsePayloads((List)payloads, res);
                    } else if (payloads instanceof String) {
                        this.parsePayloadPair((String)payloads, res);
                    } else {
                        if (!(payloads instanceof Map)) {
                            throw new RecipeParserException("Unsupported type of payloads at " + meta + " from <" + String.valueOf(payloads) + ">");
                        }

                        this.parsePayloadMap((Map)payloads, res);
                    }
                }

                Object iconObj = ioRawMap.get("icon");
                if (iconObj instanceof String) {
                    res.icon = this.findIcon((String)iconObj);
                }

                Object iconColorObj = ioRawMap.get("iconColor");
                if (iconColorObj instanceof String) {
                    res.iconColor = Color.valueOf((String)iconColorObj);
                }
            } else if (ioEntry instanceof List) {
                for(Object content : (List)ioEntry) {
                    if (content instanceof String) {
                        this.parseAnyPair((String)content, res);
                    } else {
                        if (!(content instanceof Map)) {
                            throw new RecipeParserException("Unsupported type of content at " + meta + " from <" + String.valueOf(content) + ">");
                        }

                        this.parseAnyMap((Map)content, res);
                    }
                }
            } else {
                if (!(ioEntry instanceof String)) {
                    throw new RecipeParserException("Unsupported type of " + meta + " <" + String.valueOf(ioEntry) + ">");
                }

                this.parseAnyPair((String)ioEntry, res);
            }

            return res;
        }
    }

    private void parseItems(List items, IOEntry res) {
        for(Object entryRaw : items) {
            if (entryRaw instanceof String) {
                this.parseItemPair((String)entryRaw, res);
            } else if (entryRaw instanceof Map) {
                this.parseItemMap((Map)entryRaw, res);
            } else {
                this.error("Unsupported type of items <" + String.valueOf(entryRaw) + ">, so skip them");
            }
        }

    }

    private void parseItemPair(String pair, IOEntry res) {
        try {
            String[] id2Amount = pair.split("/");
            if (id2Amount.length != 1 && id2Amount.length != 2) {
                this.error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                return;
            }

            String itemID = id2Amount[0];
            Item item = ContentResolver.findItem(itemID);
            if (item == null) {
                this.error("<" + itemID + "> doesn't exist in all items, so skip this");
                return;
            }

            ItemStack entry = new ItemStack();
            entry.item = item;
            if (id2Amount.length == 2) {
                String amountStr = id2Amount[1];
                entry.amount = Integer.parseInt(amountStr);
            } else {
                entry.amount = 1;
            }

            res.items = ParserUtils.addItemStack(res.items, entry);
        } catch (Exception e) {
            this.error("Can't parse an item from <" + pair + ">, so skip it", e);
        }

    }

    private void parseFluids(List fluids, IOEntry res) {
        for(Object entryRaw : fluids) {
            if (entryRaw instanceof String) {
                this.parseFluidPair((String)entryRaw, res);
            } else if (entryRaw instanceof Map) {
                this.parseFluidMap((Map)entryRaw, res);
            } else {
                this.error("Unsupported type of fluids <" + String.valueOf(entryRaw) + ">, so skip them");
            }
        }

    }

    private void parseFluidPair(String pair, IOEntry res) {
        try {
            String[] id2Amount = pair.split("/");
            if (id2Amount.length != 1 && id2Amount.length != 2) {
                this.error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                return;
            }

            String fluidID = id2Amount[0];
            Liquid fluid = ContentResolver.findFluid(fluidID);
            if (fluid == null) {
                this.error("<" + fluidID + "> doesn't exist in all fluids, so skip this");
                return;
            }

            LiquidStack entry = new LiquidStack(Liquids.water, 0.0F);
            entry.liquid = fluid;
            if (id2Amount.length == 2) {
                String amountStr = id2Amount[1];
                entry.amount = Float.parseFloat(amountStr);
            } else {
                entry.amount = 1.0F;
            }

            res.fluids = ParserUtils.addLiquidStack(res.fluids, entry);
        } catch (Exception e) {
            this.error("Can't parse a fluid from <" + pair + ">, so skip it", e);
        }

    }

    private void parsePayloads(List payloads, IOEntry res) {
        for(Object entryRaw : payloads) {
            if (entryRaw instanceof String) {
                this.parsePayloadPair((String)entryRaw, res);
            } else if (entryRaw instanceof Map) {
                this.parsePayloadMap((Map)entryRaw, res);
            } else {
                this.error("Unsupported type of items <" + String.valueOf(entryRaw) + ">, so skip them");
            }
        }

    }

    private void parsePayloadPair(String pair, IOEntry res) {
        try {
            String[] id2Amount = pair.split("/");
            if (id2Amount.length != 1 && id2Amount.length != 2) {
                this.error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                return;
            }

            String payloadID = id2Amount[0];
            UnlockableContent payload = ContentResolver.findPayload(payloadID);
            if (payload == null) {
                this.error("<" + payloadID + "> doesn't exist in all payloads, so skip this");
                return;
            }

            PayloadStack entry = new PayloadStack();
            entry.item = payload;
            if (id2Amount.length == 2) {
                String amountStr = id2Amount[1];
                entry.amount = Integer.parseInt(amountStr);
            } else {
                entry.amount = 1;
            }

            res.payloads = ParserUtils.addPayloadStack(res.payloads, entry);
        } catch (Exception e) {
            this.error("Can't parse an item from <" + pair + ">, so skip it", e);
        }

    }

    private void parseAnyPair(String pair, IOEntry res) {
        try {
            String[] id2Amount = pair.split("/");
            if (id2Amount.length != 1 && id2Amount.length != 2) {
                this.error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                return;
            }

            String id = id2Amount[0];
            Item item = ContentResolver.findItem(id);
            if (item != null) {
                ItemStack entry = new ItemStack(Items.copper, 1);
                entry.item = item;
                if (id2Amount.length == 2) {
                    String amountStr = id2Amount[1];
                    entry.amount = Integer.parseInt(amountStr);
                }

                res.items = ParserUtils.addItemStack(res.items, entry);
                return;
            }

            Liquid fluid = ContentResolver.findFluid(id);
            if (fluid != null) {
                LiquidStack entry = new LiquidStack(Liquids.water, 1.0F);
                entry.liquid = fluid;
                if (id2Amount.length == 2) {
                    String amountStr = id2Amount[1];
                    entry.amount = Float.parseFloat(amountStr);
                }

                res.fluids = ParserUtils.addLiquidStack(res.fluids, entry);
                return;
            }

            UnlockableContent payload = ContentResolver.findPayload(id);
            if (payload != null) {
                PayloadStack entry = new PayloadStack(Blocks.router, 1);
                entry.item = payload;
                if (id2Amount.length == 2) {
                    String amountStr = id2Amount[1];
                    entry.amount = Integer.parseInt(amountStr);
                }

                res.payloads = ParserUtils.addPayloadStack(res.payloads, entry);
                return;
            }

            this.error("Can't find the corresponding item, fluid or payload from this <" + pair + ">, so skip it");
        } catch (Exception e) {
            this.error("Can't parse this uncertain <" + pair + ">, so skip it", e);
        }

    }

    private void parseAnyMap(Map map, IOEntry res) {
        try {
            Object itemRaw = map.get("item");
            if (itemRaw instanceof String) {
                Item item = ContentResolver.findItem((String)itemRaw);
                if (item != null) {
                    ItemStack entry = new ItemStack();
                    entry.item = item;
                    Object amountRaw = map.get("amount");
                    entry.amount = ParserUtils.parseInt(amountRaw);
                    res.items = ParserUtils.addItemStack(res.items, entry);
                    return;
                }
            }

            Object fluidRaw = map.get("fluid");
            if (fluidRaw instanceof String) {
                Liquid fluid = ContentResolver.findFluid((String)fluidRaw);
                if (fluid != null) {
                    LiquidStack entry = new LiquidStack(Liquids.water, 0.0F);
                    entry.liquid = fluid;
                    Object amountRaw = map.get("amount");
                    entry.amount = ParserUtils.parseFloat(amountRaw);
                    res.fluids = ParserUtils.addLiquidStack(res.fluids, entry);
                    return;
                }
            }

            Object payloadRaw = map.get("payload");
            if (payloadRaw instanceof String) {
                UnlockableContent payload = ContentResolver.findPayload((String)payloadRaw);
                if (payload != null) {
                    PayloadStack entry = new PayloadStack();
                    entry.item = payload;
                    Object amountRaw = map.get("amount");
                    entry.amount = ParserUtils.parseInt(amountRaw);
                    res.payloads = ParserUtils.addPayloadStack(res.payloads, entry);
                    return;
                }
            }

            this.error("Can't find the corresponding item, fluid or payload from <" + String.valueOf(map) + ">, so skip it");
        } catch (Exception e) {
            this.error("Can't parse this uncertain <" + String.valueOf(map) + ">, so skip it", e);
        }

    }

    private void parseItemMap(Map map, IOEntry res) {
        try {
            ItemStack entry = new ItemStack();
            Object itemID = map.get("item");
            if (!(itemID instanceof String)) {
                this.error("Can't recognize a fluid from <" + String.valueOf(map) + ">");
                return;
            }

            Item item = ContentResolver.findItem((String)itemID);
            if (item == null) {
                this.error("<" + String.valueOf(itemID) + "> doesn't exist in all items, so skip this");
                return;
            }

            entry.item = item;
            int amount = ParserUtils.parseInt(map.get("amount"));
            entry.amount = amount;
            if (amount <= 0) {
                this.error("Item amount is +" + amount + " <= 0, so reset as 1");
                entry.amount = 1;
            }

            res.items = ParserUtils.addItemStack(res.items, entry);
        } catch (Exception e) {
            this.error("Can't parse an item <" + String.valueOf(map) + ">, so skip it", e);
        }

    }

    private void parseFluidMap(Map map, IOEntry res) {
        try {
            LiquidStack entry = new LiquidStack(Liquids.water, 0.0F);
            Object fluidID = map.get("fluid");
            if (!(fluidID instanceof String)) {
                this.error("Can't recognize an item from <" + String.valueOf(map) + ">");
                return;
            }

            Liquid fluid = ContentResolver.findFluid((String)fluidID);
            if (fluid == null) {
                this.error(String.valueOf(fluidID) + " doesn't exist in all fluids, so skip this");
                return;
            }

            entry.liquid = fluid;
            float amount = ParserUtils.parseFloat(map.get("amount"));
            entry.amount = amount;
            if (amount <= 0.0F) {
                this.error("Fluids amount is +" + amount + " <= 0, so reset as 1.0f");
                entry.amount = 1.0F;
            }

            res.fluids = ParserUtils.addLiquidStack(res.fluids, entry);
        } catch (Exception e) {
            this.error("Can't parse <" + String.valueOf(map) + ">, so skip it", e);
        }

    }

    private void parsePayloadMap(Map map, IOEntry res) {
        try {
            PayloadStack entry = new PayloadStack();
            Object payloadID = map.get("payload");
            if (!(payloadID instanceof String)) {
                this.error("Can't recognize a fluid from <" + String.valueOf(map) + ">");
                return;
            }

            UnlockableContent payload = ContentResolver.findPayload((String)payloadID);
            if (payload == null) {
                this.error("<" + String.valueOf(payloadID) + "> doesn't exist in all payloads, so skip this");
                return;
            }

            entry.item = payload;
            int amount = ParserUtils.parseInt(map.get("amount"));
            entry.amount = amount;
            if (amount <= 0) {
                this.error("Payload amount is +" + amount + " <= 0, so reset as 1");
                entry.amount = 1;
            }

            res.payloads = ParserUtils.addPayloadStack(res.payloads, entry);
        } catch (Exception e) {
            this.error("Can't parse an item <" + String.valueOf(map) + ">, so skip it", e);
        }

    }

    private void error(String content) {
        this.error(content, (Throwable)null);
    }

    private void error(String content, @Nullable Throwable e) {
        String var10000 = this.buildRecipeIndexInfo();
        String message = var10000 + content;
        this.errors.add(message);
        if (e == null) {
            Log.err(message, new Object[0]);
        } else {
            Log.err(message, e);
        }

    }

    private void warn(String content) {
        String var10000 = this.buildRecipeIndexInfo();
        String message = var10000 + content;
        this.warnings.add(message);
        Log.warn(message, new Object[0]);
    }

    private String buildRecipeIndexInfo() {
        return "[" + this.curBlock + "](at recipe " + this.recipeIndex + ")\n";
    }

    public static String genName(Block meta) {
        return meta.localizedName.equals(meta.name) ? meta.name : meta.localizedName + "(" + meta.name + ")";
    }

    private Prov<TextureRegion> findIcon(String name) {
        Prov<TextureRegion> icon = ContentResolver.findIcon(name);
        if (icon == null) {
            this.error("Icon <" + name + "> not found, so use a cross instead.");
            icon = NotFound;
        }

        return icon;
    }

    private static Effect composeMultiFx(List<String> names) {
        ArrayList<Effect> all = new ArrayList();

        for(String name : names) {
            Effect fx = ContentResolver.findFx(name);
            if (fx != null) {
                all.add(fx);
            }
        }

        return new MultiEffect((Effect[])all.toArray(EffectType));
    }

    @Nullable
    private static Effect parseFx(Object obj) {
        if (obj instanceof String) {
            return ContentResolver.findFx((String)obj);
        } else {
            return obj instanceof List ? composeMultiFx((List)obj) : null;
        }
    }

    @Nullable
    private static Object findValueByAlias(Map map, String... aliases) {
        for(String alias : aliases) {
            Object tried = map.get(alias);
            if (tried != null) {
                return tried;
            }
        }

        return null;
    }
}
