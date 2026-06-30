package magical.content;

public static class Recipe{
    public float craftTime = 60f;
    public ItemStack[] input;
    public ItemStack[] output;

    public Recipe(String name, float craftTime, ItemStack[] input, ItemStack[] output){
        this.craftTime = craftTime;
        this.input = input;
        this.output = output;
    }
}
