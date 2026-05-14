package magical.content;

import static mindustry.content.TechTree.*;
import mindustry.type.ItemStack;
import mindustry.content.Planets;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLPlanets;

public class MLTechTree {
    public static void load() {
        MLPlanets.cecilia.techTree = Planets.serpulo.techTree;

        TechNode root = nodeRoot("cecilia", MLBlocks.baseCore, () -> {

              node(MLBlocks.phantomTitaniumSteelCompressor, () -> {
                node(MLBlocks.xuanCrystalManufacturingMachine, () -> {
                });
            });
        });
    }
}
