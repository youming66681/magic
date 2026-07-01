//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package magical.content;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.serialization.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;

public class ParserUtils {
    public static float parseFloat(@Nullable Object floatObj) {
        if (floatObj == null) {
            return 0.0F;
        } else if (floatObj instanceof Number) {
            return ((Number)floatObj).floatValue();
        } else {
            try {
                return Float.parseFloat((String)floatObj);
            } catch (Exception var2) {
                return 0.0F;
            }
        }
    }

    public static int parseInt(@Nullable Object intObj) {
        if (intObj == null) {
            return 0;
        } else if (intObj instanceof Number) {
            return ((Number)intObj).intValue();
        } else {
            try {
                return Integer.parseInt((String)intObj);
            } catch (Exception var2) {
                return 0;
            }
        }
    }

    public static Object parseJsonToObject(Object o) {
        if (o instanceof Seq seq) {
            ArrayList list = new ArrayList(seq.size);

            for(Object e : new Seq.SeqIterable(seq)) {
                list.add(parseJsonToObject(e));
            }

            return list;
        } else if (!(o instanceof ObjectMap objMap)) {
            return o instanceof JsonValue ? convert((JsonValue)o) : o;
        } else {
            HashMap map = new HashMap();
            ObjectMap.Entries var3 = (new ObjectMap.Entries(objMap)).iterator();

            while(var3.hasNext()) {
                ObjectMap.Entry<Object, Object> entry = (ObjectMap.Entry)var3.next();
                map.put(entry.key, parseJsonToObject(entry.value));
            }

            return map;
        }
    }

    @Nullable
    private static Object convert(JsonValue j) {
        JsonValue.ValueType type = j.type();
        switch (type) {
            case object:
                HashMap map = new HashMap();

                for(JsonValue cur = j.child; cur != null; cur = cur.next) {
                    map.put(cur.name, convert(cur));
                }

                return map;
            case array:
                ArrayList list = new ArrayList();

                for(JsonValue cur = j.child; cur != null; cur = cur.next) {
                    list.add(convert(cur));
                }

                return list;
            case stringValue:
                return j.asString();
            case doubleValue:
                return j.asDouble();
            case longValue:
                return j.asLong();
            case booleanValue:
                return j.asBoolean();
            case nullValue:
                return null;
            default:
                return Collections.emptyMap();
        }
    }

    public static String kebab2camel(String kebab) {
        StringBuilder sb = new StringBuilder();
        boolean hyphen = false;

        for(int i = 0; i < kebab.length(); ++i) {
            char c = kebab.charAt(i);
            if (c == '-') {
                hyphen = true;
            } else if (hyphen) {
                sb.append(Character.toUpperCase(c));
                hyphen = false;
            } else if (i == 0) {
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public static Object field(Class<?> type, String name) {
        try {
            Object b = type.getField(name).get((Object)null);
            if (b == null) {
                String var10002 = type.getSimpleName();
                throw new IllegalArgumentException(var10002 + ": not found: '" + name + "'");
            } else {
                return b;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ItemStack[] addItemStack(ItemStack[] stackArray, ItemStack stack) {
        ArrayList<ItemStack> newItemStack = new ArrayList(Arrays.asList(stackArray));
        newItemStack.add(stack);
        return (ItemStack[])newItemStack.toArray(stackArray);
    }

    public static LiquidStack[] addLiquidStack(LiquidStack[] stackArray, LiquidStack stack) {
        ArrayList<LiquidStack> newLiquidStack = new ArrayList(Arrays.asList(stackArray));
        newLiquidStack.add(stack);
        return (LiquidStack[])newLiquidStack.toArray(stackArray);
    }

    public static PayloadStack[] addPayloadStack(PayloadStack[] stackArray, PayloadStack stack) {
        ArrayList<PayloadStack> newPayloadStack = new ArrayList(Arrays.asList(stackArray));
        newPayloadStack.add(stack);
        return (PayloadStack[])newPayloadStack.toArray(stackArray);
    }
}
