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

public class MLBlocks {

    public static Block
            //基础科技
            baseCore, phantomTitaniumSteelCompressor, xuanCrystalManufacturingMachine, phantomSteelCompressor, phantomSteelVoltageMachine, electroge,
            fluvialErosion, adaptiveWall, largeAdaptiveWall, Birefringence, phantomSteelDrill, phantomSteelConveyor, phantomSteelBridge, phantomSteeljunction,
            phantomSteelUnloader, phantomSteelPowerNode, phantomTitaniumSteelPowerNode, excitedYuan, fuelPoweredGenerator, phantomTitaniumSteelConveyor,
            phantomSteelWall, largePhantomSteelWall, phantomTitaniumSteelWall, largePhantomTitaniumSteelWall, curvatureEvolutionPod, quantumFactory, chipMachine,
            BasicManufacturingPlant,
            //进阶科技
            starHarborShipbuildingCenter;

    public static void load() {

        //我超，盒
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
            itemCapacity = 10;
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
                    }}
            );
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
            requirements(Category.distribution, ItemStack.with(new Object[]{MLItems.phantomSteel, 1, Items.graphite, 2}));
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
        //power
        //单位
        //基础制造厂
        BasicManufacturingPlant = new UnitFactory("BasicManufacturingPlant"){{
            requirements(Category.units, ItemStack.with(new Object[]{MLItems.phantomSteel, 80, Items.graphite, 60, Items.silicon, 45}));
            plans = Seq.with(
                    new UnitPlan(MLUnitTypes.drizzle, 60f * 40, ItemStack.with(MLItems.phantomSteel, 20, Items.graphite, 30, Items.silicon, 10)),
                    new UnitPlan(MLUnitTypes.Breeze, 60f * 30, ItemStack.with(MLItems.phantomSteel, 30, Items.silicon, 15)),
                    new UnitPlan(MLUnitTypes.StillWater, 60f * 50, ItemStack.with(MLItems.phantomSteel, 40, Items.silicon, 20, Items.graphite, 30))
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
                    new UnitType[]{MLUnitTypes.StillWater, MLUnitTypes.ripple}
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
                    new UnitType[]{MLUnitTypes.ripple, MLUnitTypes.Turbulence}
            );
        }};
        starHarborShipbuildingCenter = new magical.content.FlexAssembler("starHarborShipbuildingCenter"){{
            requirements(Category.units, ItemStack.with(new Object[]{MLItems.phantomSteel, 300, Items.graphite, 150, Items.silicon, 120, MLItems.phantomTitaniumSteel, 60, MLItems.mysticCrystal, 90}));
            size = 7;

            droneType = UnitTypes.assemblyDrone;
            dronesCreated = 4;
            // 配方：(等级标签, 输出单位, 时间(秒), 范围(格), 需要模块数, 载荷需求...)
            addPlan("T1", UnitTypes.stell, 45f, 4, 0, new PayloadStack(UnitTypes.evoke, 2));
            addPlan("T1", UnitTypes.locus, 55f, 5, 0, new PayloadStack(UnitTypes.evoke, 2));
            addPlan("T1", UnitTypes.elude, 35f, 4, 0, new PayloadStack(Blocks.copperWall, 3));
            addPlan("T2", UnitTypes.quell, 90f, 6, 1, new PayloadStack(UnitTypes.locus, 1), new PayloadStack(UnitTypes.stell, 1));
            addPlan("T2", UnitTypes.avert, 100f, 6, 1, new PayloadStack(UnitTypes.locus, 2));
            addPlan("T2", UnitTypes.precept, 110f, 7, 1, new PayloadStack(Blocks.berylliumWall, 4), new PayloadStack(UnitTypes.evoke, 2));
            addPlan("T3", UnitTypes.obviate, 150f, 8, 2, new PayloadStack(UnitTypes.quell, 2), new PayloadStack(UnitTypes.avert, 1));

            areaSize = 0;

            consumePower(20f);
            consumeLiquid(Liquids.water, 1f);
        }};
         //unit
    }
}