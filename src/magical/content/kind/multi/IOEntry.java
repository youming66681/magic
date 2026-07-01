//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package magical.content;

import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectSet;
import arc.util.Nullable;
import mindustry.ctype.UnlockableContent;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;

public class IOEntry {
    public ItemStack[] items;
    public LiquidStack[] fluids;
    public float power;
    public float heat;
    public PayloadStack[] payloads;
    public ObjectSet<Item> itemsUnique;
    public ObjectSet<Liquid> fluidsUnique;
    public ObjectSet<UnlockableContent> payloadsUnique;
    @Nullable
    public Prov<TextureRegion> icon;
    @Nullable
    public Color iconColor;

    public IOEntry() {
        this.items = ItemStack.empty;
        this.fluids = LiquidStack.empty;
        this.power = 0.0F;
        this.heat = 0.0F;
        this.payloads = new PayloadStack[0];
        this.itemsUnique = new ObjectSet();
        this.fluidsUnique = new ObjectSet();
        this.payloadsUnique = new ObjectSet();
    }

    public void cacheUnique() {
        for(ItemStack item : this.items) {
            this.itemsUnique.add(item.item);
        }

        for(LiquidStack fluid : this.fluids) {
            this.fluidsUnique.add(fluid.liquid);
        }

        for(PayloadStack payload : this.payloads) {
            this.payloadsUnique.add(payload.item);
        }

    }

    public boolean isEmpty() {
        return this.items.length == 0 && this.fluids.length == 0 && this.power <= 0.0F && this.heat <= 0.0F && this.payloads.length == 0;
    }

    public int maxItemAmount() {
        int max = 0;

        for(ItemStack item : this.items) {
            max = Math.max(item.amount, max);
        }

        return max;
    }

    public float maxFluidAmount() {
        float max = 0.0F;

        for(LiquidStack fluid : this.fluids) {
            max = Math.max(fluid.amount, max);
        }

        return max;
    }

    public int maxPayloadAmount() {
        int max = 0;

        for(PayloadStack payload : this.payloads) {
            max = Math.max(payload.amount, max);
        }

        return max;
    }

    public String toString() {
        String var10000 = String.valueOf(this.items);
        return "IOEntry{items=" + var10000 + "fluids=" + String.valueOf(this.fluids) + "power=" + this.power + "heat=" + this.heat + "payloads=" + String.valueOf(this.payloads) + "}";
    }
}
