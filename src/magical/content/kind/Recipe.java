package magical.content;

import arc.struct.Seq;
import mindustry.type.Item;
import mindustry.type.ItemStack;

public class Recipe {

    public Input input;
    public Output output;

    public float craftTime;

    public static class Input {
        public ItemStack[] items;
        public float power;
    }

    public static class Output {
        public ItemStack[] items;
    }
}