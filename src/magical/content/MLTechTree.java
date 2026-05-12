package magical.content;

import mindustry.content.TechTree;
import mindustry.type.ItemStack;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLPlanets;

public class MLTechTree {
    public static void load(){
        TechTree.TechNode ceciliaRoot = new TechTree.TechNode(null, null, ItemStack.empty);
        ceciliaRoot.name = "cecilia";
        ceciliaRoot.alwaysUnlocked = true;

        TechTree.TechNode coreNode = new TechTree.TechNode(
                ceciliaRoot,
                MLBlocks.baseCore,
                ItemStack.empty
        );
        ceciliaRoot.children.add(coreNode);

        TechTree.roots.add(ceciliaRoot);
    }
}
