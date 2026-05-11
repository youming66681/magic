package magical.content;

import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.meta.BlockGroup;
import mindustry.content.Blocks;

import magical.content.MLItems;
import magical.content.MLUnitTypes;
import magical.content.MLTechTree;
import magical.content.MLPlanets;

public static Block jzhx;

public class MLBlocks {
    public static void load(){
        jzhx = new BaseCore("基座核心"){{
            requirements(Category.effect, with(MLItems.幻钛钢, 200, MLItems.玄晶, 200, MLItems.幻荧合金, 100));

            unitType = UnitTypes.alpha;
            health = 500;
            itemCapacity = 2000;
            size = 2;
            thrusterLength = 20/2f;

            unitCapModifier = 5;
        }};
        BaseCore.load();
    }
}