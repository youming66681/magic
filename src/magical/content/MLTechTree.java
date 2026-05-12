package magical.content;

import mindustry.content.TechTree;
import mindustry.ctype.UnlockableContent;
import mindustry.type.TechTree.TechNode;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLPlanets;

public class MLTechTree {
    public static void load(){
        TechTree.TechNode ceciliaRoot = new TechTree.TechNode(null, null, "cecilia");

        TechTree.TechNode coreNode = new TechTree.TechNode(ceciliaRoot, MLBlocks.baseCore, null);
        ceciliaRoot.children.add(coreNode);

        TechTree.roots.add(ceciliaRoot);
    }
}
