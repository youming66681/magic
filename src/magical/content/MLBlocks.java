package magical.content;

import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.Tile;
import mindustry.game.Team;
import mindustry.content.UnitTypes;
import mindustry.type.ItemStack;

import magical.content.MLItems;
import magical.content.MLUnitTypes;
import magical.content.MLTechTree;
import magical.content.MLPlanets;

public class MLBlocks {
    public static Block baseCore;

    public static void load(){
        baseCore = new CoreBlock("baseCore"){{
            requirements(Category.effect, ItemStack.with(new Object[]{MLItems.phantomTitaniumSteel, 200, MLItems.mysticCrystal, 200, MLItems.phantomLuminousAlloy, 100}));

            unitType = UnitTypes.alpha;
            health = 500;
            itemCapacity = 2000;
            size = 2;
            thrusterLength = 20f;

            unitCapModifier = 5;

            baseCore.deconstructable = true;

            public boolean canDelete (Tile tile){
                return tile.team.cores().size > 1;
            }
        }};
    }
}