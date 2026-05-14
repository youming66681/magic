package magical.content;

import static mindustry.content.TechTree.*;
import mindustry.type.ItemStack;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLPlanets;

public class MLTechTree {
    public static void load() {
        MLPlanets.cecilia.techTree = nodeRoot("cecilia", MLBlocks.baseCore, () -> {

            node(MLBlocks.phantomTitaniumSteelCompressor, () -> {
            });
        });
    }
}
