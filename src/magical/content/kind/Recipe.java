package magical.content;

import mindustry.type.ItemStack;

public class Recipe {

    public String key;
    public float craftTime;
    public ItemStack[] input;
    public ItemStack[] output;

    public Recipe(String key, float craftTime, ItemStack[] input, ItemStack[] output){
        this.key = key;
        this.craftTime = craftTime;
        this.input = input;
        this.output = output;
    }
}