package magical.content;

import mindustry.content.TechTree;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLPlanets;

public class MLTechTree {
    public static void load(){
        TechTree.Node ceciliaRoot = TechTree.root("cecilia", node -> {
            node.block(MLBlocks.baseCore);
        });

        TechTree.roots.add(ceciliaRoot);
    }
}
