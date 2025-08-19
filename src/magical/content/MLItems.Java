//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package magical.content;

import arc.graphics.Color;
import mindustry.graphics.Pal;
import mindustry.type.Item;

public class MLItems {
    public static void load() {
        public static Item phantomSteel = new Item("幻钢", Color.valueOf("97B5EDFF")) {
            {
                this.description = "用于建造幻境工业的开端";
                this.hardness = 3;
                this.cost = 1.0F;
            }
        };
        public static Item phantomTitaniumSteel = new Item("幻钛钢", Color.valueOf("46649AFF")) {
            {
                this.description = "强度很高的合成材料";
                this.hardness = 4;
                this.cost = 1.0F;
            }
        };
        public static Item xuanJing = new Item("玄晶", Color.valueOf("2A2A2AFF")) {
            {
                this.description = "强度很高的合成材料";
                this.hardness = 3;
                this.cost = 1.0F;
            }
        };

    }
}