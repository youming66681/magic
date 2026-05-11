package magical.content;

import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.Tile;
import mindustry.game.Team;
import mindustry.content.UnitTypes;

import magical.content.MLItems;
import magical.content.MLUnitTypes;
import magical.content.MLTechTree;
import magical.content.MLPlanets;

import magical.blocks.ABaseCore;

public class MLBlocks {
    public static Block jzhx;

    public static void load(){
        jzhx = new CoreBlock("基座核心"){{
            requirements(Category.effect, ItemStack.with(new Object[]{(MLItems.htg, 200, MLItems.xj, 200, MLItems.hyhj, 100}));

            unitType = UnitTypes.alpha;
            health = 500;
            itemCapacity = 2000;
            size = 2;
            thrusterLength = 20f;

            unitCapModifier = 5;

        }};
    }
}