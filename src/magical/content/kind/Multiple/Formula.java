package magical.world.blocks.production;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import mindustry.entities.Effect;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.draw.DrawBlock;

public class Formula{

    public ItemStack[] inputItems = ItemStack.empty;
    public ItemStack[] outputItems = ItemStack.empty;

    public LiquidStack[] inputLiquids = LiquidStack.empty;
    public LiquidStack[] outputLiquids = LiquidStack.empty;

    public float craftTime = 60f;
    public float powerUse = 0f;

    public Effect craftEffect = Effect.none;
    public Effect updateEffect = Effect.none;
    public float updateEffectChance = 0.04f;

    public DrawBlock drawer;

    public TextureRegion icon;

    public Color color = Color.white.cpy();

    public String name = "";
    public String description = "";

    public Formula input(ItemStack... stacks){
        inputItems = stacks;
        return this;
    }

    public Formula output(ItemStack... stacks){
        outputItems = stacks;
        return this;
    }

    public Formula inputLiquid(LiquidStack... stacks){
        inputLiquids = stacks;
        return this;
    }

    public Formula outputLiquid(LiquidStack... stacks){
        outputLiquids = stacks;
        return this;
    }

    public Formula craftTime(float craftTime){
        this.craftTime = craftTime;
        return this;
    }

    public Formula power(float power){
        this.powerUse = power;
        return this;
    }

    public Formula craftEffect(Effect effect){
        craftEffect = effect;
        return this;
    }

    public Formula updateEffect(Effect effect, float chance){
        updateEffect = effect;
        updateEffectChance = chance;
        return this;
    }

    public Formula drawer(DrawBlock drawer){
        this.drawer = drawer;
        return this;
    }

    public Formula icon(TextureRegion region){
        icon = region;
        return this;
    }

    public Formula color(Color color){
        this.color = color;
        return this;
    }

    public Formula name(String name){
        this.name = name;
        return this;
    }

    public Formula description(String description){
        this.description = description;
        return this;
    }

}