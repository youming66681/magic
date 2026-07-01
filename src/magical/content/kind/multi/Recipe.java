//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package magical.content;

import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.util.Nullable;
import mindustry.content.Fx;
import mindustry.entities.Effect;

public class Recipe {
    public IOEntry input;
    public IOEntry output;
    public float craftTime = 0.0F;
    @Nullable
    public Prov<TextureRegion> icon;
    @Nullable
    public Color iconColor;
    public Effect craftEffect;

    public Recipe() {
        this.craftEffect = Fx.none;
    }

    public void cacheUnique() {
        this.input.cacheUnique();
        this.output.cacheUnique();
    }

    public boolean isConsumeItem() {
        return this.input.items.length > 0;
    }

    public boolean isOutputItem() {
        return this.output.items.length > 0;
    }

    public boolean isConsumeFluid() {
        return this.input.fluids.length > 0;
    }

    public boolean isOutputFluid() {
        return this.output.fluids.length > 0;
    }

    public boolean isConsumePower() {
        return this.input.power > 0.0F;
    }

    public boolean isOutputPower() {
        return this.output.power > 0.0F;
    }

    public boolean isConsumeHeat() {
        return this.input.heat > 0.0F;
    }

    public boolean isOutputHeat() {
        return this.output.heat > 0.0F;
    }

    public boolean isConsumePayload() {
        return this.input.payloads.length > 0;
    }

    public boolean isOutputPayload() {
        return this.output.payloads.length > 0;
    }

    public boolean hasItems() {
        return this.isConsumeItem() || this.isOutputItem();
    }

    public boolean hasFluids() {
        return this.isConsumeFluid() || this.isOutputFluid();
    }

    public boolean hasPower() {
        return this.isConsumePower() || this.isOutputPower();
    }

    public boolean hasHeat() {
        return this.isConsumeHeat() || this.isOutputHeat();
    }

    public boolean hasPayloads() {
        return this.isConsumePayload() || this.isOutputPayload();
    }

    public int maxItemAmount() {
        return Math.max(this.input.maxItemAmount(), this.output.maxItemAmount());
    }

    public float maxFluidAmount() {
        return Math.max(this.input.maxFluidAmount(), this.output.maxFluidAmount());
    }

    public float maxPower() {
        return Math.max(this.input.power, this.output.power);
    }

    public float maxHeat() {
        return Math.max(this.input.heat, this.output.heat);
    }

    public int maxPayloadAmount() {
        return Math.max(this.input.maxPayloadAmount(), this.output.maxPayloadAmount());
    }

    public String toString() {
        String var10000 = String.valueOf(this.input);
        return "Recipe{input=" + var10000 + "output=" + String.valueOf(this.output) + "craftTime" + this.craftTime + "}";
    }
}
