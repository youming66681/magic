package magical.content;

import mindustry.content.TechTree;
import mindustry.type.ItemStack;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLPlanets;

public class MLTechTree {
    public static void load() {
        TechTree.TechNode ceciliaRoot = new TechTree.TechNode(
                null,
                MLBlocks.baseCore,
                ItemStack.empty
        );

        TechTree.TechNode phantomTitaniumSteelCompressor = new TechTree.TechNode(
                MLBlocks.baseCore,
                MLBlocks.phantomTitaniumSteelCompressor,
                ItemStack.with(MLItems.phantomSteel, 5000, Items.titanium, 3000, Items.graphite, 1000)
        );

        ceciliaRoot.name = "cecilia";

        TechTree.roots.add(ceciliaRoot);
    }
}
