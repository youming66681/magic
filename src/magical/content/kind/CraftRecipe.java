package magical.content;

public class CraftRecipe{

    public ItemStack[] consumeItems = ItemStack.empty;
    public LiquidStack[] consumeLiquids = LiquidStack.empty;

    public ItemStack[] outputItems = ItemStack.empty;
    public LiquidStack outputLiquid;

    public float craftTime = 60f;
    public float power = 0f;

    public Effect craftEffect = Fx.none;

    public CraftRecipe(){}
}