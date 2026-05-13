package magical.content;

import arc.Core;
import mindustry.content.UnitTypes;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BlockGroup;
import mindustry.content.TechTree;
import mindustry.content.Items;
import mindustry.content.Fx;
import mindustry.world.draw.DrawMulti;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawFlame;
import arc.graphics.Color;
import mindustry.world.blocks.production.GenericCrafter;

import magical.content.MLItems;
import magical.content.MLUnitTypes;
import magical.content.MLTechTree;
import magical.content.MLPlanets;

public class MLBlocks {
    public static Block baseCore;
    public static Block phantomTitaniumSteelCompressor;

    public static void load() {
        baseCore = new baseCore("baseCore") {{
            requirements(Category.effect, ItemStack.with(new Object[]{MLItems.phantomTitaniumSteel, 200, MLItems.mysticCrystal, 200, MLItems.phantomLuminousAlloy, 100}));

            unitType = UnitTypes.alpha;
            health = 500;
            itemCapacity = 2000;
            size = 2;
            thrusterLength = 20f;

            unitCapModifier = 5;

        }};

        phantomTitaniumSteelCompressor = new GenericCrafter("phantom-titanium-steel-compressor"){{
            requirements(Category.crafting, ItemStack.with(new Object[]{MLItems.phantomSteel, 50, Items.titanium, 30, Items.graphite, 10}));

            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(MLItems.phantomTitaniumSteel, 1);
            craftTime = 60f;
            size = 2;
            hasItems = true;
            hasPower = true;
            itemCapacity = 10;
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame());
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.05f;

            TechTree.TechNode phantomTitaniumSteelCompressor = new TechTree.TechNode(
                    MLBlocks.baseCore,
                    MLBlocks.phantomTitaniumSteelCompressor,
                    ItemStack.with(new Object[]{MLItems.phantomSteel, 5000, Items.titanium, 3000, Items.graphite, 1000})
            );

            consumeItem(ItemStack.with(new Object[]{(MLItems.phantomSteel, 1, Items.titanium, 1}));
            consumePower(1.0f);
        }};
    }
}