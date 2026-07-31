package magical.content;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.Sized;
import mindustry.entities.UnitSorts;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.MissileBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.PointLaserBulletType;
import mindustry.entities.bullet.ShrapnelBulletType;
import mindustry.entities.bullet.LightningBulletType;
import mindustry.entities.bullet.PointBulletType;
import mindustry.entities.part.DrawPart;
import mindustry.entities.part.HaloPart;
import mindustry.entities.part.RegionPart;
import mindustry.entities.part.DrawPart.PartProgress;
import mindustry.entities.pattern.ShootAlternate;
import mindustry.entities.pattern.ShootBarrel;
import mindustry.entities.pattern.ShootMulti;
import mindustry.entities.pattern.ShootPattern;
import mindustry.entities.pattern.ShootSine;
import mindustry.entities.pattern.ShootSpread;
import mindustry.entities.pattern.ShootSummon;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Healthc;
import mindustry.gen.Hitboxc;
import mindustry.gen.Sounds;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.MendProjector;
import mindustry.world.blocks.defense.turrets.ContinuousTurret;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LaserTurret;
import mindustry.world.blocks.defense.turrets.PointDefenseTurret;
import mindustry.world.blocks.defense.turrets.LiquidTurret;
import mindustry.world.blocks.defense.turrets.ContinuousLiquidTurret;
import mindustry.entities.bullet.ContinuousFlameBulletType;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.Prop;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.environment.SteamVent;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.blocks.sandbox.ItemSource;
import mindustry.world.blocks.sandbox.LiquidSource;
import mindustry.world.consumers.ConsumeCoolant;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.draw.DrawArcSmelt;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawCrucibleFlame;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawFlame;
import mindustry.world.draw.DrawGlowRegion;
import mindustry.world.draw.DrawLiquidTile;
import mindustry.world.draw.DrawMulti;
import mindustry.world.draw.DrawPlasma;
import mindustry.world.draw.DrawRegion;
import mindustry.world.draw.DrawTurret;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.distribution.Conveyor;
import mindustry.world.blocks.distribution.Junction;
import mindustry.world.blocks.distribution.BufferedItemBridge;
import mindustry.world.blocks.storage.Unloader;
import mindustry.world.meta.BlockGroup;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.power.Battery;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.blocks.power.ThermalGenerator;
import mindustry.world.blocks.power.SolarGenerator;
import mindustry.world.blocks.power.ImpactReactor;
import mindustry.world.blocks.power.NuclearReactor;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.draw.DrawWarmupRegion;
import mindustry.world.blocks.defense.Wall;
import mindustry.type.UnitType;
import mindustry.content.UnitTypes;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.type.LiquidStack;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.blocks.units.UnitFactory.UnitPlan;
import arc.struct.Seq;
import mindustry.type.PayloadStack;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.entities.effect.ParticleEffect;
import arc.graphics.Color;
import mindustry.world.draw.DrawTurret;
import mindustry.entities.part.RegionPart;
import mindustry.entities.part.HaloPart;
import mindustry.entities.Effect;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.effect.WaveEffect;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.blocks.payloads.Constructor;
import mindustry.world.blocks.payloads.PayloadDeconstructor;
import mindustry.entities.part.ShapePart;

public class MLBlocks {

    public static Block
            //基础科技
            baseCore, phantomTitaniumSteelCompressor, xuanCrystalManufacturingMachine, phantomSteelCompressor, phantomSteelVoltageMachine, electroge,
            fluvialErosion, adaptiveWall, largeAdaptiveWall, Birefringence, phantomSteelDrill, phantomSteelConveyor, phantomSteelBridge, phantomSteeljunction,
            phantomSteelUnloader, phantomSteelPowerNode, phantomTitaniumSteelPowerNode, excitedYuan, fuelPoweredGenerator, phantomTitaniumSteelConveyor,
            phantomSteelWall, largePhantomSteelWall, phantomTitaniumSteelWall, largePhantomTitaniumSteelWall, curvatureEvolutionPod, quantumFactory, chipMachine,
            BasicManufacturingPlant,
            //进阶科技
            starHarborShipbuildingCenter, baseStationCore, WingStonePunchingMachine, metaglassBooster, LightDescends, PhantomCrystal,LargePlastaniumCompressor,
            Thundercloud, BreakingArmy, Nebula, wingWall, LargeWingWall, ElectromagneticFissionReactor, StarshipMaterialConstructor, StarshipMaterialDeconstructor,
            //高端科技
            TerminalCore, WingEssenceMetalSynthesizer, PhantomSteelIncinerator, XuansteelMixer, LuminFeatherStoneReactor, PhantomGlowAlloyCombiner, LargePhaseWeaver,
            LargeSurgeSmelter, BulletsRain;

    public static void load() {

        //我超，盒
        //基座核心
        baseCore = new baseCore("baseCore") {{
            requirements(Category.effect, ItemStack.with(new Object[]{MLItems.phantomTitaniumSteel, 200, MLItems.mysticCrystal, 200, MLItems.nanoCarbonAlloy, 100}));

            unitType = UnitTypes.alpha;
            health = 500;
            itemCapacity = 2000;
            size = 2;
            alwaysUnlocked = true;

            unitCapModifier = 5;

        }};
        //基站核心
        baseStationCore = new CoreBlock("baseStationCore") {{
            requirements(Category.effect, ItemStack.with(new Object[]{MLItems.phantomSteel, 5000, Items.silicon, 2000, MLItems.phantomTitaniumSteel, 1000, MLItems.mysticCrystal, 1000, MLItems.logicChip, 500}));

            unitType = MLUnitTypes.Popular;
            health = 5000;
            itemCapacity = 12000;
            armor = 5;
            size = 4;

            unitCapModifier = 24;

        }};
        //终端核心
        TerminalCore = new CoreBlock("TerminalCore") {{
            requirements(Category.effect, ItemStack.with(new Object[]{MLItems.wingedStone, 6000, Items.silicon, 6000, MLItems.acrylic, 6000, MLItems.arrayChip, 1500}));

            unitType = MLUnitTypes.SpinningSpear;
            health = 12000;
            itemCapacity = 18000;
            armor = 12;
            size = 6;

            unitCapModifier = 36;

        }};
        //强强
        //厂子
        //幻钢压缩机
        phantomSteelCompressor = new GenericCrafter("phantomSteelCompressor") {{
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
        phantomSteelVoltageMachine = new GenericCrafter("phantomSteelVoltageMachine") {{
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
        phantomTitaniumSteelCompressor = new GenericCrafter("phantomTitaniumSteelCompressor") {{
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
        xuanCrystalManufacturingMachine = new GenericCrafter("xuanCrystalManufacturingMachine") {{
            requirements(Category.crafting, ItemStack.with(new Object[]{MLItems.phantomSteel, 20, Items.titanium, 30, Items.silicon, 20}));

            craftEffect = Fx.hitEmpSpark;
            outputItem = new ItemStack(MLItems.mysticCrystal, 1);
            craftTime = 45f;
            size = 2;
            hasItems = true;
            hasPower = true;
            itemCapacity = 10;
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame());

            consumeItems(ItemStack.with(MLItems.phantomSteel, 1, Items.coal, 1));
            consumePower(1.0f);
        }};
        //芯片制造机
        chipMachine = new magical.content.MultiCrafter("chipMachine"){{
            requirements(Category.crafting, ItemStack.with(MLItems.phantomSteel, 30, MLItems.phantomTitaniumSteel, 10, MLItems.mysticCrystal, 20, Items.silicon, 40, Items.metaglass, 50));
            consumePower(15f);
            health = 400;
            itemCapacity = 20;
            size = 4;
            liquidCapacity = 40;
            canOverdrive = false;
            hasItems = true;
            solid = true;
            useBlockDrawer = true;
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame());
            hasPower = true;
            craftPlans = Seq.with(
                    new CraftPlan(){{
                        craftTime = 30f;
                        consumePower(15f);
                        consumeItems(ItemStack.with(MLItems.phantomSteel, 1, MLItems.phantomTitaniumSteel, 1, MLItems.mysticCrystal, 1, Items.silicon, 3));
                        outputItems = ItemStack.with(MLItems.logicChip, 1);
                    }},
            new CraftPlan(){{
                craftTime = 60f;
                consumePower(20f);
                consumeItems(ItemStack.with(MLItems.wingedStone, 1, MLItems.acrylic, 1, Items.silicon, 6));
                outputItems = ItemStack.with(MLItems.arrayChip, 1);
            }}
            );
        }};
        //翼石冲压机
        WingStonePunchingMachine = new GenericCrafter("WingStonePunchingMachine") {{
            requirements(Category.crafting, ItemStack.with(new Object[]{Items.metaglass, 25, Items.silicon, 50, Items.titanium, 25, MLItems.logicChip, 10}));

            craftEffect = Fx.hitEmpSpark;
            outputItem = new ItemStack(MLItems.wingedStone, 1);
            craftTime = 90f;
            health = 200;
            size = 2;
            hasItems = true;
            hasPower = true;
            itemCapacity = 15;
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame());

            consumeItems(ItemStack.with(MLItems.phantomSteel, 1, Items.graphite, 1));
            consumePower(1.5f);
        }};
        //钢化玻璃强化器
        metaglassBooster = new GenericCrafter("metaglassBooster") {{
            requirements(Category.crafting, ItemStack.with(new Object[]{Items.metaglass, 15, Items.silicon, 40, MLItems.phantomSteel, 30, MLItems.logicChip, 10}));

            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(MLItems.acrylic, 1);
            craftTime = 120f;
            health = 200;
            size = 2;
            hasItems = true;
            hasLiquids = true;
            hasPower = true;
            itemCapacity = 15;
            liquidCapacity = 20;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.oil), new DrawDefault(), new DrawFlame());

            consumeItems(ItemStack.with(Items.metaglass, 1));
            consumeLiquid(Liquids.oil, 0.2f);
            consumePower(1.5f);
        }};
        //大塑钢
        LargePlastaniumCompressor = new GenericCrafter("LargePlastaniumCompressor") {{
            requirements(Category.crafting, ItemStack.with(new Object[]{Items.silicon, 100, Items.lead, 130, Items.graphite, 80, Items.titanium, 100, MLItems.logicChip, 15}));

            hasItems = true;
            liquidCapacity = 60f;
            craftTime = 60f;
            outputItem = new ItemStack(Items.plastanium, 2);
            size = 3;
            health = 640;
            hasPower = hasLiquids = true;
            craftEffect = Fx.formsmoke;
            updateEffect = Fx.plasticburn;
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame());

            consumeLiquid(Liquids.oil, 0.25f);
            consumePower(6f);
            consumeItem(Items.titanium, 2);
        }};
        //翼精金属合成器
        WingEssenceMetalSynthesizer = new GenericCrafter("WingEssenceMetalSynthesizer") {{
            requirements(Category.crafting, ItemStack.with(new Object[]{MLItems.mysticSteel, 25, Items.plastanium, 30, Items.thorium, 40, Items.silicon, 40, MLItems.arrayChip, 15}));

            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(MLItems.wingedMetal, 1);
            craftTime = 80f;
            health = 600;
            size = 3;
            hasItems = true;
            hasPower = true;
            itemCapacity = 20;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawDefault(), new DrawFlame());

            consumeItems(ItemStack.with(MLItems.wingedStone, 1, Items.plastanium, 1));
            consumePower(3f);
        }};
        //幻钢焚烧机
        PhantomSteelIncinerator = new GenericCrafter("PhantomSteelIncinerator") {{
            requirements(Category.crafting, ItemStack.with(new Object[]{MLItems.acrylic, 30, Items.silicon, 25, MLItems.arrayChip, 10}));

            updateEffect = Fx.freezing;
            outputLiquid = new LiquidStack(MLLiquids.PhantomSteelSolution, 0.5f);
            craftTime = 60f;
            health = 400;
            size = 2;
            hasItems = true;
            hasLiquids = true;
            hasPower = true;
            itemCapacity = 20;
            liquidCapacity = 40;
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame());

            consumeItems(ItemStack.with(MLItems.phantomSteel, 2));
            consumePower(3f);
        }};
        //玄钢混制机
        XuansteelMixer = new GenericCrafter("XuansteelMixer") {{
            requirements(Category.crafting, ItemStack.with(new Object[]{Items.silicon, 80, MLItems.mysticCrystal, 30, MLItems.phantomTitaniumSteel, 35, MLItems.acrylic, 40, MLItems.arrayChip, 15}));

            hasItems = true;
            liquidCapacity = 60f;
            itemCapacity = 20;
            craftTime = 150f;
            outputItem = new ItemStack(MLItems.mysticSteel, 1);
            size = 3;
            health = 600;
            hasPower = hasLiquids = true;
            updateEffect = Fx.hitEmpSpark;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(MLLiquids.PhantomSteelSolution), new DrawDefault(), new DrawFlame());

            consumeLiquid(MLLiquids.PhantomSteelSolution, 0.15f);
            consumePower(2f);
            consumeItem(MLItems.mysticCrystal, 1);
        }};
        //荧羽石反应器
        LuminFeatherStoneReactor = new GenericCrafter("LuminFeatherStoneReactor") {{
            requirements(Category.crafting, ItemStack.with(new Object[]{Items.silicon, 50, MLItems.mysticSteel, 15, MLItems.phantomTitaniumSteel, 40, MLItems.acrylic, 20, Items.thorium, 30, MLItems.arrayChip, 15}));

            hasItems = true;
            itemCapacity = 20;
            craftTime = 90f;
            outputItem = new ItemStack(MLItems.fluorescentFeatherStone, 1);
            size = 3;
            health = 600;
            hasPower = true;
            updateEffect = Fx.hitEmpSpark;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawDefault(), new DrawFlame());

            consumePower(10f);
            consumeItems(ItemStack.with(Items.thorium, 2, Items.silicon, 1));
        }};
        //幻荧合金组合器
        PhantomGlowAlloyCombiner = new GenericCrafter("PhantomGlowAlloyCombiner") {{
            requirements(Category.crafting, ItemStack.with(new Object[]{Items.silicon, 60, MLItems.mysticSteel, 45, MLItems.fluorescentFeatherStone, 30, MLItems.wingedMetal, 20, MLItems.arrayChip, 15}));

            hasItems = true;
            itemCapacity = 20;
            craftTime = 120f;
            outputItem = new ItemStack(MLItems.phantomLuminousAlloy, 1);
            size = 3;
            health = 600;
            hasPower = true;
            craftEffect = Fx.smeltsmoke;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawDefault(), new DrawFlame());

            consumePower(6f);
            consumeItems(ItemStack.with(MLItems.mysticSteel, 1, MLItems.fluorescentFeatherStone, 1, MLItems.wingedMetal, 1));
        }};
        //大型相织布编织器
        LargePhaseWeaver = new GenericCrafter("LargePhaseWeaver"){{
            requirements(Category.crafting, ItemStack.with(new Object[]{Items.silicon, 150, Items.lead, 160, Items.thorium, 110, MLItems.arrayChip, 15}));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(Items.phaseFabric, 2);
            craftTime = 120f;
            size = 3;
            hasPower = true;
            drawer = new DrawMulti(
                    new DrawRegion("-Z1"){{
                        rotateSpeed = 3f;
                        rotation = 0f;
                    }},
                    new DrawRegion("-Z2"){{
                        rotateSpeed = -3f;
                        rotation = 0f;
                    }},
            new DrawDefault());

            ambientSound = MLSounds.loopTech;
            ambientSoundVolume = 0.02f;

            consumeItems(ItemStack.with(Items.thorium, 4, Items.sand, 10));
            consumePower(10f);
            itemCapacity = 30;
        }};
        //大型巨浪合金冶炼厂
        LargeSurgeSmelter = new GenericCrafter("LargeSurgeSmelter") {{
            requirements(Category.crafting, ItemStack.with(new Object[]{Items.silicon, 120, Items.lead, 120, Items.thorium, 100, MLItems.arrayChip, 20}));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(Items.surgeAlloy, 2);
            craftTime = 80f;
            size = 4;
            hasPower = true;
            itemCapacity = 20;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawDefault(), new DrawFlame());

            consumePower(8f);
            consumeItems(ItemStack.with(Items.copper, 3, Items.lead, 4, Items.titanium, 2, Items.silicon, 3));
        }};
        //factor
        //炮
        //电戈
        electroge = new PowerTurret("electroge"){{
            requirements(Category.turret, ItemStack.with(new Object[]{MLItems.phantomSteel, 75, Items.graphite, 25}));
            range = 200f;

            recoil = 2f;
            reload = 120f;
            shake = 1f;
            shootEffect = Fx.lancerLaserShoot;
            smokeEffect = Fx.smeltsmoke;
            size = 2;
            health = 600;
            rotateSpeed = 8;
            shootSound = MLSounds.explosionAfflict;
            coolant = consumeCoolant(0.2f);

            consumePower(6f);

            shootType = new BasicBulletType(5f, 30f){{
                hitEffect = MLFx.smallElectricDetonation;//Slash(frontColor, 80f, 20f);
                despawnEffect = hitEffect;
                hitSize = 16f;
                damage = 30f;
                width = 8f;
                height = 24f;
                lifetime = 40f;
                ammoMultiplier = 1f;
                trailLength = 8;
                trailWidth = 4f;
                trailColor = Color.valueOf("97B5EDFF");
                frontColor = Color.valueOf("97B5EDFF");
                backColor = Color.valueOf("97B5EDFF");
                hitSound = MLSounds.explosionCleroi;
                despawnSound = MLSounds.explosionCleroi;
                fragBullets = 1;
                fragBullet = new LightningBulletType(){{
                        lifetime = 1f;
                        hitEffect = Fx.hitLancer;
                        lightColor = Color.valueOf("97B5EDFF");
                        damage = 10f;
                        lightning = 6;
                        lightningLength = 5;
                        lightningLengthRand= 5;
                    }};
                }};
            }};
        //流冲
        fluvialErosion = new ContinuousLiquidTurret("fluvialErosion"){{
            requirements(Category.turret, ItemStack.with(new Object[]{MLItems.phantomSteel, 100, MLItems.phantomTitaniumSteel, 30, MLItems.mysticCrystal, 50, Items.metaglass, 80}));
            liquidCapacity = 60f;
            liquidConsumed = 18f / 60f;
            targetInterval = 5f;
            targetUnderBlocks = false;
            range = 144f;
            size = 3;
            health = 1200;
            shootY = 0;

            loopSound = MLSounds.shootSublimate;
            shootEffect = Fx.shootLiquid;
            ammo(
            Liquids.water, new ContinuousFlameBulletType(){{
            damage = 20f;
            length = 144f;
            status = Liquids.water.effect;
            ammoMultiplier = 1f;
            knockback = 2f;
            pierceCap = 2;

            colors = new Color[]{Color.valueOf("596ab8").a(0.55f), Color.valueOf("596ab8").a(0.7f), Color.valueOf("596ab8").a(0.8f), Color.valueOf("596ab8"), Color.white};
            flareColor = Color.valueOf("596ab8");
               }},
            Liquids.cryofluid, new ContinuousFlameBulletType(){{
            damage = 30f;
            length = 144f;
            status = Liquids.cryofluid.effect;
            ammoMultiplier = 1f;
            knockback = 2f;
            pierceCap = 2;

            colors = new Color[]{Color.valueOf("6ecdec").a(0.55f), Color.valueOf("6ecdec").a(0.7f), Color.valueOf("6ecdec").a(0.8f), Color.valueOf("6ecdec"), Color.white};
            flareColor = Color.valueOf("6ecdec");
                        }},
            Liquids.slag, new ContinuousFlameBulletType() {{
            damage = 40f;
            length = 144f;
            status = Liquids.slag.effect;
            ammoMultiplier = 1f;
            knockback = 3f;
            pierceCap = 3;

            colors = new Color[]{Color.valueOf("ffa166").a(0.55f), Color.valueOf("ffa166").a(0.7f), Color.valueOf("ffa166").a(0.8f), Color.valueOf("ffa166"), Color.white};
            flareColor = Color.valueOf("ffa166");
                    }},
            Liquids.oil, new ContinuousFlameBulletType() {{
            damage = 20f;
            length = 144f;
            status = Liquids.oil.effect;
            ammoMultiplier = 1f;
            knockback = 2f;
            pierceCap = 2;

            colors = new Color[]{Color.valueOf("313131").a(0.55f), Color.valueOf("313131").a(0.7f), Color.valueOf("313131").a(0.8f), Color.valueOf("313131"), Color.white};
            flareColor = Color.valueOf("313131");
                    }}
            );
        }};
        //裂光
        Birefringence = new PowerTurret("Birefringence"){{
            float brange = range = 320f;
            shootY = 0;
            requirements(Category.turret, ItemStack.with(new Object[]{MLItems.phantomSteel, 150, MLItems.phantomTitaniumSteel, 50, MLItems.mysticCrystal, 80, Items.silicon, 100}));
            shootType = new PointBulletType(){{
                shootEffect = Fx.despawn;
                hitEffect = MLFx.squareWaveRot;
                smokeEffect = Fx.smeltsmoke;
                trailEffect = MLFx.beamEffect;
                despawnEffect = MLFx.squareWaveRot;
                trailSpacing = 20f;
                damage = 200;
                buildingDamageMultiplier = 0.1f;
                speed = brange;
                hitShake = 3f;
                ammoMultiplier = 1f;
            }};
            rotateSpeed = 6f;
            reload = 120f;
            ammoUseEffect = Fx.casing3Double;
            recoil = 6f;
            cooldownTime = reload/2;
            shake = 3f;
            size = 3;
            shootCone = 2f;
            shootSound = MLSounds.shootForeshadow;
            unitSort = UnitSorts.strongest;

            health = 1400;

            coolant = consumeCoolant(0.3f);
            consumePower(8f);
        }};
        //激沅
        excitedYuan = new ItemTurret("excitedYuan"){{
        requirements(Category.turret, ItemStack.with(new Object[]{MLItems.phantomSteel, 50, Items.graphite, 80, Items.titanium, 40}));
        ammo(
                Items.copper, new BasicBulletType(8f, 20){{
                    hitSize = 2f;
                    width = 16f;
                    height = 24f;
                    shootEffect = Fx.shootSmall;
                    ammoMultiplier = 2;
                    reloadMultiplier = 2f;
                    //knockback = 0.3f;
                    lifetime = 25f;
                    trailLength = 6;
                    trailWidth = 3f;

                    hitEffect = despawnEffect = Fx.hitBulletColor;
                    hitColor = backColor = trailColor = Color.valueOf("d99d73");
                    frontColor = Color.valueOf("d99d73");
                    buildingDamageMultiplier = 0.1f;
                }},
                Items.graphite, new BasicBulletType(8f, 30){{
                    hitSize = 2f;
                    width = 16f;
                    height = 24f;
                    shootEffect = Fx.shootSmall;
                    ammoMultiplier = 1;
                    reloadMultiplier = 1.5f;
                    knockback = 0.5f;
                    lifetime = 25f;
                    trailLength = 6;
                    trailWidth = 3f;

                    hitEffect = despawnEffect = Fx.hitBulletColor;
                    hitColor = backColor = trailColor = Color.valueOf("b2c6d2");
                    frontColor = Color.valueOf("b2c6d2");
                    buildingDamageMultiplier = 0.1f;
                }},
                Items.titanium, new BasicBulletType(8f, 40){{
                    hitSize = 2f;
                    width = 16f;
                    height = 24f;
                    shootEffect = Fx.shootSmall;
                    ammoMultiplier = 1;
                    reloadMultiplier = 1f;
                    knockback = 1f;
                    pierceCap = 2;
                    lifetime = 25f;
                    trailLength = 6;
                    trailWidth = 3f;

                    hitEffect = despawnEffect = Fx.hitBulletColor;
                    hitColor = backColor = trailColor = Color.valueOf("8da1e3");
                    frontColor = Color.valueOf("8da1e3");
                    buildingDamageMultiplier = 0.1f;
                }}
        );
        reload = 25f;
        recoilTime = reload / 2f;
        ammoUseEffect = Fx.casing1;
        range = 200f;
        inaccuracy = 0f;
        recoil = 3f;
        shoot = new ShootAlternate(10f);
        shake = 2f;
        size = 3;
        shootCone = 24f;
        shootSound = MLSounds.shootAlt;

        health = 1120;
        coolant = consumeCoolant(0.3f);
        }};
        //光降
        LightDescends = new ItemTurret("LightDescends"){{
            requirements(Category.turret, ItemStack.with(new Object[]{MLItems.phantomSteel, 120, Items.silicon, 80, Items.titanium, 90, MLItems.phantomTitaniumSteel, 75, MLItems.logicChip, 60,}));
            ammo(
                    Items.graphite, new BasicBulletType(16f, 30) {{
                        splashDamageRadius = 32f;
                        splashDamage = 30f;
                        knockback = 1f;
                        speed = 16f;
                        damage = 30f;
                        lifetime = 45f;
                        homingPower = 0.3f;
                        sprite = "magic-导弹";
                        frontColor = Color.valueOf("E3E3E3FF");
                        backColor  = Color.valueOf("FF5B5BFF");
                        trailLength = 15;
                        trailWidth  = 5;
                        trailColor  = Color.valueOf("E3E3E3");
                        homingDelay = 3f;
                        homingRange = 2400f;
                        width  = 32f;
                        height = 64f;
                        hitShake = 1f;
                        ammoMultiplier = 1f;
                        smokeEffect = Fx.shootSmallFlame;
                        hitEffect   = Fx.flakExplosionBig;
                        hitSound    = MLSounds.explosion;
                        buildingDamageMultiplier = 0.1f;
                    }},
            MLItems.phantomSteel, new BasicBulletType(16f, 60) {{
                splashDamageRadius = 32f;
                splashDamage = 60f;
                knockback = 2f;
                speed = 16f;
                damage = 60f;
                lifetime = 45f;
                homingPower = 0.6f;
                sprite = "magic-导弹";
                frontColor = Color.valueOf("97B5EDFF");
                backColor  = Color.valueOf("97B5EDFF");
                trailLength = 15;
                trailWidth  = 5;
                trailColor  = Color.valueOf("97B5EDFF");
                homingDelay = 2f;
                homingRange = 2400f;
                width  = 32f;
                height = 64f;
                hitShake = 1f;
                ammoMultiplier = 1f;
                smokeEffect = Fx.shootSmallFlame;
                hitEffect   = Fx.flakExplosionBig;
                hitSound    = MLSounds.explosion;
                buildingDamageMultiplier = 0.1f;
            }},
            MLItems.phantomTitaniumSteel, new BasicBulletType(16f, 90) {{
                splashDamageRadius = 32f;
                splashDamage = 90f;
                knockback = 4f;
                speed = 16f;
                damage = 90f;
                lifetime = 45f;
                homingPower = 0.9f;
                sprite = "magic-导弹";
                frontColor = Color.valueOf("46649AFF");
                backColor  = Color.valueOf("46649AFF");
                trailLength = 15;
                trailWidth  = 5;
                trailColor  = Color.valueOf("46649AFF");
                homingDelay = 1f;
                homingRange = 2400f;
                width  = 32f;
                height = 64f;
                hitShake = 1f;
                ammoMultiplier = 1f;
                smokeEffect = Fx.shootSmallFlame;
                hitEffect   = Fx.flakExplosionBig;
                hitSound    = MLSounds.explosion;
                buildingDamageMultiplier = 0.1f;
            }}
            );
            reload = 300f;
            recoilTime = 0;
            ammoUseEffect =  new ParticleEffect() {{
                particles = 16;
                sizeFrom = 4f;
                sizeTo = 0f;
                length = 60f;
                lifetime = 60f;
                lightOpacity = 0f;
                colorFrom = Color.white;
                colorTo = Color.white;
            }};
            range = 720f;
            inaccuracy = 0f;
            recoil = 3f;
            customShadow = false;
            maxAmmo = 40;
            cooldownTime = 60;
            solid = false;
            underBullets = true;
            shoot = new ShootBarrel() {{
                shots = 8;
                shotDelay = 4f;
                barrels = new float[]{
                        8f, 8f, 0f,
                        8f, 8f, -90f,
                        8f, -8f, 180f,
                        8f, -8f, -90f,
                        -8f, -8f, 90f,
                        -8f, -8f, 180f,
                        -8f, 8f, 0f,
                        -8f, 8f, 90f
                };
            }};
            shake = 2f;
            size = 3;
            shootCone = 360f;
            shootSound = MLSounds.missileLaunch;
            canOverdrive = false;
            hasPower = true;
            shake = 1;
            rotateSpeed = 0;
            ammoPerShot = 8;
            shootY = 0;

            drawer = new DrawTurret(){{
            parts.add(new RegionPart("-1") {{
                        mirror = false;
                        x = 0f;
                        y = 0f;
                        moveX = 4f;
                        moveY = 4f;
                    }},
            new RegionPart("-2") {{
                        mirror = false;
                        x = 0f;
                        y = 0f;
                        moveX = -4f;
                        moveY = -4f;
                    }},
            new RegionPart("-3") {{
                        mirror = false;
                        x = 0f;
                        y = 0f;
                        moveX = -4f;
                        moveY = 4f;
                    }},
            new RegionPart("-4") {{
                        mirror = false;
                        x = 0f;
                        y = 0f;
                        moveX = 4f;
                        moveY = -4f;
               }});
            }};

            health = 1600;
            armor = 4;
            consumePower(5f);
        }};
        //幻晶
        PhantomCrystal = new ItemTurret("PhantomCrystal"){{
            requirements(Category.turret, ItemStack.with(new Object[]{MLItems.acrylic,80 , MLItems.phantomTitaniumSteel, 60, MLItems.wingedStone, 90, MLItems.logicChip, 30}));
            ammo(
                    MLItems.wingedStone, new BasicBulletType(10f, 50){{
                        hitSize = 16f;
                        width = 12f;
                        height = 21f;
                        shootEffect = Fx.shootBig;
                        ammoMultiplier = 1;
                        reloadMultiplier = 1f;
                        knockback = 0.3f;
                        lifetime = 28f;
                        status = StatusEffects.shocked;
                        statusDuration = 60;
                        lightningDamage = 10;
                        lightning = 3;
                        lightningLength = 12;
                        lightningColor = Color.valueOf("9C88C3FF");
                        hitEffect = despawnEffect = Fx.hitBulletColor;
                        hitColor = backColor = Color.valueOf("9C88C3FF");
                        frontColor = Color.valueOf("9C88C3FF");
                        buildingDamageMultiplier = 0.1f;
                    }},
                    Items.titanium, new BasicBulletType(10f, 40){{
                        hitSize = 16f;
                        width = 12f;
                        height = 21f;
                        shootEffect = Fx.shootBig;
                        ammoMultiplier = 1.2f;
                        reloadMultiplier = 1f;
                        pierceCap = 2;
                        pierceBuilding = true;
                        knockback = 0.6f;
                        lifetime = 28f;
                        hitEffect = despawnEffect = Fx.hitBulletColor;
                        hitColor = backColor = Color.valueOf("8da1e3");
                        frontColor = Color.valueOf("8da1e3");
                        buildingDamageMultiplier = 0.1f;
                    }}
            );
            reload = 9f;
            recoilTime = reload / 2f;
            ammoUseEffect = Fx.casing2;
            range = 280f;
            inaccuracy = 3f;
            recoil = 3f;
            shoot = new ShootAlternate(12f);
            shake = 3f;
            size = 3;
            //shootSound = MLSounds.shootAlt;
            armor = 5;
            health = 2000;

            coolant = consumeCoolant(0.4f);
        }};
        //雷云
        Thundercloud = new PowerTurret("Thundercloud"){{
            range = 240f;
            requirements(Category.turret, ItemStack.with(new Object[]{MLItems.acrylic,80 , MLItems.phantomTitaniumSteel, 60, MLItems.wingedStone, 90, Items.silicon, 75, MLItems.logicChip, 30}));
            shootType = new BasicBulletType(8f, 50f){{
                homingPower = 0.03f;
                homingDelay = 1.5f;
                homingRange = 190f;
                lifetime = 30f;
                width = 16f;
                height = 32f;
                hitSize = 28f;
                splashDamageRadius = 24f;
                splashDamage = 50f;
                frontColor = Color.valueOf("97B5EDFF");
                backColor = Color.valueOf("97B5EDFF");
                trailLength = 4;
                trailWidth = 2f;
                trailColor = Color.valueOf("97B5EDFF");
                ammoMultiplier = 1f;
                hitSound = MLSounds.plasmadrop;
                buildingDamageMultiplier = 0.1f;
                hitEffect = new MultiEffect(
                        new WaveEffect(){{
                            lifetime = 30f;
                            sizeFrom = 0f;
                            sizeTo = 24f;
                            strokeFrom = 0f;
                            strokeTo = 2f;
                            colorFrom = Color.valueOf("97B5EDFF");
                            colorTo = Color.valueOf("97B5EDFF");
                        }},
                        new ParticleEffect(){{
                            particles = 8;
                            sizeFrom = 4f;
                            sizeTo = 0f;
                            length = 24f;
                            baseLength = 0f;
                            interp = Interp.pow10Out;
                            sizeInterp = Interp.pow10In;
                            lifetime = 30f;
                            colorFrom = Color.valueOf("97B5EDFF");
                            colorTo = Color.valueOf("97B5EDFF");
                        }}
                );
                despawnEffect = Fx.none;
                smokeEffect = Fx.smokeCloud;
                trailChance = 1f;
                trailInterval = 20f;
                trailEffect = new ParticleEffect(){{
                    particles = 9;
                    length = 9f;
                    baseLength = 0f;
                    lifetime = 9f;
                    sizeFrom = 3f;
                    sizeTo = 0f;
                    colorFrom = Color.valueOf("97B5EDFF");
                    colorTo = Color.valueOf("97B5EDFF");
                }};
                fragBullets = 2;
                fragBullet = new BasicBulletType(16f, 1f){{
                    hitSound = MLSounds.laser;
                    width = 8f;
                    height = 8f;
                    frontColor = Color.valueOf("97B5EDFF");
                    backColor = Color.valueOf("97B5EDFF");
                    hittable = false;
                    reflectable = false;
                    collides = false;
                    absorbable = false;
                    lifetime = 12f;
                    shootEffect = Fx.none;
                    smokeEffect = Fx.none;
                    hitEffect = Fx.none;
                    buildingDamageMultiplier = 0.1f;
                    fragBullets = 1;
                    fragBullet = new LaserBulletType(40f){{
                        hitSound = MLSounds.laser;
                        lifetime = 32f;
                        width = 16f;
                        length = 72f;
                        lightningSpacing = 5f;
                        lightningLength = 4;
                        lightningDelay = 0.4f;
                        lightningLengthRand = 2;
                        lightningAngleRand = 4;
                        lightningDamage = 20f;
                        lightningColor =
                        Color.valueOf("97B5EDFF");
                        collidesTeam = true;
                        hitEffect = Fx.none;
                        despawnEffect = Fx.none;
                        buildingDamageMultiplier = 0.1f;
                        colors = new Color[]{
                                Color.valueOf("97B5EDFF"),
                                Color.valueOf("97B5EDFF"),
                                Color.valueOf("97B5EDFF")
                        };
                    }};
                }};
            }};
            drawer = new DrawMulti(
                    new DrawTurret(){{
                        parts.add(
                                new HaloPart(){{
                                    sides = 4;
                                    shapes = 1;
                                    y = 8f;
                                    color = Color.valueOf("97B5EDFF");
                                    colorTo = Color.valueOf("97B5EDFF");
                                    tri = false;
                                    hollow = true;
                                    stroke = 0f;
                                    strokeTo = 1f;
                                    radius = 0f;
                                    radiusTo = 4f;
                                    haloRadius = 0f;
                                    haloRotateSpeed = 1f;
                                    layer = 110f;
                                }}
                        );
                    }}
            );
            shoot = new ShootBarrel(){{
                shots = 7;
                shotDelay = 1f;
                barrels = new float[]{
                        0,0,30,
                        0,0,20,
                        0,0,10,
                        0,0,0,
                        0,0,-10,
                        0,0,-20,
                        0,0,-30
                };
            }};
            rotateSpeed = 3f;
            reload = 300f;
            ammoUseEffect = Fx.casing3Double;
            recoil = 1f;
            shake = 1f;
            size = 3;
            shootSound = MLSounds.lasercharge2;
            armor = 3;
            health = 2100;
            coolant = consumeCoolant(0.3f);
            consumePower(10f);
        }};
        //破军
        BreakingArmy = new PowerTurret("BreakingArmy"){{
            requirements(Category.turret, ItemStack.with(new Object[]{Items.plastanium, 80, MLItems.wingedStone, 60, Items.silicon, 110, MLItems.logicChip, 40}));
            range = 400f;

            recoil = 4f;
            reload = 180f;
            shake = 4f;
            shootEffect = Fx.instShoot;
            smokeEffect = Fx.smeltsmoke;
            size = 4;
            armor = 6;
            health = 2400;
            rotateSpeed = 4;
            shootSound = MLSounds.laser;
            coolant = consumeCoolant(0.4f);
            unitSort = UnitSorts.strongest;

            consumePower(16f);

            drawer = new DrawTurret(){{
                parts.add(new RegionPart("-l") {{
                              mirror = false;
                              x = 0f;
                              y = 0f;
                              moveX = -4f;
                              moveY = 0f;
                          }},
                        new RegionPart("-r") {{
                            mirror = false;
                            x = 0f;
                            y = 0f;
                            moveX = 4f;
                            moveY = 0f;
                        }});
            }};
            shootType = new BasicBulletType(16f, 480f){{
                hitEffect = Fx.colorSparkBig;
                despawnEffect = hitEffect;
                hitSize = 16f;
                width = 16f;
                height = 32f;
                lifetime = 25f;
                trailLength = 8;
                trailWidth = 3f;
                pierceCap = 2;
                pierceBuilding = true;
                knockback = 4f;
                trailColor = Color.valueOf("FEEBB3FF");
                frontColor = Color.valueOf("FEEBB3FF");
                backColor = Color.valueOf("FEEBB3FF");
                hitSound = MLSounds.shootFuse;
                buildingDamageMultiplier = 0.1f;
            }};
        }};
        //星云
        Nebula = new PowerTurret("Nebula"){{
                requirements(Category.turret, ItemStack.with(new Object[]{Items.plastanium, 90, MLItems.phantomTitaniumSteel, 50, Items.silicon, 120, MLItems.logicChip, 30}));
                shootType = new BasicBulletType(16f, 50f) {{
                    width = 12f;
                    height = 24f;
                    lifetime = 30f;
                    frontColor = Color.valueOf("FEEBB3FF");
                    backColor = Color.valueOf("FEEBB3FF");
                    trailLength = 6;
                    trailWidth = 3f;
                    trailColor = Color.valueOf("FEEBB3FF");
                    status = StatusEffects.sapped;
                    statusDuration = 120f;
                    shootEffect = new WaveEffect() {{
                        lifetime = 15f;
                        size = 0;
                        sizeTo = 24f;
                        strokeFrom = 0f;
                        strokeTo = 3f;
                        colorFrom = Color.valueOf("FEEBB3FF");
                        colorTo = Color.valueOf("FEEBB3FF");
                    }};
                    fragBullets = 4;
                    fragBullet = new BasicBulletType(16f, 60f) {{
                        width = 32f;
                        height = 32f;
                        lifetime = 16f;
                        shrinkY = 0f;
                        homingRange = 240f;
                        homingPower = 0.6f;
                        splashDamageRadius = 24f;
                        splashDamage = 40f;
                        hitSound = MLSounds.plasmaboom;
                        spin = 6f;
                        status = StatusEffects.slow;
                        statusDuration = 240f;
                        hitEffect = new MultiEffect(
                                new WaveEffect() {{
                                    lifetime = 10f;
                                    size = 0;
                                    sizeTo = 24f;
                                    strokeFrom = 0f;
                                    strokeTo = 3f;
                                    colorFrom = Color.valueOf("FEEBB3FF");
                                    colorTo = Color.valueOf("FEEBB3FF");
                                }},
                                new ParticleEffect() {{
                                    particles = 8;
                                    sizeFrom = 2f;
                                    sizeTo = 0f;
                                    length = 24f;
                                    baseLength = 24f;
                                    lifetime = 15f;
                                    interp = Interp.pow10Out;
                                    sizeInterp = Interp.pow10In;
                                    colorFrom = Color.valueOf("FEEBB3FF");
                                    colorTo = Color.valueOf("FEEBB3FF");
                                }},
                                new ParticleEffect() {{
                                    particles = 8;
                                    line = true;
                                    strokeFrom = 2f;
                                    strokeTo = 0f;
                                    lenFrom = 4f;
                                    lenTo = 0f;
                                    length = 24f;
                                    baseLength = 24f;
                                    lifetime = 15f;
                                    interp = Interp.pow5Out;
                                    sizeInterp = Interp.pow5In;
                                    colorFrom = Color.valueOf("FEEBB3FF");
                                    colorTo = Color.valueOf("FEEBB3FF");
                                }}
                        );
                        trailLength = 8;
                        trailWidth = 4f;
                        trailColor = Color.valueOf("FEEBB3FF");
                        frontColor = Color.valueOf("FEEBB3FF");
                        backColor = Color.valueOf("FEEBB3FF");
                        sprite = "magic-十字星";
                    }};
                }};
            range = 360f;
            recoil = 6f;
            recoilTime = 60f;
            reload = 150f;
            shake = 9f;
            liquidCapacity = 60;
            shootEffect = Fx.none;
            smokeEffect = Fx.smeltsmoke;
            size = 3;
            armor = 6;
            health = 1800;
            rotateSpeed = 4;
            shootSound = MLSounds.laser;
            coolant = consumeCoolant(0.3f);
            consumePower(12f);
            drawer = new DrawMulti(
                    new DrawTurret(){{
                        parts.add(
                                    new HaloPart() {{
                                        sides = 4;
                                        shapes = 1;
                                        y = 12f;
                                        color = Color.valueOf("FEEBB3FF");
                                        colorTo = Color.valueOf("FEEBB3FF");
                                        tri = false;
                                        hollow = true;
                                        stroke = 0f;
                                        strokeTo = 1f;
                                        radius = 0f;
                                        radiusTo = 4f;
                                        haloRadius = 0f;
                                        haloRotateSpeed = -2f;
                                        layer = 110f;
                                    }},
                                    new HaloPart() {{
                                        sides = 4;
                                        shapes = 1;
                                        y = 12f;
                                        color = Color.valueOf("FEEBB3FF");
                                        colorTo = Color.valueOf("FEEBB3FF");
                                        tri = false;
                                        hollow = true;
                                        stroke = 0f;
                                        strokeTo = 1f;
                                        radius = 0f;
                                        radiusTo = 6f;
                                        haloRadius = 0f;
                                        haloRotateSpeed = 2f;
                                        layer = 110f;
                                    }}
                        );
                    }}
            );
            }};
        //弹雨
        BulletsRain = new ItemTurret("BulletsRain"){{
            requirements(Category.turret, ItemStack.with(new Object[]{MLItems.wingedMetal, 100, Items.surgeAlloy, 140, MLItems.phantomTitaniumSteel, 120, MLItems.mysticSteel, 80, MLItems.arrayChip, 40,}));
            ammo(
                    Items.graphite, new BasicBulletType(12f, 50){{
                        hitSize = 12f;
                        width = 14f;
                        height = 28f;
                        shootEffect = Fx.shootSmall;
                        ammoMultiplier = 1;
                        reloadMultiplier = 1f;
                        lifetime = 30f;
                        trailLength = 4;
                        trailWidth = 3f;
                        homingRange = 360f;
                        homingPower = 0.08f;
                        homingDelay = 2f;
                        hitEffect = despawnEffect = Fx.hitBulletColor;
                        hitColor = backColor = trailColor = Color.valueOf("b2c6d2");
                        frontColor = Color.valueOf("b2c6d2");
                        buildingDamageMultiplier = 0.1f;
                    }}
            );
            reload = 8f;
            ammoUseEffect = Fx.casing2Double;
            coolantMultiplier = 0.98;
            rotateSpeed = 4;
            range = 360f;
            inaccuracy = 40f;
            recoil = 2f;
            shake = 2f;
            ammoPerShot = 2;
            maxAmmo = 80;
            size = 4;
            shootSound = MLSounds.shootArtillerySap;
            shoot = new ShootBarrel() {{
                shots = 2;
                shotDelay = 0f;
                barrels = new float[]{
                        0f, 16f, 0f
                };
            }};
            health = 2400;
            armor = 6;
            coolant = consumeCoolant(0.6f);
            drawer = new DrawMulti(
                    new DrawTurret(){{
                        parts.add(
                                new ShapePart(){{
                                    y = 20f;
                                    color = Color.valueOf("FEEBB3FF");
                                    stroke = 0f;
                                    strokeTo = 2f;
                                    circle = true;
                                    hollow = true;
                                    radius = 0f;
                                    radiusTo = 8f;
                                    layer = 110f;
                                }},
                                new HaloPart(){{
                                    sides = 3;
                                    shapes = 3;
                                    y = 20f;
                                    color = Color.valueOf("FEEBB3FF");
                                    colorTo = Color.valueOf("FEEBB3FF");
                                    tri = true;
                                    radius = 0f;
                                    radiusTo = 6f;
                                    triLength = 0f;
                                    triLengthTo = 12f;
                                    haloRadius = 0f;
                                    haloRadiusTo = 8f;
                                    haloRotateSpeed = 2f;
                                }}
                        );
                    }}
            );
        }};
        //turret
        //！？强强？！
        int whm = 4;
        adaptiveWall = new magical.content.AdaptiveWall("adaptiveWall"){{
            requirements(Category.defense, ItemStack.with(new Object[]{MLItems.mysticCrystal, 6, Items.silicon, 6}));
            health = 120 * whm;
        }};
        largeAdaptiveWall = new magical.content.AdaptiveWall("largeAdaptiveWall"){{
            requirements(Category.defense, ItemStack.with(new Object[]{MLItems.mysticCrystal, 6 * whm, Items.silicon, 6 * whm}));
            health = 120 * whm * 4;
            size = 2;
        }};
        phantomSteelWall = new Wall("phantomSteelWall"){{
            requirements(Category.defense, ItemStack.with(new Object[]{MLItems.phantomSteel, 6}));
            health = 140 * whm;
        }};
        largePhantomSteelWall = new Wall("largePhantomSteelWall"){{
            requirements(Category.defense, ItemStack.with(new Object[]{MLItems.phantomSteel, 6 * whm}));
            health = 140 * whm * 4;
            size = 2;
        }};
        phantomTitaniumSteelWall = new Wall("phantomTitaniumSteelWall"){{
            requirements(Category.defense, ItemStack.with(new Object[]{MLItems.phantomTitaniumSteel, 6}));
            health = 200 * whm;
        }};
        largePhantomTitaniumSteelWall = new Wall("largePhantomTitaniumSteelWall"){{
            requirements(Category.defense, ItemStack.with(new Object[]{MLItems.phantomTitaniumSteel, 6 * whm}));
            health = 200 * whm * 4;
            size = 2;
        }};
        wingWall = new Wall("wingWall"){{
            requirements(Category.defense, ItemStack.with(new Object[]{MLItems.wingedStone, 6}));
            health = 260 * whm;
            lightningChance = 0.32f;
            lightningDamage = 32;
            lightningLength = 16;
            lightningColor = Color.valueOf("9C88C3FF");
        }};
        LargeWingWall = new Wall("LargeWingWall"){{
            requirements(Category.defense, ItemStack.with(new Object[]{MLItems.wingedStone, 6 * whm}));
            health = 260 * whm * 4;
            size = 2;
            lightningChance = 0.32f;
            lightningDamage = 32;
            lightningLength = 16;
            lightningColor = Color.valueOf("9C88C3FF");
        }};
        //wall
        //出来了，出来了
        phantomSteelDrill  = new Drill("phantomSteelDrill"){{
            requirements(Category.production, ItemStack.with(new Object[]{MLItems.phantomSteel, 60,Items.graphite, 30}));
            drillTime = 220;
            size = 4;
            drawRim = true;
            tier = 3;
            updateEffect = Fx.pulverizeMedium;
            drillEffect = Fx.mineBig;
            rotateSpeed = 4f;
            itemCapacity = 40;
            health = 700;

            liquidBoostIntensity = 1.4f;

            consumeLiquid(Liquids.water, 0.2f).boost();
        }};
        //钻头
        //物流溜溜溜
        phantomSteelConveyor = new Conveyor("phantomSteelConveyor"){{
            requirements(Category.distribution, ItemStack.with(new Object[]{MLItems.phantomSteel, 1, Items.graphite, 1}));
            health = 150;
            speed = 0.12f;
            displayedSpeed = 17f;
        }};
        phantomTitaniumSteelConveyor = new Conveyor("phantomTitaniumSteelConveyor"){{
            requirements(Category.distribution, ItemStack.with(new Object[]{MLItems.phantomTitaniumSteel, 1, MLItems.phantomSteel, 1, Items.titanium, 1}));
            health = 200;
            speed = 0.2f;
            displayedSpeed = 25f;
        }};
        phantomSteelBridge = new BufferedItemBridge("phantomSteelBridge"){{
            requirements(Category.distribution, ItemStack.with(new Object[]{MLItems.phantomSteel, 9, Items.graphite, 18}));
            fadeIn = moveArrows = false;
            range = 8;
            health = 60;
            transportTime = 3;
        }};
        phantomSteeljunction = new Junction("phantomSteeljunction"){{
            requirements(Category.distribution, ItemStack.with(new Object[]{MLItems.phantomSteel, 6}));
            speed = 18;
            health = 60;
        }};
        phantomSteelUnloader = new Unloader("phantomSteelUnloader"){{
            requirements(Category.distribution, ItemStack.with(new Object[]{MLItems.phantomSteel, 60, Items.titanium, 60, Items.silicon,60}));
            speed = 1;
            group = BlockGroup.transportation;
        }};
        //Conveyor
        //电死你
        phantomSteelPowerNode = new PowerNode("phantomSteelPowerNode"){{
            requirements(Category.power, ItemStack.with(new Object[]{MLItems.phantomSteel, 3, Items.silicon, 1, Items.graphite, 5}));
            maxNodes = 15;
            laserRange = 15;
            health = 200;
            underBullets = true;
            consumePowerBuffered(2000f);
        }};
        phantomTitaniumSteelPowerNode = new PowerNode("phantomTitaniumSteelPowerNode"){{
            requirements(Category.power, ItemStack.with(new Object[]{MLItems.phantomTitaniumSteel, 2, Items.silicon, 6, MLItems.acrylic, 4}));
            maxNodes = 30;
            size = 2;
            health = 400;
            laserRange = 30;
            underBullets = true;
            consumePowerBuffered(4000f);
        }};
            //燃能发电机
        fuelPoweredGenerator = new ConsumeGenerator("fuelPoweredGenerator"){{
            requirements(Category.power, ItemStack.with(new Object[]{MLItems.phantomSteel, 60, Items.graphite, 30}));
            powerProduction = 5f;
            itemDuration = 150f;
            size = 2;

            ambientSound = MLSounds.loopSmelter;
            ambientSoundVolume = 0.03f;
            generateEffect = Fx.generatespark;

            consume(new magical.content.ConsumeItemFlammable());

            drawer = new DrawMulti(new DrawDefault(), new DrawWarmupRegion());

        }};
        //电磁裂变炉
        ElectromagneticFissionReactor = new ConsumeGenerator("ElectromagneticFissionReactor"){{
            requirements(Category.power, ItemStack.with(new Object[]{MLItems.phantomTitaniumSteel, 50, MLItems.acrylic, 60, Items.silicon, 80, MLItems.logicChip, 20 }));
            powerProduction = 30f;
            itemDuration = 120f;
            size = 4;

            ambientSound = MLSounds.loopSmelter;
            ambientSoundVolume = 0.1f;
            generateEffect = MLFx.smallElectricDetonation;

            consumeLiquid(Liquids.water, 0.8f);
            consumeItem(MLItems.wingedStone, 2);

            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.water), new DrawDefault());

        }};
        //power
        //单位
        //基础制造厂
        BasicManufacturingPlant = new UnitFactory("BasicManufacturingPlant"){{
            requirements(Category.units, ItemStack.with(new Object[]{MLItems.phantomSteel, 80, Items.graphite, 60, Items.silicon, 45}));
            plans = Seq.with(
                    new UnitPlan(MLUnitTypes.drizzle, 60f * 40, ItemStack.with(MLItems.phantomSteel, 20, Items.graphite, 30, Items.silicon, 10)),
                    new UnitPlan(MLUnitTypes.Breeze, 60f * 30, ItemStack.with(MLItems.phantomSteel, 30, Items.silicon, 15)),
                    new UnitPlan(MLUnitTypes.StillWater, 60f * 50, ItemStack.with(MLItems.phantomSteel, 40, Items.silicon, 20, Items.graphite, 30)),
                    new UnitPlan(MLUnitTypes.war, 60f * 50, ItemStack.with(MLItems.phantomTitaniumSteel, 20, MLItems.logicChip, 5, Items.silicon, 30)),
                    new UnitPlan(MLUnitTypes.BlazingFire, 60f * 40, ItemStack.with(MLItems.phantomTitaniumSteel, 10, MLItems.logicChip, 5, Items.silicon, 10)),
                    new UnitPlan(MLUnitTypes.ExpelDarkness, 60f * 60, ItemStack.with(MLItems.phantomTitaniumSteel, 30, MLItems.logicChip, 5, Items.silicon, 40))
            );
            size = 3;
            consumePower(2f);
        }};
        //曲率进化舱
        curvatureEvolutionPod = new Reconstructor("curvatureEvolutionPod"){{
            requirements(Category.units, ItemStack.with(new Object[]{MLItems.phantomSteel, 240, Items.graphite, 120, Items.silicon, 90}));
            size = 5;
            consumePower(6f);
            consumeItems(ItemStack.with(new Object[]{Items.silicon, 40, MLItems.phantomSteel, 60, Items.graphite, 50}));
            constructTime = 60f * 15f;
            health = 720;

            upgrades.addAll(
                    new UnitType[]{MLUnitTypes.drizzle, MLUnitTypes.Drizzle},
                    new UnitType[]{MLUnitTypes.Breeze, MLUnitTypes.SlantingWind},
                    new UnitType[]{MLUnitTypes.StillWater, MLUnitTypes.ripple},
                    new UnitType[]{MLUnitTypes.war, MLUnitTypes.BeaconFire},
                    new UnitType[]{MLUnitTypes.BlazingFire, MLUnitTypes.glow},
                    new UnitType[]{MLUnitTypes.ExpelDarkness, MLUnitTypes.ChasingLight}
            );
        }};
        //量子制造厂
        quantumFactory = new Reconstructor("quantumFactory"){{
            requirements(Category.units, ItemStack.with(new Object[]{MLItems.phantomSteel, 300, Items.graphite, 150, Items.silicon, 120, MLItems.phantomTitaniumSteel, 60, MLItems.mysticCrystal, 90}));
            size = 7;
            consumePower(10f);
            consumeItems(ItemStack.with(new Object[]{Items.silicon, 70, MLItems.phantomSteel, 80, Items.graphite, 90, MLItems.phantomTitaniumSteel, 50, MLItems.mysticCrystal, 30}));
            constructTime = 60f * 30f;
            health = 1440;

            upgrades.addAll(
                    new UnitType[]{MLUnitTypes.Drizzle, MLUnitTypes.drizzlingRain},
                    new UnitType[]{MLUnitTypes.SlantingWind, MLUnitTypes.Gale},
                    new UnitType[]{MLUnitTypes.ripple, MLUnitTypes.Turbulence},
                    new UnitType[]{MLUnitTypes.BeaconFire, MLUnitTypes.War},
                    new UnitType[]{MLUnitTypes.glow, MLUnitTypes.blazing},
                    new UnitType[]{MLUnitTypes.ChasingLight, MLUnitTypes.Dawn}
            );
        }};
        //星港造舰中心
        starHarborShipbuildingCenter = new magical.content.FlexAssembler("starHarborShipbuildingCenter"){{
            requirements(Category.units, ItemStack.with(new Object[]{MLItems.phantomTitaniumSteel, 500, Items.titanium, 800, Items.silicon, 1200, MLItems.logicChip, 200, MLItems.mysticCrystal, 500}));
            size = 7;

            droneType = MLUnitTypes.Pioneer;
            dronesCreated = 4;
            // 配方：(等级标签, 输出单位, 时间(秒), 范围(格), 需要模块数, 载荷需求...)
            addPlan("T1", MLUnitTypes.Starlight, 1200f, 11, 0,
                    new PayloadStack(MLBlocks.largePhantomSteelWall, 8),
                    new PayloadStack(MLBlocks.largePhantomTitaniumSteelWall, 8),
                    new PayloadStack(Blocks.repairPoint, 8),
                    new PayloadStack(Blocks.forceProjector, 4));
            addPlan("T1", MLUnitTypes.Qingxiao, 1500f, 11, 0,
                    new PayloadStack(MLBlocks.largePhantomSteelWall, 8),
                    new PayloadStack(MLBlocks.largePhantomTitaniumSteelWall, 8),
                    new PayloadStack(Blocks.lancer, 4),
                    new PayloadStack(Blocks.forceProjector, 2));

            consumePower(20f);
            consumeLiquid(Liquids.water, 1f);
        }};
        StarshipMaterialConstructor = new Constructor("StarshipMaterialConstructor"){{
            requirements(Category.units, ItemStack.with(new Object[]{MLItems.phantomSteel, 120, Items.graphite, 90, Items.silicon, 70, MLItems.logicChip, 25}));
            hasPower = true;
            buildSpeed = 1f;
            consumePower(5f);
            size = 5;
            minBlockSize = 1;
            maxBlockSize = 5;
            filter = Seq.with(
                    MLBlocks.largePhantomSteelWall,
                    MLBlocks.largePhantomTitaniumSteelWall,
                    MLBlocks.Nebula,
                    Blocks.repairPoint,
                    Blocks.forceProjector,
                    Blocks.lancer
            );
        }};
        StarshipMaterialDeconstructor = new PayloadDeconstructor("StarshipMaterialDeconstructor"){{
            requirements(Category.units, ItemStack.with(new Object[]{MLItems.phantomSteel, 120, Items.graphite, 90, Items.silicon, 70, MLItems.logicChip, 25}));
            itemCapacity = 250;
            consumePower(5f);
            size = 5;
            deconstructSpeed = 5f;
        }};
         //unit
    }
}