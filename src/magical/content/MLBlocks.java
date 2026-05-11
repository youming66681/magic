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
        jzhx = new ABaseCore("基座核心"){{
            requirements(Category.effect, with(Items.幻钛钢, 200, Items.玄晶, 200, Items.幻荧合金, 100));

            unitType = UnitTypes.alpha;
            health = 500;
            itemCapacity = 2000;
            size = 2;
            thrusterLength = 20/2f;

            unitCapModifier = 5;
        }};
    }
}