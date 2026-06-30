package magical.content;

import arc.struct.Seq;
import mindustry.type.ItemStack;

public class Formula {

    public String name;
    public float craftTime = 60f;

    public Seq<ItemStack> inputs = new Seq<>();
    public Seq<ItemStack> outputs = new Seq<>();

    public Formula(String name){
        this.name = name;
    }
}