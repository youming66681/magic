package magical.content;

import mindustry.type.ItemStack;

public class Recipe {

    public float craftTime;
    public ItemStack[] input;
    public ItemStack[] output;

    public Recipe(float craftTime, ItemStack[] input, ItemStack[] output){
        this.craftTime = craftTime;
        this.input = input;
        this.output = output;
    }
}