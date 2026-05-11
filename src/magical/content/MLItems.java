package magical.content;

import arc.graphics.*;
//import javafx.scene.paint.Color;
import mindustry.type.*;

import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLTechTree;
import magical.content.MLPlanets;

public class MLItems {
    public static Item yjbl, xj, hg, htg, ys, xg, nmnls, jt, yys, yjjs, hyhj, nmthj;

    public static void load() {
        yjbl = new Item("有机玻璃", Color.valueOf("404040FF")) {{
            hardness = 1;
            cost = 1.0F;
        }};

        xj = new Item("玄晶", Color.valueOf("46649AFF")) {{
            description = "一种奇特的晶体";
            hardness = 2;
            cost = 1.0F;
        }};

        hg = new Item("幻钢", Color.valueOf("97B5EDFF")) {{
            description = "用于建造幻境工业的开端";
            hardness = 3;
            cost = 1.0F;
        }};

        htg = new Item("幻钛钢", Color.valueOf("46649AFF")) {{
            description = "强度很高的合成材料";
            hardness = 4;
            cost = 1.0F;
        }};

        ys = new Item("翼石", Color.valueOf("9C88C3FF")) {{
            hardness = 4;
            explosiveness = 0.1F;
            cost = 1.0F;
            charge = 2.0F;
        }};

        xg = new Item("玄钢", Color.valueOf("7595D2FF")) {{
            description = "比较烫手";
            hardness = 5;
            cost = 1.0F;
            flammability = 0.1F;
        }};

        nmnls = new Item("纳米能量丝", Color.valueOf("CCCEDBFF")) {{
            description = "纳米丝织材料";
            hardness = 5;
            cost = 1.0F;
            radioactivity = 2.6F;
            explosiveness = 5.0F;
            charge = 10.0F;
        }};

        jt = new Item("晶碳", Color.valueOf("8AA3F4FF")) {{
            description = "比较烫手";
            hardness = 6;
            cost = 1.0F;
            flammability = 5.0F;
            charge = 2.5F;
        }};

        yys = new Item("荧羽石", Color.valueOf("CCCEDBFF")) {{
            description = "一种通体荧光白色的材料，蕴含大量能量";
            hardness = 6;
            cost = 1.0F;
            radioactivity = 2.0F;
            explosiveness = 5.5F;
            charge = 10.0F;
        }};

        yjjs = new Item("翼精金属", Color.valueOf("8175A5FF")) {{
            description = "羿石中和了一些易放电的属性，变成了更加稳定的一种金属";
            hardness = 7;
            cost = 1.0F;
            charge = 1F;
        }};

        hyhj = new Item("幻荧合金", Color.valueOf("6569C9FF")) {{
            description = "一种强度很高的合金";
            hardness = 8;
            cost = 1.0F;
        }};

        nmthj = new Item("纳米碳合金", Color.valueOf("D1EFFFFF")) {{
            description = "最坚硬的材料";
            hardness = 20;
            cost = 1.0F;
            radioactivity = 2.6F;
            explosiveness = 6.0F;
            flammability = 1.0F;
            charge = 12.5F;
        }};
    }
}