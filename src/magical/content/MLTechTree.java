package magical.content;

import mindustry.content.TechTree;
import mindustry.type.ItemStack;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLPlanets;

public class MLTechTree {
    public static void load() {
        MLPlanets.cecilia.techTree = ceciliaRoot("cecilia", coreShard, () -> {
                null,
                MLBlocks.baseCore,
                ItemStack.empty
        );

        ceciliaRoot.name = "cecilia";

        TechTree.roots.add(ceciliaRoot);
    }
}
