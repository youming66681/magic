package magical.content;

import mindustry.content.Blocks;
import magical.content.MLItems;

public class MLBlocks {
    public static 幻钢矿 = new OreBlock(Items.幻钢){{
        variants = 4;
        oreDefault = true;
        oreThreshold = 0.8f;
        oreScale = 21.0f;
    }};
    public static 石墨矿 = new OreBlock(Items.graphite){{
        variants = 3;
        oreDefault = true;
        oreThreshold = 0.8f;
        oreScale = 21.0f;
    }};
    public static 翼石矿 = new OreBlock(Items.翼石){{
        variants = 5;
        oreDefault = true;
        oreThreshold = 0.9f;
        oreScale = 23.0f;
    }};
    public static 荧羽石矿 = new OreBlock(Items.荧羽石){{
        variants = 6;
        oreDefault = true;
        oreThreshold = 0.8f;
        oreScale = 18.0f;
    }};
    public static void load(){}
}