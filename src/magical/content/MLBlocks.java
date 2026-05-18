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
import mindustry.gen.Sounds;
import mindustry.world.blocks.environment.*;
import mindustry.world.turrets.PowerTurret;
import mindustry.entities.bullet.BasicBulletType;

import magical.content.MLItems;
import magical.content.MLUnitTypes;
import magical.content.MLTechTree;
import magical.content.MLPlanets;
import magical.content.MLFx;

public class MLBlocks {
    public static Block baseCore;
    public static Block phantomTitaniumSteelCompressor;
    public static Block xuanCrystalManufacturingMachine;
    public static Block phantomSteelCompressor;
    public static Block phantomSteelVoltageMachine;
    public static Block electroge;

    public static void load() {
        /*基础科技*/
        //基座核心
        baseCore = new baseCore("baseCore") {{
            requirements(Category.effect, ItemStack.with(new Object[]{MLItems.phantomTitaniumSteel, 200, MLItems.mysticCrystal, 200, MLItems.phantomLuminousAlloy, 100}));

            unitType = UnitTypes.alpha;
            health = 500;
            itemCapacity = 2000;
            size = 2;
            thrusterLength = 20f;

            unitCapModifier = 5;

        }};
        //幻钢压缩机
        phantomSteelCompressor = new GenericCrafter("phantomSteelCompressor"){{
            requirements(Category.crafting, ItemStack.with(new Object[]{Items.copper, 50, Items.lead, 50}));

            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(MLItems.phantomSteel, 1);
            craftTime = 120f;
            size = 2;
            hasItems = true;
            itemCapacity = 20;
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame());

            consumeItems(ItemStack.with(Items.copper, 2, Items.lead, 2));
        }};
        //幻钢电压机
        phantomSteelVoltageMachine = new GenericCrafter("phantomSteelVoltageMachine"){{
            requirements(Category.crafting, ItemStack.with(new Object[]{MLItems.phantomSteel, 25, Items.copper, 75, Items.lead, 75}));

            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(MLItems.phantomSteel, 2);
            craftTime = 60f;
            size = 2;
            hasItems = true;
            hasPower = true;
            itemCapacity = 20;
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame());

            consumeItems(ItemStack.with(Items.copper, 2, Items.lead, 2));
            consumePower(0.75f);
        }};
        //幻钛钢熔炼机
        phantomTitaniumSteelCompressor = new GenericCrafter("phantomTitaniumSteelCompressor"){{
            requirements(Category.crafting, ItemStack.with(new Object[]{MLItems.phantomSteel, 50, Items.titanium, 30, Items.graphite, 10}));

            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(MLItems.phantomTitaniumSteel, 1);
            craftTime = 60f;
            size = 2;
            hasItems = true;
            hasPower = true;
            itemCapacity = 10;
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame());

            consumeItems(ItemStack.with(MLItems.phantomSteel, 1, Items.titanium, 1));
            consumePower(1.0f);
        }};
        //玄晶混制机
        xuanCrystalManufacturingMachine = new GenericCrafter("xuanCrystalManufacturingMachine"){{
            requirements(Category.crafting, ItemStack.with(new Object[]{MLItems.phantomSteel, 20, Items.titanium, 30, Items.silicon, 20}));

            craftEffect = Fx.hitEmpSpark;
            outputItem = new ItemStack(MLItems.phantomTitaniumSteel, 1);
            craftTime = 45f;
            size = 2;
            hasItems = true;
            hasPower = true;
            itemCapacity = 10;
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame());

            consumeItems(ItemStack.with(MLItems.phantomSteel, 1, Items.coal, 1));
            consumePower(1.0f);
        }};
        //电戈
        electroge = new PowerTurret("electroge"){{
            requirements(Category.turret, with(MLItems.phantomSteel, 75, Items.graphite, 25));
            range = 200f;

            recoil = 2f;
            reload = 120f;
            shake = 1f;
            shootEffect = Fx.lancerLaserShoot;
            smokeEffect = Fx.smeltsmoke;
            size = 2;
            health = 600;
            rotateSpeed = 8;
            shootSound = Sounds.explosionAfflict;
            coolant = consumeCoolant(0.2f);

            consumePower(6f);

            shootType = new BasicBulletType(30){{
                hitEffect = MLFx.smallElectricDetonation;

                hitSize = 16f;
                damage = 30f;
                speed = 5f;
                width = 16f;
                height = 32f;
                lifetime = 40f;
                ammoMultiplier = 1f;
                trailLength = 6f;
                trailWidth = 3f;
                velocityRnd = 1f;
                trailColor = Color.valueOf("FEEBB3FF");
                frontColor = Color.valueOf("FEEBB3FF");
                backColor = Color.valueOf("FEEBB3FF");
                hitSound = Sounds.explosionCleroi;
            }};
        }};
    }
}