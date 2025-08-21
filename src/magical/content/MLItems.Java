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
        public static Item hg = new Item("幻钢", Color.valueOf("97B5EDFF")) {
            {
                description = "用于建造幻境工业的开端";
                hardness = 3;
                cost = 1.0f;
            }
        };
        public static Item htg = new Item("幻钛钢", Color.valueOf("46649AFF")) {
            {
                description = "强度很高的合成材料";
                hardness = 4;
                cost = 1.0f;
            }
        };
        public static Item xj = new Item("玄晶", Color.valueOf("2A2A2AFF")) {
            {
                description = "强度很高的合成材料";
                hardness = 3;
                cost = 1.0f;
            }
        };
        public static Item jt = new Item("晶碳", Color.valueOf("8AA3F4FF")) {
            {
                description = "高强度材料";
                hardness = 6;
                cost = 1.0f;
                explosivenes = 1.0f;
                flammability = 5.0f;
                charge = 2.5f;
            }
        };

    }
}