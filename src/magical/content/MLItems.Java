//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package magical.content;

/*import arc.graphics.Color;
import mindustry.content.Items;
import mindustry.type.Item;*/

public class MLItems {
    public static Item HG;
    public static Item HTG;
    public static Item XJ;

    public static void load() {
        Items.fissileMatter.hidden = false;
        HG = new Item("幻钢", Color.valueOf("97B5EDFF")) {
            {
                this.description = "用于建造幻境工业的开端";
                this.hardness = 3;
                this.cost = 1.0F;
            }
        };
        HTG = new Item("幻钛钢", Color.valueOf("46649AFF")) {
            {
                this.description = "强度很高的合成材料";
                this.hardness = 4;
                this.cost = 1.0F;
            }
        };
        XJ = new Item("玄晶", Color.valueOf("92DD7EFF")) {
            {
                this.description = "一种奇特的晶体";
                this.hardness = 3;
                this.cost = 1.0F;
            }
        };
    }
}
