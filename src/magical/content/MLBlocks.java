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
import mindustry.world.blocks.power.Battery;
import mindustry.world.blocks.power.ConsumeGenerator;
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
import mindustry.content.UnitTypes;

import magical.content.MLItems;
import magical.content.MLUnitTypes;
import magical.content.MLTechTree;
import magical.content.MLPlanets;
import magical.content.MLFx;
import magical.magic;
import magical.content.MLSounds;


public class MLBlocks {
    public static Block baseCore;
    public static Block phantomTitaniumSteelCompressor;
    public static Block xuanCrystalManufacturingMachine;
    public static Block phantomSteelCompressor;
    public static Block phantomSteelVoltageMachine;
    public static Block electroge;
    public static Block fluvialErosion;
    public static Block adaptiveWall;
    public static Block largeAdaptiveWall;

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
                hitEffect = MLFx.smallElectricDetonation;
                despawnEffect = MLFx.smallElectricDetonation;
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
            requirements(Category.turret, ItemStack.with(new Object[]{MLItems.phantomSteel, 100, MLItems.phantomTitaniumSteel, 30, MLItems.mysticSteel, 50, Items.metaglass, 80}));
            liquidCapacity = 60f;
            liquidConsumed = 18f / 60f;
            targetInterval = 5f;
            newTargetInterval = 30f;
            targetUnderBlocks = false;
            range = 144f;
            size = 3;
            health = 1200;

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
            timescaleDamage = true;

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
            timescaleDamage = true;

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
            timescaleDamage = true;

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
            timescaleDamage = true;

            colors = new Color[]{Color.valueOf("313131").a(0.55f), Color.valueOf("313131").a(0.7f), Color.valueOf("313131").a(0.8f), Color.valueOf("313131"), Color.white};
            flareColor = Color.valueOf("313131");
                    }}
            );
        }};
        int whm = 4;
        adaptiveWall = new AdaptiveWall("adaptiveWall"){{
            requirements(Category.defense, ItemStack.with(new Object[]{MLItems.mysticSteel, 6, Items.silicon, 6}));
            health = 110 * whm;
        }};
        largeAdaptiveWall = new AdaptiveWall("adaptiveWall"){{
            requirements(Category.defense, ItemStack.with(new Object[]{MLItems.mysticSteel, 6 * whm, Items.silicon, 6 * whm}));
            health = 110 * whm * 4;
            size = 2;
        }};
    }
}