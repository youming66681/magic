package magical.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ai.*;
import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.unit.*;
import mindustry.type.weapons.*;
import mindustry.world.meta.*;
import mindustry.type.UnitType;
import mindustry.content.UnitTypes;
import mindustry.content.Fx;
import mindustry.entities.abilities.ShieldArcAbility;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.entities.pattern.ShootAlternate;
import mindustry.content.StatusEffects;
import mindustry.gen.MechUnit;
import mindustry.type.weapons.RepairBeamWeapon;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static mindustry.Vars.*;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLTechTree;
import magical.content.MLPlanets;
import magical.content.MLFx;

public class MLUnitTypes {
    public static UnitType
            //陆
            //一级
            drizzle, Drizzle, drizzlingRain, ColdRain, HeavyRain,
    //二级
    war, BeaconFire, War, CrusadeAgainst, ImperialArmy,
    //空
    //一级
    Breeze, SlantingWind, Gale, Storm, Hurricane,
    //二级
    BlazingFire, glow, blazing, Ember, BlazingSplendor,
    //海
    //一级
    StillWater, ripple, Turbulence, TerrifyingWaves, SeaSuffering,
    //二级
    ExpelDarkness, ChasingLight, Dawn, FlowingSun, SplendidBrilliant,
    //核心机
    Popular, SpinningSpear,
    //星舰
    //小型
    Pioneer, Starlight, Qingxiao,
    //大型
    BrokenEdge, FlyingSwallow, Tianshu,
    //旗舰
    Aoba;

    public static void load() {
        //幻境陆军
        //一级
        //t1
        drizzle = new UnitType("drizzle") {{
            constructor = UnitTypes.stell.constructor;
            omniMovement = false;
            rotateMoveFirst = false;
            squareShape = true;
            rotateSpeed = 4;
            speed = 2f;
            hitSize = 20f;

            health = 600;
            armor = 1;
            itemCapacity = 10;
            faceTarget = false;
            weapons.add(new Weapon("magic-drizzle1") {{
                reload = 150f;
                recoil = 3;
                x = 0;
                y = 0;
                shootY = 4f;
                mirror = false;
                rotate = true;
                rotateSpeed = 3;
                //rotationLimit = 30f;
                inaccuracy = 0f;
                ejectEffect = Fx.casing1;
                layerOffset = 0.001f;
                shootSound = MLSounds.shootArtillery;
                shoot.shots = 6;
                shoot.shotDelay = 6;
                bullet = new BasicBulletType(8, 25) {{
                    lifetime = 20;
                    width = 8;
                    height = 16;
                    splashDamageRadius = 24;
                    splashDamage = 25;
                    hitEffect = despawnEffect = MLFx.Explosion1;
                    hitSound = MLSounds.explosion;
                }};
            }});
        }};
        //t2
        Drizzle = new UnitType("Drizzle") {{
            constructor = UnitTypes.stell.constructor;
            omniMovement = false;
            rotateMoveFirst = false;
            squareShape = true;
            rotateSpeed = 3.5f;
            speed = 1.75f;
            hitSize = 28f;
            health = 1200;
            armor = 2;
            itemCapacity = 20;
            faceTarget = false;
            weapons.add(new Weapon("magic-Drizzle1") {{
                reload = 20f;
                recoil = 0;
                x = 0;
                y = 0;
                shootY = 8f;
                mirror = false;
                rotate = true;
                rotateSpeed = 2;
                //rotationLimit = 30f;
                inaccuracy = 2f;
                ejectEffect = Fx.casing2;
                layerOffset = 0.001f;
                shootSound = MLSounds.shootArtillerySmall;
                parts.addAll(
                        new RegionPart("-l") {{
                            mirror = false;
                            heatProgress = PartProgress.recoil;
                            recoilIndex = 0;
                            progress = PartProgress.recoil;
                            moveY = -2;
                        }},
                        new RegionPart("-r") {{
                            mirror = false;
                            heatProgress = PartProgress.recoil;
                            recoilIndex = 1;
                            progress = PartProgress.recoil;
                            moveY = -2;
                        }});
                recoils = 2;
                bullet = new BasicBulletType(8, 30) {{
                    lifetime = 24;
                    width = 6;
                    height = 12;
                    splashDamageRadius = 16;
                    splashDamage = 30;
                    hitEffect = despawnEffect = MLFx.Explosion2;
                    hitSound = MLSounds.explosion;
                }};
            }});
        }};
        //t3
        drizzlingRain = new UnitType("drizzlingRain") {{
            constructor = UnitTypes.stell.constructor;
            omniMovement = false;
            rotateMoveFirst = false;
            squareShape = true;
            rotateSpeed = 3f;
            speed = 1.5f;
            hitSize = 36f;
            health = 2400;
            armor = 4;
            itemCapacity = 40;
            faceTarget = false;
            crushDamage = 1;
            weapons.add(new Weapon("magic-drizzlingRain1") {{
                reload = 60f;
                recoil = 5;
                x = 0;
                y = 0;
                shootY = 8f;
                mirror = false;
                rotate = true;
                rotateSpeed = 2f;
                inaccuracy = 0f;
                ejectEffect = Fx.casing3;
                layerOffset = 0.001f;
                shootSound = MLSounds.shootArtillery;
                bullet = new BasicBulletType(12, 80) {{
                    lifetime = 20;
                    width = 12;
                    height = 24;
                    splashDamageRadius = 32;
                    splashDamage = 80;
                    hitEffect = despawnEffect = MLFx.Explosion3;
                    hitSound = MLSounds.explosion;
                }};
            }});
        }};
        //t4
        ColdRain = new UnitType("ColdRain") {{
            constructor = UnitTypes.stell.constructor;
            omniMovement = false;
            rotateMoveFirst = false;
            squareShape = true;
            rotateSpeed = 4f;
            speed = 2.5f;
            hitSize = 50f;
            health = 12000;
            armor = 24;
            faceTarget = false;
            crushDamage = 2;
            weapons.add(new Weapon("magic-ColdRain0") {{
                reload = 120f;
                recoil = 4;
                x = 0;
                y = 0;
                mirror = false;
                rotate = true;
                rotateSpeed = 4f;
                inaccuracy = 0f;
                ejectEffect = Fx.casing4;
                layerOffset = 0.001f;
                shootSound = MLSounds.mediumCannon;
                shoot = new ShootBarrel() {{
                    shots = 2;
                    shotDelay = 0f;
                    barrels = new float[]{
                            8f, 12f, 0f,
                            -8f, 12f, 0f
                    };
                }};
                bullet = new BasicBulletType(10, 180) {{
                    lifetime = 16;
                    width = 16;
                    height = 32;
                    splashDamageRadius = 32;
                    splashDamage = 120;
                    hitEffect = despawnEffect = MLFx.Explosion3;
                    hitSound = MLSounds.explosion;
                }};
            }});
        }};
        //t5
        HeavyRain = new UnitType("HeavyRain") {{
            constructor = UnitTypes.stell.constructor;
            omniMovement = false;
            rotateMoveFirst = false;
            squareShape = true;
            rotateSpeed = 2.5f;
            speed = 1f;
            hitSize = 64f;
            health = 28000;
            armor = 36;
            faceTarget = false;
            crushDamage = 4;
            weapons.add(new Weapon("magic-HeavyRain0") {{
                reload = 150f;
                recoil = 2.5f;
                x = 0;
                y = 0;
                mirror = false;
                rotate = true;
                rotateSpeed = 2.5f;
                inaccuracy = 0f;
                ejectEffect = Fx.casing3Double;
                layerOffset = 0.001f;
                shootSound = MLSounds.shootFuse;
                shoot = new ShootBarrel() {{
                    shots = 10;
                    shotDelay = 2.5f;
                    barrels = new float[]{
                            12f, 24f, 0f,
                            -12f, 24f, 0f
                    };
                }};
                bullet = new BasicBulletType(16, 200) {{
                    lifetime = 20;
                    width = 16;
                    height = 32;
                    splashDamageRadius = 32;
                    splashDamage = 100;
                    hitEffect = despawnEffect = MLFx.Explosion3;
                    hitSound = MLSounds.explosion;
                }};
            }});
        }};
        //二级
        //t1
        war = new UnitType("war") {{
            constructor = MechUnit::create;
            canDrown = true;
            rotateSpeed = 3f;
            speed = 1.4f;
            hitSize = 16f;
            health = 900;
            armor = 3;
            baseRotateSpeed = 2.5f;
            mechStepParticles = true;
            mechFrontSway = 0.1f;
            mechSideSway = 0.1f;
            drownTimeMultiplier = 1;
            range = 176;
            weapons.add(new Weapon("magic-war0") {{
                reload = 12f;
                shake = 1f;
                recoil = 2.4f;
                x = 4f;
                y = 0f;
                rotate = false;
                top = false;
                inaccuracy = 2.5f;
                shootSound = MLSounds.pew;
                alternate = true;
                ejectEffect = Fx.casing4;
                shootCone = 15f;
                shootY = 4f;
                shootX = 2f;
                bullet = new BasicBulletType(8f, 20f) {{
                    pierce = true;
                    pierceBuilding = true;
                    pierceCap = 2;
                    lifetime = 23f;
                    shootEffect = Fx.none;
                    trailLength = 4;
                    trailWidth = 2f;
                    trailColor = Color.valueOf("FEEBB3FF");
                    backColor = Color.valueOf("FEEBB3FF");
                    frontColor = Color.valueOf("FEEBB3FF");
                    width = 8f;
                    height = 16f;
                }};
            }});
        }};
        //t2
        BeaconFire = new UnitType("BeaconFire") {{
            constructor = MechUnit::create;
            canDrown = true;
            rotateSpeed = 2.5f;
            speed = 1.8f;
            hitSize = 24f;
            health = 1800;
            armor = 6;
            baseRotateSpeed = 2.5f;
            mechStepParticles = true;
            mechFrontSway = 0.2f;
            mechSideSway = 0.2f;
            drownTimeMultiplier = 2;
            abilities.add(new ShieldArcAbility() {{
                whenShooting = true;
                radius = 12f;
                width = 6f;
                max = 500f;
                regen = 0.5f;
                cooldown = 360f;
                angle = 180f;
                angleOffset = 0f;
            }});
            weapons.add(
                    new Weapon("magic-BeaconFire0") {{
                        reload = 180f;
                        mirror = true;
                        shootY = 6f;
                        x = -6f;
                        y = -1f;
                        rotate = false;
                        recoil = 0f;
                        continuous = true;
                        alternate = false;
                        cooldownTime = 60f;
                        shootSound = MLSounds.beam;
                        shootX = -4f;
                        bullet = new ContinuousLaserBulletType(250f) {{
                            length = 40f;
                            width = 2f;
                            incendChance = 2f;
                            incendSpread = 4f;
                            incendAmount = 1;
                            hitEffect = Fx.none;
                            statusDuration = 60f;
                            drawSize = 40f;
                            lifetime = 180f;
                            shake = 1f;
                            despawnEffect = Fx.smokeCloud;
                            smokeEffect = Fx.none;
                            shootEffect = Fx.none;
                            collidesTeam = true;
                            colors = new Color[]{
                                    Color.valueOf("D86E56FF"),
                                    Color.valueOf("FFA05CFF"),
                                    Color.white
                            };
                        }};
                    }});
        }};
        //t3
        War = new UnitType("War") {{
            constructor = MechUnit::create;
            canDrown = true;
            rotateSpeed = 1.5f;
            speed = 0.8f;
            hitSize = 32f;
            health = 3600;
            armor = 12;
            baseRotateSpeed = 1.5f;
            mechStepParticles = true;
            mechFrontSway = 0.3f;
            mechSideSway = 0.3f;
            drownTimeMultiplier = 3;
            range = 256;
            abilities.add(new ShieldArcAbility() {{
                whenShooting = true;
                radius = 24f;
                width = 6f;
                max = 1000f;
                regen = 1f;
                cooldown = 180f;
                angle = 120f;
                angleOffset = 0f;
            }});
            weapons.add(new Weapon("magic-War0") {{
                reload = 60f;
                shake = 3f;
                recoil = 3f;
                x = 12f;
                rotate = false;
                top = false;
                inaccuracy = 6f;
                shootSound = MLSounds.pao;
                alternate = true;
                ejectEffect = Fx.casing4;
                shootCone = 15f;
                cooldownTime = 60f;
                shootY = 7f;
                shootX = 0f;
                shoot = new ShootAlternate() {{
                    shots = 9;
                    shotDelay = 3f;
                    barrels = 1;
                    spread = 0f;
                }};
                bullet = new BasicBulletType(8f, 90f) {
                    {
                        pierce = true;
                        pierceBuilding = true;
                        pierceCap = 3;
                        lifetime = 32f;
                        shootEffect = Fx.none;
                        trailLength = 6;
                        trailWidth = 3f;
                        trailColor = Color.valueOf("FEEBB3FF");
                        backColor = Color.valueOf("FEEBB3FF");
                        frontColor = Color.valueOf("FEEBB3FF");
                        width = 16f;
                        height = 32f;
                    }
                };
            }});
        }};
        //t4
        CrusadeAgainst = new UnitType("CrusadeAgainst") {{
            constructor = MechUnit::create;
            canDrown = true;
            rotateSpeed = 1.6f;
            speed = 0.6f;
            hitSize = 40f;
            health = 18000;
            armor = 36;
            baseRotateSpeed = 1.6f;
            mechStepParticles = true;
            mechFrontSway = 0.4f;
            mechSideSway = 0.4f;
            drownTimeMultiplier = 4;
            range = 288;
            abilities.add(new ShieldArcAbility() {{
                whenShooting = true;
                radius = 32f;
                width = 8f;
                max = 4000f;
                regen = 2f;
                cooldown = 120f;
                angle = 120f;
                angleOffset = 0f;
            }});
            weapons.add(
                    new Weapon("magic-CrusadeAgainst0") {{
                        reload = 90f;
                        shake = 4f;
                        recoil = 4f;
                        x = 16f;
                        rotate = false;
                        top = false;
                        inaccuracy = 0f;
                        shootSound = MLSounds.laser;
                        alternate = true;
                        ejectEffect = Fx.none;
                        shootY = 8f;
                        shootX = 0f;
                        bullet = new LaserBulletType() {{
                            damage = 160f;
                            smokeEffect = Fx.bigShockwave;
                            lightningSpacing = 8f;
                            lightningLength = 4;
                            lightningDelay = 0.8f;
                            lightningLengthRand = 4;
                            lightningAngleRand = 4f;
                            lightningDamage = 20f;
                            colors = new Color[]{
                                    Color.valueOf("FEEBB3FF"),
                                    Color.valueOf("FEEBB3FF"),
                                    Color.valueOf("FEEBB3FF")
                            };
                            width = 16f;
                            length = 192;
                        }};
                    }},
                    new Weapon("magic-CrusadeAgainst1") {{
                        reload = 60f;
                        shake = 2f;
                        recoil = 2f;
                        x = 12f;
                        rotate = false;
                        top = false;
                        inaccuracy = 2f;
                        shootSound = MLSounds.pao;
                        alternate = true;
                        ejectEffect = Fx.casing3;
                        shootCone = 10f;
                        cooldownTime = 40f;
                        shootY = 4f;
                        shootX = 0f;
                        shoot = new ShootAlternate() {{
                            shots = 8;
                            shotDelay = 2f;
                            barrels = 1;
                            spread = 0f;
                        }};
                        bullet = new BasicBulletType() {{
                            damage = 40f;
                            pierce = true;
                            pierceBuilding = true;
                            pierceCap = 2;
                            speed = 8f;
                            lifetime = 30f;
                            shootEffect = Fx.none;
                            trailLength = 4;
                            trailWidth = 2f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            frontColor = Color.valueOf("FEEBB3FF");
                            width = 8f;
                            height = 24f;
                        }};
                    }},
                    new Weapon("magic-CrusadeAgainst2") {{
                        mirror = true;
                        x = 6f;
                        y = 0f;
                        reload = 120f;
                        shootCone = 40f;
                        shoot = new ShootAlternate() {{
                            shots = 12;
                            shotDelay = 3f;
                            barrels = 1;
                            spread = 0f;
                        }};
                        shootSound = MLSounds.missile;
                        rotate = true;
                        rotateSpeed = 4f;
                        inaccuracy = 0f;
                        controllable = false;
                        autoTarget = true;
                        alternate = false;
                        bullet = new MissileBulletType() {{
                            damage = 40f;
                            splashDamageRadius = 16f;
                            splashDamage = 40f;
                            homingRange = 120f;
                            homingPower = 0.2f;
                            homingDelay = 2f;
                            hitEffect = Fx.flakExplosion;
                            despawnEffect = Fx.flakExplosion;
                            sprite = "magic-导弹";
                            trailLength = 2;
                            trailWidth = 1f;
                            trailEffect = Fx.none;
                            trailColor = Color.valueOf("FFFFFFFF");
                            backColor = Color.valueOf("FF5B5BFF");
                            frontColor = Color.valueOf("E3E3E3FF");
                            width = 8f;
                            height = 24f;
                            speed = 16f;
                            lifetime = 18f;
                        }};
                    }});
        }};
        //t5
        ImperialArmy = new UnitType("ImperialArmy") {{
            constructor = MechUnit::create;
            canDrown = true;
            rotateSpeed = 1f;
            speed = 0.4f;
            hitSize = 64f;
            health = 36000;
            armor = 48;
            baseRotateSpeed = 1f;
            mechStepParticles = true;
            mechFrontSway = 0.5f;
            mechSideSway = 0.5f;
            drownTimeMultiplier = 5;
            range = 360;
            abilities.add(new ShieldArcAbility() {{
                whenShooting = true;
                radius = 40f;
                width = 10f;
                max = 10000f;
                regen = 5f;
                cooldown = 360f;
                angle = 150f;
                angleOffset = 0f;
            }});
            weapons.add(
                    new Weapon("magic-ImperialArmy0") {{
                        reload = 5f;
                        shake = 5f;
                        recoil = 10f;
                        x = 27f;
                        rotate = false;
                        top = false;
                        inaccuracy = 0f;
                        shootSound = MLSounds.shootBig;
                        alternate = true;
                        ejectEffect = Fx.casing4;
                        shootY = 24f;
                        shootX = 6f;
                        bullet = new BasicBulletType() {{
                            damage = 150f;
                            pierce = true;
                            pierceBuilding = true;
                            pierceCap = 3;
                            speed = 16f;
                            lifetime = 22.5f;
                            hitSound = MLSounds.lasercharge2;
                            shootEffect = Fx.shootBig;
                            smokeEffect = Fx.shootBigSmoke;
                            trailLength = 4;
                            trailWidth = 6f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            frontColor = Color.valueOf("FEEBB3FF");
                            width = 16f;
                            height = 32f;
                            trailChance = 0.75f;
                            trailInterval = 20f;
                            trailEffect = new WaveEffect() {{
                                lifetime = 15f;
                                sizeFrom = 0f;
                                sizeTo = 24f;
                                strokeFrom = 0f;
                                strokeTo = 3f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }};
                        }};
                    }});
        }};
        //幻境空军
        //一级
        //t1
        Breeze = new UnitType("Breeze") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            lowAltitude = true;
            faceTarget = true;
            rotateSpeed = 9f;
            speed = 3.2f;
            drag = 0.02f;
            accel = 0.12f;
            hitSize = 18;
            health = 480;
            armor = 1;
            itemCapacity = 0;
            engineOffset = 10;
            engineSize = 3f;
            weapons.add(new Weapon("magic-Breeze1") {{
                shootY = 4f;
                rotate = false;
                mirror = true;
                reload = 30;
                x = 4;
                y = 0;
                shootSound = MLSounds.shootAlt;
                ejectEffect = Fx.casing1;
                layerOffset = 0.001f;
                bullet = new BasicBulletType(8, 40) {{//"circle-bullet"
                    width = 8;
                    height = 16;
                    lifetime = 24;
                    frontColor = Color.white;
                    backColor = Color.white;
                    trailColor = Color.white;
                    trailLength = 4;
                    trailWidth = 2;
                }};
            }});
        }};
        //t2
        SlantingWind = new UnitType("SlantingWind") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            lowAltitude = true;
            faceTarget = true;
            rotateSpeed = 8f;
            speed = 2.8f;
            drag = 0.04f;
            accel = 0.1f;
            hitSize = 24;
            health = 960;
            armor = 2;
            itemCapacity = 0;
            engineOffset = 16;
            engineSize = 4f;
            weapons.add(new Weapon("magic-SlantingWind1") {{
                shootY = 8f;
                rotate = false;
                mirror = true;
                reload = 30;
                x = 4;
                y = 0;
                shootSound = MLSounds.missile;
                ejectEffect = Fx.casing1;
                layerOffset = 0.001f;
                bullet = new BasicBulletType(8, 30, "circle-bullet") {{
                    width = 12;
                    height = 12;
                    lifetime = 24;
                    frontColor = Color.white;
                    backColor = Color.white;
                    trailColor = Color.white;
                    trailLength = 6;
                    trailWidth = 4;
                    splashDamageRadius = 24;
                    splashDamage = 30;
                    hitEffect = despawnEffect = MLFx.smallElectricDetonation;
                    hitSound = MLSounds.explosion;
                }};
            }});
        }};
        //t3
        Gale = new UnitType("Gale") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            lowAltitude = false;
            circleTarget = true;
            faceTarget = true;
            rotateMoveFirst = true;
            omniMovement = true;
            rotateSpeed = 7f;
            speed = 3.5f;
            drag = 0.06f;
            accel = 0.08f;
            hitSize = 28;
            health = 1920;
            armor = 4;
            itemCapacity = 60;
            targetAir = false;
            engineOffset = 14;
            engineSize = 6f;
            targetFlags = new BlockFlag[]{BlockFlag.factory};
            weapons.add(
                    new Weapon("magic-Gale1") {{
                        rotate = false;
                        mirror = false;
                        reload = 60;
                        x = 0;
                        y = 0;
                        shootSound = MLSounds.plasmadrop;
                        ejectEffect = Fx.casing1;
                        layerOffset = 0.001f;
                        bullet = new BasicBulletType(0, 10, "large-bomb") {{
                            ignoreRotation = true;
                            collidesAir = false;
                            maxRange = 10;
                            width = 24;
                            height = 24;
                            lifetime = 30;
                            frontColor = Color.valueOf("C8BA8FFF");
                            backColor = Color.valueOf("958F60FF");
                            splashDamageRadius = 32;
                            splashDamage = 100;
                            incendAmount = 1;
                            incendSpread = 10;
                            incendChance = 0.05f;
                            spin = 1.5f;
                            makeFire = true;
                            hitEffect = despawnEffect = MLFx.Explosion3;
                            hitSound = MLSounds.explosion;
                        }};
                    }});
        }};
        //t4
        Storm = new UnitType("Storm") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            lowAltitude = true;
            faceTarget = false;
            rotateSpeed = 4f;
            speed = 2.4f;
            drag = 0.04f;
            accel = 0.1f;
            hitSize = 48;
            health = 10000;
            armor = 20;
            itemCapacity = 0;
            engineOffset = 16;
            engineSize = 8f;
            range = 280;
            targetFlags = new BlockFlag[]{BlockFlag.core};
            weapons.add(
                    new Weapon("magic-Storm1") {{
                        reload = 30f;
                        recoil = 2f;
                        x = -10f;
                        y = -8f;
                        rotate = true;
                        mirror = true;
                        rotateSpeed = 2f;
                        inaccuracy = 0f;
                        ejectEffect = Fx.smeltsmoke;
                        shootSound = MLSounds.shootFuse;
                        alternate = true;
                        bullet = new PointBulletType() {{
                            hitEffect = Fx.none;
                            despawnEffect = Fx.none;
                            damage = 150f;
                            trailSpacing = 9f;
                            trailEffect = new ParticleEffect() {{
                                particles = 1;
                                baseLength = 0f;
                                lifetime = 30f;
                                line = true;
                                randLength = false;
                                lenFrom = 9f;
                                lenTo = 0f;
                                strokeFrom = 6f;
                                strokeTo = 0f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                                cone = 0f;
                            }};
                            speed = 16f;
                            lifetime = 15f;
                        }};
                    }},
                    new Weapon("magic-Storm0") {{
                        reload = 15f;
                        x = 6f;
                        y = 8f;
                        rotate = true;
                        rotateSpeed = 4f;
                        mirror = true;
                        alternate = true;
                        inaccuracy = 0f;
                        shootSound = MLSounds.laser;
                        shake = 2f;
                        bullet = new BasicBulletType() {{
                            damage = 70f;
                            splashDamage = 60f;
                            splashDamageRadius = 20f;
                            speed = 8f;
                            lifetime = 35f;
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FFFFFF");
                            trailLength = 6;
                            trailWidth = 3f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            hitSound = MLSounds.plasmaboom;
                            shootEffect = new ParticleEffect() {{
                                particles = 3;
                                sizeFrom = 2f;
                                sizeTo = 0f;
                                length = 20f;
                                baseLength = 0f;
                                lifetime = 20f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FFFFFF");
                                cone = 180f;
                            }};
                            width = 16f;
                            height = 32f;
                            hitEffect = new WrapEffect() {{
                                effect = Fx.dynamicSpikes;
                                color = Color.valueOf("FEEBB3FF");
                                rotation = 20f;
                            }};
                        }};
                    }});
        }};
        //t5
        Hurricane = new UnitType("Hurricane") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            lowAltitude = true;
            faceTarget = false;
            rotateSpeed = 5f;
            speed = 2f;
            drag = 0.04f;
            accel = 0.1f;
            hitSize = 56;
            health = 26000;
            armor = 32;
            itemCapacity = 0;
            engineOffset = 28;
            engineSize = 14f;
            range = 240;
            weapons.add(
                    new Weapon("magic-Hurricane0") {{
                        x = 16f;
                        y = 8f;
                        mirror = true;
                        reload = 5f;
                        shootSound = MLSounds.JG;
                        inaccuracy = 2.5f;
                        recoil = 5f;
                        rotate = true;
                        rotateSpeed = 5f;
                        shoot = new ShootAlternate() {{
                            barrels = 2;
                            spread = 2f;
                        }};
                        bullet = new BasicBulletType() {{
                            damage = 25f;
                            pierceCap = 2;
                            pierceBuilding = true;
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            width = 8f;
                            height = 16f;
                            trailLength = 2;
                            trailWidth = 2f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            pierce = true;
                            speed = 16f;
                            lifetime = 15f;
                            hitEffect = new WaveEffect() {{
                                lifetime = 8f;
                                sizeFrom = 0f;
                                sizeTo = 12f;
                                strokeFrom = 1f;
                                strokeTo = 0f;
                                colorFrom = Color.valueOf("FFFFFF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }};
                            despawnEffect = new WaveEffect() {{
                                lifetime = 8f;
                                sizeFrom = 0f;
                                sizeTo = 12f;
                                strokeFrom = 1f;
                                strokeTo = 0f;
                                colorFrom = Color.valueOf("FFFFFF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }};
                        }};
                    }},
                    new Weapon("magic-Hurricane0") {{
                        x = 12f;
                        y = -12f;
                        mirror = true;
                        reload = 2.5f;
                        shootSound = MLSounds.JG;
                        inaccuracy = 5f;
                        recoil = 5f;
                        rotate = true;
                        rotateSpeed = 5f;
                        shoot = new ShootAlternate() {{
                            barrels = 2;
                            spread = 2f;
                        }};
                        bullet = new BasicBulletType() {{
                            damage = 25f;
                            pierceCap = 2;
                            pierceBuilding = true;
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            width = 8f;
                            height = 16f;
                            trailLength = 2;
                            trailWidth = 2f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            pierce = true;
                            speed = 16f;
                            lifetime = 15f;
                            hitEffect = new WaveEffect() {{
                                lifetime = 8f;
                                sizeFrom = 0f;
                                sizeTo = 12f;
                                strokeFrom = 1f;
                                strokeTo = 0f;
                                colorFrom = Color.valueOf("FFFFFF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }};
                            despawnEffect = new WaveEffect() {{
                                lifetime = 8f;
                                sizeFrom = 0f;
                                sizeTo = 12f;
                                strokeFrom = 1f;
                                strokeTo = 0f;
                                colorFrom = Color.valueOf("FFFFFF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }};
                        }};
                    }});
        }};
        //二级
        //t1
        BlazingFire = new UnitType("BlazingFire") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            lowAltitude = true;
            rotateSpeed = 9f;
            speed = 5f;
            drag = 0.02f;
            accel = 0.12f;
            hitSize = 10;
            health = 720;
            armor = 2;
            itemCapacity = 20;
            engineOffset = 6;
            engineSize = 4f;
            range = 24;
            targetFlags = new BlockFlag[]{BlockFlag.turret};
            weapons.add(new Weapon("magic-BlazingFire0") {{
                x = 0f;
                reload = 60f;
                mirror = false;
                rotate = true;
                shootSound = MLSounds.explosion;
                bullet = new BasicBulletType(0f, 160f) {{
                    instantDisappear = true;
                    killShooter = true;
                    shootEffect = Fx.none;
                    smokeEffect = Fx.none;
                    backColor = Color.valueOf("FEEBB3FF");
                    frontColor = Color.valueOf("FEEBB3FF");
                    width = 16f;
                    height = 16f;
                    shrinkY = 0f;
                    hitSound = Sounds.explosion;
                    hitSoundVolume = 20f;
                    status = StatusEffects.slow;
                    statusDuration = 240f;
                    splashDamageRadius = 64f;
                    splashDamage = 140f;
                    buildingDamageMultiplier = 1.1f;
                    absorbable = false;
                    hitEffect = new MultiEffect(
                            new WaveEffect() {{
                                lifetime = 15f;
                                sizeFrom = 0f;
                                sizeTo = 64f;
                                strokeFrom = 0f;
                                strokeTo = 5f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }},
                            new ParticleEffect() {{
                                particles = 12;
                                sizeFrom = 4f;
                                sizeTo = 0f;
                                length = 32f;
                                baseLength = 32f;
                                lifetime = 30f;
                                interp = Interp.pow10Out;
                                sizeInterp = Interp.pow10In;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }},
                            new ParticleEffect() {{
                                particles = 12;
                                line = true;
                                strokeFrom = 3f;
                                strokeTo = 0f;
                                lenFrom = 6f;
                                lenTo = 0f;
                                length = 32f;
                                baseLength = 32f;
                                lifetime = 30f;
                                interp = Interp.pow5Out;
                                sizeInterp = Interp.pow5In;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }}
                    );
                    hitShake = 8f;
                    makeFire = true;
                    incendAmount = 16;
                    incendSpread = 32f;
                    incendChance = 1f;
                    pierce = true;
                    pierceBuilding = true;
                }};
            }});
        }};
        //t2
        glow = new UnitType("glow") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            lowAltitude = true;
            rotateSpeed = 9f;
            speed = 4f;
            drag = 0.04f;
            accel = 0.1f;
            hitSize = 16;
            health = 1440;
            armor = 4;
            engineOffset = 8;
            engineSize = 4f;
            range = 224;
            targetFlags = new BlockFlag[]{BlockFlag.factory};
            weapons.add(new Weapon("magic-glow0") {{
                reload = 60f;
                x = 0f;
                y = 0f;
                rotate = false;
                mirror = false;
                alternate = true;
                inaccuracy = 0f;
                shootSound = MLSounds.missile;
                shake = 2f;
                ignoreRotation = true;
                bullet = new BasicBulletType(8f, 50f) {{
                    speed = 8f;
                    lifetime = 28f;
                    width = 16f;
                    height = 16f;
                    damage = 50f;
                    splashDamage = 50f;
                    splashDamageRadius = 16f;
                    buildingDamageMultiplier = 1.2f;
                    frontColor = Color.valueOf("FEEBB3FF");
                    backColor = Color.valueOf("FEEBB3FF");
                    trailLength = 4;
                    trailWidth = 4f;
                    trailColor = Color.valueOf("FEEBB3FF");
                    hitSound = MLSounds.plasmaboom;
                    despawnEffect = Fx.none;
                    sprite = "circle-bullet";
                    hitEffect = new WrapEffect(
                            Fx.dynamicSpikes,
                            Color.valueOf("FEEBB3FF")
                    ) {{
                        rotation = 16f;
                    }};
                    status = StatusEffects.slow;
                    statusDuration = 240f;
                    makeFire = true;
                    incendAmount = 4;
                    incendSpread = 8f;
                    incendChance = 0.5f;
                }};
            }});
        }};
        //t3
        blazing = new UnitType("blazing") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            circleTarget = true;
            circleTargetRadius = 90;
            faceTarget = true;
            omniMovement = false;
            lowAltitude = true;
            rotateSpeed = 6.75f;
            speed = 4.5f;
            drag = 0.03f;
            accel = 0.08f;
            hitSize = 28;
            health = 2880;
            armor = 8;
            engineOffset = 16;
            engineSize = 8f;
            range = 320;
            trailLength = 9;
            abilities.add(new ShieldArcAbility() {{
                whenShooting = true;
                radius = 24f;
                width = 6f;
                max = 1500f;
                regen = 1.5f;
                cooldown = 300f;
                angle = 150f;
                angleOffset = 0f;
            }});
            targetFlags = new BlockFlag[]{
                    BlockFlag.generator,
                    BlockFlag.reactor,
                    BlockFlag.battery
            };
            weapons.add(
                    new Weapon("magic-blazing0") {{
                        x = -6f;
                        y = 0f;
                        mirror = true;
                        reload = 6f;
                        rotate = false;
                        recoil = 3f;
                        inaccuracy = 3f;
                        shootSound = MLSounds.JG;
                        bullet = new BasicBulletType(16f, 30f) {{
                            width = 8f;
                            height = 16f;
                            lifetime = 16f;
                            pierce = true;
                            pierceBuilding = true;
                            pierceCap = 3;
                            splashDamageRadius = 8f;
                            splashDamage = 20f;
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            trailLength = 4;
                            trailWidth = 2f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            ammoMultiplier = 1f;
                            makeFire = true;
                            incendAmount = 1;
                            incendSpread = 2f;
                            incendChance = 0.125f;
                            hitEffect = new WaveEffect() {{
                                lifetime = 8f;
                                sizeFrom = 0f;
                                sizeTo = 8f;
                                strokeFrom = 0f;
                                strokeTo = 1f;
                                colorFrom = Color.white;
                                colorTo = Color.valueOf("FEEBB3FF");
                            }};
                            despawnEffect = new WaveEffect() {{
                                lifetime = 8f;
                                sizeFrom = 0f;
                                sizeTo = 8f;
                                strokeFrom = 0f;
                                strokeTo = 1f;
                                colorFrom = Color.white;
                                colorTo = Color.valueOf("FEEBB3FF");
                            }};
                        }};
                    }},
                    new Weapon("magic-blazing1") {{
                        x = 0f;
                        y = 0f;
                        reload = 300f;
                        mirror = false;
                        alternate = false;
                        shootSound = MLSounds.missile;
                        shoot = new ShootBarrel() {{
                            shots = 2;
                            shotDelay = 0f;
                            barrels = new float[]{
                                    10f, 0f, 0f,
                                    -10f, 0f, 0f
                            };
                        }};
                        inaccuracy = 0f;
                        shootCone = 180f;
                        bullet = new MissileBulletType(8f, 80f) {{
                            hitSound = MLSounds.plasmaboom;
                            shrinkY = 0f;
                            homingRange = 180f;
                            homingPower = 0.06f;
                            splashDamageRadius = 24f;
                            splashDamage = 160f;
                            makeFire = true;
                            incendAmount = 3;
                            incendSpread = 6f;
                            incendChance = 0.5f;
                            hitEffect = new MultiEffect(
                                    new WaveEffect() {{
                                        lifetime = 25f;
                                        sizeFrom = 0f;
                                        sizeTo = 48f;
                                        strokeFrom = 0f;
                                        strokeTo = 2f;
                                        colorFrom = Color.valueOf("FEEBB3FF");
                                        colorTo = Color.valueOf("FEEBB3FF");
                                    }},
                                    new ParticleEffect() {{
                                        particles = 8;
                                        sizeFrom = 2f;
                                        sizeTo = 0f;
                                        length = 24f;
                                        baseLength = 24f;
                                        lifetime = 30f;
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
                                        lifetime = 30f;
                                        interp = Interp.pow5Out;
                                        sizeInterp = Interp.pow5In;
                                        colorFrom = Color.valueOf("FEEBB3FF");
                                        colorTo = Color.valueOf("FEEBB3FF");
                                    }}
                            );
                            despawnEffect = Fx.none;
                            trailLength = 8;
                            trailWidth = 4f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            width = 16f;
                            height = 32f;
                            lifetime = 40f;
                            sprite = "magic-大导弹";
                        }};
                    }});
        }};
        //t4
        Ember = new UnitType("Ember") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            circleTarget = true;
            circleTargetRadius = 160;
            faceTarget = true;
            omniMovement = false;
            lowAltitude = true;
            rotateSpeed = 4f;
            speed = 4f;
            drag = 0.04f;
            accel = 0.08f;
            hitSize = 56;
            health = 16000;
            armor = 32;
            engineOffset = 32;
            engineSize = 12f;
            range = 400;
            abilities.add(new ShieldArcAbility() {{
                whenShooting = true;
                radius = 32f;
                width = 8f;
                max = 4000f;
                regen = 2f;
                cooldown = 240f;
                angle = 150f;
                angleOffset = 0f;
            }});
            targetFlags = new BlockFlag[]{
                    BlockFlag.turret
            };
            weapons.add(
                    new Weapon("magic-Ember0") {{
                        reload = 180f;
                        mirror = true;
                        shootY = 16f;
                        shootX = 4f;
                        x = 9f;
                        y = 0f;
                        rotate = false;
                        recoil = 0f;
                        continuous = true;
                        alternate = false;
                        shootSound = MLSounds.beam;
                        layerOffset = -0.001f;
                        bullet = new ContinuousLaserBulletType(80f) {{
                            length = 160f;
                            width = 8f;
                            incendChance = 4f;
                            incendSpread = 8f;
                            incendAmount = 2;
                            hitEffect = Fx.none;
                            statusDuration = 60f;
                            lifetime = 180f;
                            shake = 4f;
                            despawnEffect = Fx.smokeCloud;
                            smokeEffect = Fx.smeltsmoke;
                            collidesTeam = true;
                            colors = new Color[]{
                                    Color.valueOf("D86E56FF"),
                                    Color.valueOf("FFA05CFF"),
                                    Color.white
                            };
                        }};
                    }},
                    new Weapon("magic-Ember1") {{
                        x = 0f;
                        y = 0f;
                        reload = 300f;
                        mirror = false;
                        alternate = false;
                        shootSound = MLSounds.missileLaunch;
                        shoot = new ShootBarrel() {{
                            shots = 4;
                            shotDelay = 0f;
                            barrels = new float[]{
                                    8f, 0f, 0f,
                                    -8f, 0f, 0f,
                                    16f, -4f, 0f,
                                    -16f, -4f, 0f
                            };
                        }};
                        inaccuracy = 0f;
                        bullet = new MissileBulletType(8f, 120f) {{
                            hitSound = MLSounds.plasmaboom;
                            shrinkY = 0f;
                            homingRange = 400f;
                            homingPower = 0.04f;
                            homingDelay = 4f;
                            splashDamageRadius = 48f;
                            splashDamage = 180f;
                            makeFire = true;
                            incendAmount = 4;
                            incendSpread = 8f;
                            incendChance = 0.5f;
                            hitEffect = new MultiEffect(
                                    new WaveEffect() {{
                                        lifetime = 25f;
                                        sizeFrom = 0f;
                                        sizeTo = 64f;
                                        strokeFrom = 0f;
                                        strokeTo = 4f;
                                        colorFrom = Color.valueOf("FEEBB3FF");
                                        colorTo = Color.valueOf("FEEBB3FF");
                                    }},
                                    new ParticleEffect() {{
                                        particles = 16;
                                        sizeFrom = 2f;
                                        sizeTo = 0f;
                                        length = 48f;
                                        baseLength = 48f;
                                        lifetime = 30f;
                                        interp = Interp.pow10Out;
                                        sizeInterp = Interp.pow10In;
                                        colorFrom = Color.valueOf("FEEBB3FF");
                                        colorTo = Color.valueOf("FEEBB3FF");
                                    }},
                                    new ParticleEffect() {{
                                        particles = 16;
                                        line = true;
                                        strokeFrom = 4f;
                                        strokeTo = 0f;
                                        lenFrom = 4f;
                                        lenTo = 0f;
                                        length = 48f;
                                        baseLength = 48f;
                                        lifetime = 30f;
                                        interp = Interp.pow5Out;
                                        sizeInterp = Interp.pow5In;
                                        colorFrom = Color.valueOf("FEEBB3FF");
                                        colorTo = Color.valueOf("FEEBB3FF");
                                    }}
                            );
                            despawnEffect = Fx.none;
                            trailLength = 8;
                            trailWidth = 3f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            width = 16f;
                            height = 32f;
                            lifetime = 50f;
                            sprite = "magic-大导弹";
                        }};
                    }});
        }};
        //t5
        BlazingSplendor = new UnitType("BlazingSplendor") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            circleTarget = true;
            circleTargetRadius = 200;
            faceTarget = true;
            omniMovement = false;
            lowAltitude = false;
            rotateSpeed = 2.5f;
            speed = 2.5f;
            drag = 0.025f;
            accel = 0.05f;
            hitSize = 72;
            health = 32000;
            armor = 40;
            engineOffset = 24;
            engineSize = 12f;
            targetFlags = new BlockFlag[]{
                    BlockFlag.generator,
                    BlockFlag.reactor,
                    BlockFlag.battery
            };
            weapons.add(
                    new Weapon("magic-Gale1") {{
                        rotate = false;
                        mirror = false;
                        reload = 60;
                        x = 0;
                        y = 0;
                        shootSound = MLSounds.plasmadrop;
                        ejectEffect = Fx.casing1;
                        layerOffset = 0.001f;
                        bullet = new BasicBulletType(0, 250, "magic-十字星") {{
                            ignoreRotation = true;
                            collidesAir = false;
                            collides = false;
                            maxRange = 40;
                            width = 96;
                            height = 96;
                            lifetime = 45;
                            frontColor = Color.valueOf("C8BA8FFF");
                            backColor = Color.valueOf("958F60FF");
                            splashDamageRadius = 48;
                            splashDamage = 750;
                            incendAmount = 5;
                            incendSpread = 10;
                            incendChance = 0.5f;
                            spin = 2.5f;
                            makeFire = true;
                            hitEffect = despawnEffect = MLFx.Explosion4;
                            hitSound = MLSounds.explosion;
                            fragBullets = 10;
                            fragBullet = new BasicBulletType(4, 150, "magic-十字星") {{
                                ignoreRotation = true;
                                collidesAir = false;
                                collides = false;
                                width = 48;
                                height = 48;
                                lifetime = 40;
                                frontColor = Color.valueOf("C8BA8FFF");
                                backColor = Color.valueOf("958F60FF");
                                splashDamageRadius = 48;
                                splashDamage = 350;
                                incendAmount = 5;
                                incendSpread = 5;
                                incendChance = 0.25f;
                                spin = 2.5f;
                                makeFire = true;
                                hitEffect = despawnEffect = MLFx.Explosion4;
                                hitSound = MLSounds.explosion;
                            }};
                        }};
                    }});
        }};
        //幻境海军
        //一级
        //t1
        StillWater = new UnitType("StillWater") {{
            constructor = UnitTypes.risso.constructor;
            speed = 1.4f;
            rotateSpeed = 7;
            waveTrailX = 0;
            waveTrailY = -6;
            hitSize = 18;
            health = 720;
            armor = 1;
            faceTarget = false;
            weapons.add(new Weapon("magic-StillWater1") {{
                reload = 25f;
                recoil = 1.5f;
                x = 0;
                y = 0;
                shootY = 8f;
                mirror = false;
                rotate = true;
                rotateSpeed = 6f;
                inaccuracy = 0f;
                ejectEffect = Fx.casing3;
                layerOffset = 0.001f;
                shootSound = MLSounds.shootArtillery;
                bullet = new BasicBulletType(8, 20) {{
                    lifetime = 25;
                    width = 8;
                    height = 16;
                    splashDamageRadius = 24;
                    splashDamage = 20;
                    hitEffect = despawnEffect = MLFx.Explosion1;
                    hitSound = MLSounds.explosion;
                }};
            }});
        }};
        //t2
        ripple = new UnitType("ripple") {{
            constructor = UnitTypes.risso.constructor;
            speed = 1.2f;
            rotateSpeed = 6;
            waveTrailX = 0;
            waveTrailY = -8;
            hitSize = 24;
            health = 1440;
            armor = 2;
            faceTarget = false;
            weapons.add(new Weapon("magic-ripple1") {{
                reload = 30f;
                recoil = 2f;
                x = 4;
                y = 0;
                shootY = 8f;
                mirror = true;
                rotate = true;
                rotateSpeed = 4f;
                inaccuracy = 0f;
                ejectEffect = Fx.casing2;
                layerOffset = 0.001f;
                shootSound = MLSounds.shootArtillery;
                bullet = new BasicBulletType(8, 35) {{
                    lifetime = 30;
                    width = 8;
                    height = 16;
                    splashDamageRadius = 16;
                    splashDamage = 35;
                    hitEffect = despawnEffect = MLFx.Explosion2;
                    hitSound = MLSounds.explosion;
                }};
            }});
        }};
        //t3
        Turbulence = new UnitType("Turbulence") {{
            constructor = UnitTypes.risso.constructor;
            speed = 1f;
            rotateSpeed = 5;
            waveTrailX = 0;
            waveTrailY = -8;
            hitSize = 28;
            health = 2880;
            armor = 4;
            faceTarget = false;
            weapons.add(new Weapon("magic-Turbulence1") {{
                reload = 180f;
                recoil = 3f;
                x = 0;
                y = -4;
                shootY = 8f;
                mirror = false;
                rotate = true;
                rotateSpeed = 3f;
                inaccuracy = 6f;
                ejectEffect = Fx.casing3;
                layerOffset = 0.001f;
                shootSound = MLSounds.missile;
                shoot.shots = 30;
                shoot.shotDelay = 3;
                bullet = new BasicBulletType(14, 25) {{
                    lifetime = 20;
                    width = 8;
                    height = 16;
                    splashDamageRadius = 32;
                    splashDamage = 25;
                    hitEffect = despawnEffect = MLFx.Explosion3;
                    hitSound = MLSounds.explosion;
                    trailColor = Color.white;
                    trailLength = 6;
                    trailWidth = 4;
                }};
            }});
        }};
        //t4
        TerrifyingWaves = new UnitType("TerrifyingWaves") {{
            constructor = UnitTypes.risso.constructor;
            speed = 0.8f;
            rotateSpeed = 4;
            waveTrailX = 0;
            waveTrailY = -24;
            hitSize = 48;
            health = 14000;
            armor = 28;
            faceTarget = false;
            weapons.add(
                    new Weapon("magic-TerrifyingWaves0") {{
                        reload = 60f;
                        recoil = 3f;
                        x = 0;
                        y = -12;
                        shootY = 24f;
                        mirror = false;
                        rotate = true;
                        rotateSpeed = 2f;
                        inaccuracy = 4f;
                        ejectEffect = Fx.casing4;
                        layerOffset = 0.001f;
                        shootSound = MLSounds.mediumCannon;
                        shoot = new ShootAlternate() {{
                            barrels = 2;
                            spread = 4f;
                            shotDelay = 15f;
                        }};
                        parts.addAll(
                                new RegionPart("-l") {{
                                    mirror = false;
                                    heatProgress = PartProgress.recoil;
                                    recoilIndex = 0;
                                    progress = PartProgress.recoil;
                                    moveY = -4;
                                }},
                                new RegionPart("-r") {{
                                    mirror = false;
                                    heatProgress = PartProgress.recoil;
                                    recoilIndex = 1;
                                    progress = PartProgress.recoil;
                                    moveY = -4;
                                }});
                        recoils = 2;
                        bullet = new BasicBulletType(16, 120) {{
                            lifetime = 20;
                            width = 16;
                            height = 32;
                            splashDamageRadius = 48;
                            splashDamage = 180;
                            hitEffect = despawnEffect = MLFx.Explosion4;
                            hitSound = MLSounds.explosion;
                            trailLength = 6;
                            trailWidth = 4;
                        }};
                    }},
                    new Weapon("magic-TerrifyingWaves1") {{
                        reload = 10f;
                        recoil = 2.5f;
                        x = 12;
                        y = 4;
                        mirror = true;
                        rotate = true;
                        rotateSpeed = 4f;
                        inaccuracy = 0f;
                        ejectEffect = Fx.casing1;
                        layerOffset = 0.001f;
                        shootSound = MLSounds.pew;
                        bullet = new BasicBulletType(8, 10) {{
                            lifetime = 30;
                            width = 8;
                            height = 16;
                            hitEffect = despawnEffect = Fx.hitBulletColor;
                        }};
                    }},
                    new Weapon("magic-TerrifyingWaves1") {{
                        reload = 10f;
                        recoil = 2.5f;
                        x = 10;
                        y = 14;
                        mirror = true;
                        rotate = true;
                        rotateSpeed = 4f;
                        inaccuracy = 0f;
                        ejectEffect = Fx.casing1;
                        layerOffset = 0.001f;
                        shootSound = MLSounds.pew;
                        bullet = new BasicBulletType(8, 10) {{
                            lifetime = 30;
                            width = 8;
                            height = 16;
                            hitEffect = despawnEffect = Fx.hitBulletColor;
                        }};
                    }},
                    new Weapon("magic-TerrifyingWaves1") {{
                        reload = 10f;
                        recoil = 2.5f;
                        x = 14;
                        y = -18;
                        mirror = true;
                        rotate = true;
                        rotateSpeed = 4f;
                        inaccuracy = 0f;
                        ejectEffect = Fx.casing1;
                        layerOffset = 0.001f;
                        shootSound = MLSounds.pew;
                        bullet = new BasicBulletType(8, 10) {{
                            lifetime = 30;
                            width = 8;
                            height = 16;
                            hitEffect = despawnEffect = Fx.hitBulletColor;
                        }};
                    }});
        }};
        //t5
        SeaSuffering = new UnitType("SeaSuffering") {{
            constructor = UnitTypes.risso.constructor;
            speed = 0.6f;
            rotateSpeed = 3;
            waveTrailX = 0;
            waveTrailY = -48;
            hitSize = 72;
            health = 30000;
            armor = 30;
            range = 360f;
            faceTarget = false;
            weapons.add(
                    new Weapon("magic-SeaSuffering0") {{
                        reload = 25f;
                        x = 12f;
                        y = 24f;
                        rotate = true;
                        rotateSpeed = 2.5f;
                        mirror = true;
                        alternate = true;
                        inaccuracy = 5f;
                        shootSound = MLSounds.laser;
                        shake = 2.5f;
                        bullet = new BasicBulletType() {{
                            damage = 110f;
                            lifetime = 45f;
                            speed = 8f;
                            width = 16f;
                            height = 24f;
                            hitSize = 24f;
                            splashDamageRadius = 48f;
                            splashDamage = 90f;
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            trailLength = 10;
                            trailWidth = 6f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            ammoMultiplier = 1f;
                            hitSound = MLSounds.plasmaboom;
                            hitEffect = despawnEffect = new MultiEffect(
                                    new WaveEffect() {{
                                        lifetime = 30f;
                                        sizeFrom = 0f;
                                        sizeTo = 80f;
                                        strokeFrom = 0f;
                                        strokeTo = 5f;
                                        colorFrom = Color.valueOf("FEEBB3FF");
                                        colorTo = Color.valueOf("FEEBB3FF");
                                    }},
                                    new ParticleEffect() {{
                                        particles = 10;
                                        sizeFrom = 10f;
                                        sizeTo = 0f;
                                        length = 50f;
                                        baseLength = 0f;
                                        lifetime = 30f;
                                        colorFrom = Color.valueOf("FEEBB3FF");
                                        colorTo = Color.valueOf("FEEBB3FF");
                                        interp = Interp.pow10Out;
                                        sizeInterp = Interp.pow10In;
                                    }}
                            );
                            smokeEffect = Fx.smokeCloud;
                            trailChance = 1f;
                            trailInterval = 20f;
                            trailEffect = new ParticleEffect() {{
                                particles = 10;
                                length = 10f;
                                baseLength = 16f;
                                lifetime = 10f;
                                sizeFrom = 4f;
                                sizeTo = 0f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }};
                        }};
                    }},
                    new Weapon("magic-SeaSuffering1") {{
                        reload = 150f;
                        x = 16f;
                        y = 0f;
                        rotate = true;
                        rotateSpeed = 2.5f;
                        mirror = true;
                        alternate = true;
                        inaccuracy = 0f;
                        shootSound = MLSounds.laserblast;
                        chargeSound = MLSounds.lasercharge;
                        shoot = new ShootPattern() {{
                            firstShotDelay = 80f;
                        }};
                        shake = 10f;
                        shootY = 4f;
                        bullet = new LaserBulletType() {{
                            length = 360f;
                            width = 48f;
                            damage = 250f;
                            lifetime = 60f;
                            ammoMultiplier = 1f;
                            colors = new Color[]{
                                    Color.valueOf("958F60FF"),
                                    Color.valueOf("C8BA8FFF"),
                                    Color.valueOf("FFFFFFFF")
                            };
                            despawnEffect = Fx.none;
                            hitEffect = Fx.none;
                            smokeEffect = Fx.bigShockwave;
                            chargeEffect = Fx.greenLaserCharge;
                        }};
                    }},
                    new Weapon("magic-SeaSuffering2") {{
                        autoTarget = true;
                        controllable = false;
                        shoot = new ShootBarrel() {{
                            shots = 2;
                            shotDelay = 0f;
                            barrels = new float[]{
                                    3f, 2f, 0f,
                                    -3f, 2f, 0f
                            };
                        }};
                        reload = 2f;
                        ejectEffect = Fx.casing2Double;
                        mirror = true;
                        rotateSpeed = 4f;
                        rotate = true;
                        x = 10f;
                        y = -24f;
                        inaccuracy = 0f;
                        alternate = false;
                        shootSound = MLSounds.shoot;
                        bullet = new FlakBulletType() {{
                            collidesAir = true;
                            collidesGround = false;
                            hitEffect = Fx.none;
                            despawnEffect = Fx.none;
                            damage = 8f;
                            speed = 16f;
                            lifetime = 22.5f;
                            width = 4f;
                            height = 8f;
                        }};
                    }});
        }};
        //二级
        //t1
        ExpelDarkness = new UnitType("ExpelDarkness") {{
            constructor = UnitTypes.risso.constructor;
            speed = 1.6f;
            rotateSpeed = 6;
            waveTrailX = 0;
            waveTrailY = -6;
            hitSize = 18;
            health = 1080;
            armor = 4;
            faceTarget = false;
            weapons.add(new Weapon("magic-ExpelDarkness0") {{
                reload = 30f;
                recoil = 1.5f;
                x = 0;
                y = 0;
                shootY = 0f;
                mirror = false;
                rotate = true;
                rotateSpeed = 6f;
                inaccuracy = 0f;
                ejectEffect = Fx.casing3;
                layerOffset = 0.001f;
                shootSound = MLSounds.JG;
                bullet = new BasicBulletType(8, 30) {{
                    lifetime = 30;
                    width = 10;
                    height = 20;
                    trailLength = 6;
                    trailWidth = 3f;
                    trailColor = Color.valueOf("FEEBB3FF");
                    frontColor = Color.valueOf("FEEBB3FF");
                    backColor = Color.valueOf("FEEBB3FF");
                    splashDamageRadius = 24;
                    splashDamage = 30;
                    hitEffect = despawnEffect = MLFx.smallEnergyBlast;
                    hitSound = MLSounds.plasmaboom;
                }};
            }});
        }};
        //t2
        ChasingLight = new UnitType("ChasingLight") {{
            constructor = UnitTypes.risso.constructor;
            speed = 1.4f;
            rotateSpeed = 5;
            waveTrailX = 0;
            waveTrailY = -8;
            hitSize = 24;
            health = 2160;
            armor = 6;
            faceTarget = false;
            weapons.add(
                    new Weapon("magic-ChasingLight0") {{
                        reload = 40f;
                        recoil = 2f;
                        x = 0;
                        y = 0;
                        shootY = 0f;
                        mirror = false;
                        rotate = true;
                        rotateSpeed = 5f;
                        inaccuracy = 0f;
                        ejectEffect = Fx.casing2;
                        layerOffset = 0.001f;
                        shootSound = MLSounds.shootFuse;
                        bullet = new BasicBulletType(10, 30) {{
                            lifetime = 20;
                            width = 12;
                            height = 24;
                            trailLength = 6;
                            trailWidth = 3f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            hitEffect = despawnEffect = Fx.none;
                            hitSound = MLSounds.laser;
                            fragBullets = 1;
                            fragSpread = 0;
                            fragVelocityMin = 0;
                            fragRandomSpread = 0;
                            fragBullet = new LaserBulletType(60f) {{
                                hitSound = MLSounds.laser;
                                lifetime = 32f;
                                width = 24f;
                                length = 96f;
                                Color.valueOf("FEEBB3FF");
                                collidesTeam = true;
                                hitEffect = Fx.none;
                                despawnEffect = Fx.none;
                                colors = new Color[]{
                                        Color.valueOf("FEEBB3FF"),
                                        Color.valueOf("FEEBB3FF"),
                                        Color.valueOf("FEEBB3FF")
                                };
                            }};
                        }};
                    }});
        }};
        //t3
        Dawn = new UnitType("Dawn") {{
            constructor = UnitTypes.risso.constructor;
            speed = 1.2f;
            rotateSpeed = 4;
            waveTrailX = 0;
            waveTrailY = -10;
            hitSize = 28;
            health = 4320;
            armor = 8;
            faceTarget = false;
            weapons.add(
                    new Weapon("magic-Dawn0") {{
                        reload = 50f;
                        recoil = 3f;
                        x = 0;
                        y = 0;
                        shootY = 0f;
                        mirror = false;
                        rotate = true;
                        rotateSpeed = 5f;
                        inaccuracy = 0f;
                        ejectEffect = Fx.casing4;
                        layerOffset = 0.001f;
                        shootSound = MLSounds.plasmadrop;
                        bullet = new BasicBulletType(12, 120) {{
                            sprite = "magic-十字星";
                            spin = 3;
                            lifetime = 25;
                            width = 48;
                            height = 48;
                            trailLength = 6;
                            trailWidth = 3f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            splashDamage = 80f;
                            splashDamageRadius = 24f;
                            hitEffect = despawnEffect = MLFx.smallEnergyBlast;
                            hitSound = MLSounds.plasmaboom;
                            fragBullets = 3;
                            fragBullet = new BasicBulletType(6, 50) {{
                                lifetime = 12;
                                width = 8;
                                height = 16;
                                trailLength = 4;
                                trailWidth = 2f;
                                trailColor = Color.valueOf("FEEBB3FF");
                                frontColor = Color.valueOf("FEEBB3FF");
                                backColor = Color.valueOf("FEEBB3FF");
                                hitEffect = despawnEffect = new WrapEffect(
                                        Fx.dynamicSpikes,
                                        Color.valueOf("FEEBB3FF")
                                ) {{
                                    rotation = 16f;
                                }};
                                hitSound = MLSounds.plasmaboom;
                            }};
                        }};
                    }});
        }};
        //t4
        FlowingSun = new UnitType("FlowingSun") {{
            constructor = UnitTypes.risso.constructor;
            speed = 1f;
            rotateSpeed = 3;
            waveTrailX = 0;
            waveTrailY = -32;
            hitSize = 56;
            health = 20000;
            armor = 40;
            faceTarget = false;
            range = 320;
            weapons.add(
                    new Weapon("magic-FlowingSun0") {{
                        reload = 90f;
                        x = 0f;
                        y = -8f;
                        rotate = true;
                        rotateSpeed = 4f;
                        mirror = false;
                        alternate = true;
                        inaccuracy = 0f;
                        shootSound = MLSounds.laser;
                        shake = 8f;
                        bullet = new BasicBulletType() {{
                            damage = 220f;
                            lifetime = 20f;
                            speed = 16f;
                            width = 16f;
                            height = 32f;
                            hitSize = 24f;
                            splashDamageRadius = 48f;
                            splashDamage = 180f;
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            trailLength = 10;
                            trailWidth = 3f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            ammoMultiplier = 1f;
                            hitSound = MLSounds.plasmaboom;
                            hitEffect = despawnEffect = new MultiEffect(
                                    new WaveEffect() {{
                                        lifetime = 30f;
                                        sizeFrom = 0f;
                                        sizeTo = 96f;
                                        strokeFrom = 0f;
                                        strokeTo = 5f;
                                        colorFrom = Color.valueOf("FEEBB3FF");
                                        colorTo = Color.valueOf("FEEBB3FF");
                                    }},
                                    new ParticleEffect() {{
                                        particles = 10;
                                        sizeFrom = 10f;
                                        sizeTo = 0f;
                                        length = 48f;
                                        baseLength = 48f;
                                        lifetime = 30f;
                                        colorFrom = Color.valueOf("FEEBB3FF");
                                        colorTo = Color.valueOf("FEEBB3FF");
                                        interp = Interp.pow10Out;
                                        sizeInterp = Interp.pow10In;
                                    }}
                            );
                            smokeEffect = Fx.smokeCloud;
                            trailChance = 1f;
                            trailInterval = 20f;
                            trailEffect = new ParticleEffect() {{
                                particles = 8;
                                length = 8f;
                                baseLength = 0f;
                                lifetime = 8;
                                sizeFrom = 4f;
                                sizeTo = 0f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }};
                        }};
                    }},
                    new Weapon("magic-FlowingSun1") {{
                        reload = 30f;
                        recoil = 4f;
                        x = 8;
                        y = 16;
                        shootY = 0f;
                        mirror = true;
                        rotate = true;
                        rotateSpeed = 5f;
                        inaccuracy = 0f;
                        ejectEffect = Fx.casing2;
                        layerOffset = 0.001f;
                        shootSound = MLSounds.shootFuse;
                        bullet = new BasicBulletType(12, 80) {{
                            lifetime = 24;
                            width = 12;
                            height = 24;
                            trailLength = 6;
                            trailWidth = 3f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            hitEffect = despawnEffect = Fx.none;
                            hitSound = MLSounds.laser;
                            fragBullets = 1;
                            fragSpread = 0;
                            fragVelocityMin = 0;
                            fragRandomSpread = 0;
                            fragBullet = new LaserBulletType(160f) {{
                                hitSound = MLSounds.laser;
                                lifetime = 32f;
                                width = 24f;
                                length = 128f;
                                Color.valueOf("FEEBB3FF");
                                collidesTeam = true;
                                hitEffect = Fx.none;
                                despawnEffect = Fx.none;
                                colors = new Color[]{
                                        Color.valueOf("FEEBB3FF"),
                                        Color.valueOf("FEEBB3FF"),
                                        Color.valueOf("FEEBB3FF")
                                };
                            }};
                        }};
                    }});
        }};
        //t5
        SplendidBrilliant = new UnitType("SplendidBrilliant") {{
            constructor = UnitTypes.risso.constructor;
            speed = 0.8f;
            rotateSpeed = 2;
            waveTrailX = 0;
            waveTrailY = -40;
            hitSize = 81;
            health = 40000;
            armor = 56;
            faceTarget = false;
            range = 400;
            abilities.add(
                    new ForceFieldAbility(
                            150f,     // radius
                            2.5f,     // regen
                            10000f,    // max
                            600f,     // cooldown
                            6,        // sides
                            0f        // rotation
                    )
            );
            weapons.add(
                    new Weapon("magic-SplendidBrilliant0") {{
                        reload = 120f;
                        x = 0f;
                        y = -12f;
                        rotate = true;
                        rotateSpeed = 2.5f;
                        mirror = false;
                        alternate = true;
                        inaccuracy = 0f;
                        shootSound = MLSounds.explosionAfflict;
                        shake = 10f;
                        parts.addAll(
                                new RegionPart("-l") {{
                                    mirror = false;
                                    heatProgress = PartProgress.recoil;
                                    recoilIndex = 0;
                                    progress = PartProgress.recoil;
                                    moveY = -4;
                                }},
                                new RegionPart("-m") {{
                                    mirror = false;
                                    heatProgress = PartProgress.recoil;
                                    recoilIndex = 1;
                                    progress = PartProgress.recoil;
                                    moveY = -4;
                                }},
                                new RegionPart("-r") {{
                                    mirror = false;
                                    heatProgress = PartProgress.recoil;
                                    recoilIndex = 2;
                                    progress = PartProgress.recoil;
                                    moveY = -4;
                                }}
                        );
                        recoils = 3;
                        shoot = new ShootBarrel() {{
                            shots = 3;
                            shotDelay = 20f;
                            barrels = new float[]{
                                    8f, 32f, 0f,
                                    0f, 32f, 0f,
                                    -8f, 32f, 0f
                            };
                        }};
                        bullet = new BasicBulletType() {{
                            damage = 500f;
                            lifetime = 25f;
                            speed = 16f;
                            width = 24f;
                            height = 48f;
                            hitSize = 24f;
                            splashDamageRadius = 48f;
                            splashDamage = 1000f;
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            trailLength = 9;
                            trailWidth = 3f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            hitSound = MLSounds.explosionCleroi;
                            hitEffect = despawnEffect = MLFx.EnergyExplosion;
                            smokeEffect = Fx.smokeCloud;
                            trailChance = 1f;
                            trailInterval = 20f;
                            trailEffect = new ParticleEffect() {{
                                particles = 10;
                                length = 15f;
                                baseLength = 0f;
                                lifetime = 10;
                                sizeFrom = 5f;
                                sizeTo = 0f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }};
                        }};
                    }});
        }};
        //核心机
        //风行
        Popular = new UnitType("Popular") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            lowAltitude = true;
            rotateSpeed = 8f;
            speed = 4.2f;
            drag = 0.04f;
            accel = 0.08f;
            hitSize = 28;
            health = 220;
            armor = 2;
            itemCapacity = 0;
            engineOffset = 16;
            engineSize = 4f;
            mineSpeed = 7.5f;
            mineTier = 2;
            itemCapacity = 60;
            buildSpeed = 0.9f;
            weapons.add(new Weapon("magic-Popular0") {{
                shootY = 0f;
                rotate = false;
                mirror = false;
                reload = 30;
                x = 0;
                y = 0;
                shootSound = MLSounds.laser;
                ejectEffect = Fx.none;
                layerOffset = 0.001f;
                bullet = new LaserBulletType(25f) {{
                    healPercent = 2.5f;
                    width = 16;
                    length = width * 8;
                    colors = new Color[]{Color.valueOf("FEEBB3FF"), Color.valueOf("FEEBB3FF"), Color.valueOf("FEEBB3FF")};
                    smokeEffect = Fx.none;
                }};
            }});
        }};
        //旋戈
        SpinningSpear = new UnitType("SpinningSpear") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            lowAltitude = true;
            rotateSpeed = 10f;
            speed = 5f;
            drag = 0.06f;
            accel = 0.08f;
            hitSize = 36;
            health = 1200;
            armor = 6;
            itemCapacity = 0;
            mineSpeed = 10f;
            mineTier = 3;
            itemCapacity = 100;
            buildRange = 270;
            buildSpeed = 3f;
            range = 240;
            abilities.add(
                    new ForceFieldAbility(
                            90f,     // radius
                            0.6f,     // regen
                            600f,    // max
                            320f,     // cooldown
                            4,        // sides
                            0f        // rotation
                    )
            );
            abilities.add(
                    new ForceFieldAbility(
                            90f,
                            0.6f,
                            600f,
                            320f,
                            4,
                            45f
                    )
            );
            weapons.add(
                    new Weapon("magic-SpinningSpear0") {{
                        shootY = 0f;
                        rotate = false;
                        mirror = false;
                        reload = 15;
                        x = 0;
                        y = 0;
                        shootSound = MLSounds.laser;
                        ejectEffect = Fx.none;
                        layerOffset = 0.001f;
                        bullet = new LaserBulletType(50f) {{
                            healPercent = 5f;
                            width = 32;
                            length = width * 4;
                            colors = new Color[]{Color.valueOf("FEEBB3FF"), Color.valueOf("FEEBB3FF"), Color.valueOf("FEEBB3FF")};
                            smokeEffect = Fx.none;
                        }};
                    }},
                    new Weapon("magic-SpinningSpear1") {{
                        reload = 15f;
                        recoil = 0f;
                        x = 8;
                        y = 16;
                        shootY = 0f;
                        mirror = true;
                        rotate = false;
                        rotateSpeed = 6f;
                        inaccuracy = 0f;
                        ejectEffect = Fx.casing2;
                        layerOffset = 0.001f;
                        bullet = new BasicBulletType(12, 15) {{
                            lifetime = 20;
                            width = 12;
                            height = 24;
                            trailLength = 6;
                            trailWidth = 3f;
                            homingRange = 120f;
                            homingPower = 0.04f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                        }};
                    }});
        }};
        //星舰
        //小型
        Pioneer = new UnitType("Pioneer") {{
            constructor = UnitTypes.assemblyDrone.constructor;
            flying = true;
            controller = u -> new AssemblerAI();
            lowAltitude = true;
            speed = 5f;
            rotateSpeed = 5f;
            hitSize = 16;
            buildBeamOffset = 8;
            health = 400;
            isEnemy = false;
            hidden = true;
            useUnitCap = false;
            logicControllable = false;
            playerControllable = false;
            allowedInPayloads = false;
            createWreck = false;
            engineOffset = 5;
            engineSize = 3;
            weapons.add(new RepairBeamWeapon("magic-repair") {{
                x = 0f;
                y = 0f;
                shootY = 0f;
                mirror = false;
                beamWidth = 1f;
                repairSpeed = 5f;
                bullet = new BulletType() {{
                    maxRange = 80f;
                }};
            }});
        }};
        Starlight = new UnitType("Starlight") {{
            constructor = UnitTypes.flare.constructor;
            aiController = FlyingFollowAI::new;
            flying = true;
            lowAltitude = true;
            rotateMoveFirst = true;
            omniMovement = true;
            rotateSpeed = 4f;
            speed = 4f;
            drag = 0.02f;
            accel = 0.08f;
            hitSize = 48;
            health = 8800;
            armor = 16;
            itemCapacity = 0;
            engineOffset = 0;
            engineSize = 0f;
            buildSpeed = 6;
            buildRange = 320;
            mineSpeed = 15f;
            mineTier = 4;
            itemCapacity = 200;
            trailLength = 12;
            abilities.add(
                    new ForceFieldAbility(
                            160f,     // radius
                            0.8f,     // regen
                            800f,    // max
                            240f,     // cooldown
                            4,        // sides
                            0f        // rotation
                    )
            );
            abilities.add(
                    new ForceFieldAbility(
                            160f,
                            0.8f,
                            800f,
                            240f,
                            4,
                            45f
                    )
            );
            abilities.add(new EnergyFieldAbility(
                    5f,   // damage
                    90f,   // reload
                    240f   // range
            ) {{
                healPercent = 0.5f;
                x = 0f;
                y = 0f;
                maxTargets = 15;
                effectRadius = 3f;
                damageEffect = Fx.chainLightning;
                shootSound = MLSounds.spark;
                status = StatusEffects.shocked;
            }});
            weapons.add(
                    new RepairBeamWeapon("magic-repair0") {{
                        x = -8f;
                        y = -8f;
                        shootY = 2f;
                        mirror = true;
                        beamWidth = 1f;
                        repairSpeed = 3f;
                        bullet = new BulletType() {{
                            maxRange = 160f;
                        }};
                    }},
                    new RepairBeamWeapon("magic-repair0") {{
                        x = 6f;
                        y = 6f;
                        shootY = 2f;
                        mirror = true;
                        beamWidth = 1f;
                        repairSpeed = 3f;
                        bullet = new BulletType() {{
                            maxRange = 160f;
                        }};
                    }});
        }};
        Qingxiao = new UnitType("Qingxiao") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            lowAltitude = true;
            rotateMoveFirst = true;
            omniMovement = true;
            rotateSpeed = 3f;
            speed = 3.5f;
            drag = 0.02f;
            accel = 0.08f;
            hitSize = 48;
            health = 12000;
            armor = 24;
            itemCapacity = 0;
            engineOffset = 0;
            engineSize = 0f;
            trailLength = 12;
            range = 360;
            abilities.add(
                    new ForceFieldAbility(
                            96f,     // radius
                            1.2f,     // regen
                            1200f,    // max
                            180f,     // cooldown
                            4,        // sides
                            0f        // rotation
                    )
            );
            abilities.add(new EnergyFieldAbility(
                    60f,   // damage
                    120f,   // reload
                    180f   // range
            ) {{
                healPercent = 0.25f;
                x = 0f;
                y = 0f;
                maxTargets = 10;
                effectRadius = 1f;
                damageEffect = Fx.chainLightning;
                shootSound = MLSounds.spark;
                status = StatusEffects.shocked;
            }});
            weapons.add(
                    new Weapon("magic-Qingxiao0") {{
                        reload = 90f;
                        x = 0f;
                        y = 0f;
                        rotate = false;
                        mirror = false;
                        alternate = true;
                        inaccuracy = 0f;
                        shootSound = MLSounds.plasmadrop;
                        shake = 5f;
                        ignoreRotation = true;
                        bullet = new BasicBulletType(12, 80) {{
                            damage = 180f;
                            splashDamage = 120f;
                            splashDamageRadius = 48f;
                            lifetime = 30f;
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            trailLength = 16;
                            trailWidth = 8f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            hitSound = MLSounds.plasmaboom;
                            width = 64f;
                            height = 64f;
                            knockback = 8f;
                            despawnEffect = Fx.none;
                            spin = 6f;
                            sprite = "magic-十字星";
                            hitEffect = new WrapEffect() {{
                                effect = Fx.dynamicSpikes;
                                color = Color.valueOf("FEEBB3FF");
                                rotation = 48f;
                            }};
                        }};
                    }},
                    new Weapon("magic-Qingxiao1") {{
                        x = -12f;
                        y = -10f;
                        reload = 15f;
                        mirror = true;
                        alternate = true;
                        inaccuracy = 0f;
                        rotate = false;
                        shootSound = MLSounds.laser;
                        bullet = new LaserBulletType() {{
                            damage = 50f;
                            smokeEffect = Fx.bigShockwave;
                            colors = new Color[]{
                                    Color.valueOf("FEEBB3FF"),
                                    Color.valueOf("FEEBB3FF"),
                                    Color.valueOf("FEEBB3FF")
                            };
                            width = 16f;
                            length = 240f;
                        }};
                    }});
        }};
        //大型
        BrokenEdge = new UnitType("BrokenEdge") {{
            flying = true;
            faceTarget = true;
            lowAltitude = true;
            rotateMoveFirst = true;
            omniMovement = true;
            rotateSpeed = 2f;
            health = 40000f;
            armor = 40f;
            hitSize = 72f;
            range = 600f;
            speed = 2.4f;
            accel = 0.07f;
            drag = 0.03f;
            weapons.add(
                    new Weapon("magic-BrokenEdge0") {{
                        x = 20f;
                        y = 0f;
                        mirror = true;
                        reload = 3f;
                        shootSound = MLSounds.JG;
                        inaccuracy = 0f;
                        recoil = 2.5f;
                        rotate = true;
                        rotateSpeed = 5f;
                        shoot = new ShootAlternate() {{
                            barrels = 2;
                            spread = 2f;
                        }};
                        bullet = new BasicBulletType() {{
                            damage = 50f;
                            pierceCap = 2;
                            pierceBuilding = true;
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            width = 8f;
                            height = 16f;
                            trailLength = 2;
                            trailWidth = 2f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            pierce = true;
                            speed = 16f;
                            lifetime = 37.5f;
                            hitEffect = new WaveEffect() {{
                                lifetime = 8f;
                                sizeFrom = 0f;
                                sizeTo = 12f;
                                strokeFrom = 1f;
                                strokeTo = 0f;
                                colorFrom = Color.white;
                                colorTo = Color.valueOf("FEEBB3FF");
                            }};
                            despawnEffect = new WaveEffect() {{
                                lifetime = 8f;
                                sizeFrom = 0f;
                                sizeTo = 12f;
                                strokeFrom = 1f;
                                strokeTo = 0f;
                                colorFrom = Color.white;
                                colorTo = Color.valueOf("FEEBB3FF");
                            }};
                        }};
                    }}
            );

            weapons.add(
                    new Weapon("magic-BrokenEdge1") {{
                        mirror = true;
                        x = 0f;
                        y = 0f;
                        reload = 240f;
                        shoot = new ShootBarrel() {{
                            barrels = new float[]{
                                    24f, 8f, 5f,
                                    -24f, 8f, -5f,
                                    24f, 8f, 10f,
                                    -24f, 8f, -10f
                            };
                            shots = 20;
                            shotDelay = 2f;
                        }};
                        shootSound = MLSounds.missileLarge;
                        rotate = false;
                        controllable = false;
                        autoTarget = true;
                        bullet = new MissileBulletType() {{
                            height = 32f;
                            width = 16f;
                            sprite = "magic-导弹";
                            pierceBuilding = true;
                            speed = 12f;
                            drag = -0.02f;
                            lifetime = 50f;
                            homingDelay = 15f;
                            homingPower = 0.9f;
                            homingRange = 240f;
                            damage = 90f;
                            splashDamageRadius = 36f;
                            splashDamage = 120f;
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            trailLength = 9;
                            trailWidth = 3f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            trailRotation = true;
                            trailEffect = new ParticleEffect() {{
                                particles = 10;
                                sizeFrom = 15f;
                                sizeTo = 0f;
                                lifetime = 10f;
                                length = 5f;
                                baseLength = 0f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                                cone = 30f;
                            }};
                            hitSound = MLSounds.plasmaboom;
                            despawnEffect = Fx.none;
                            hitShake = 8f;
                            hitEffect = new MultiEffect(
                                    new WaveEffect() {{
                                        lifetime = 20f;
                                        sizeFrom = 0f;
                                        sizeTo = 36f;
                                        strokeFrom = 0f;
                                        strokeTo = 3f;
                                        colorFrom = Color.valueOf("FEEBB3FF");
                                        colorTo = Color.valueOf("FEEBB3FF");
                                    }},
                                    new ParticleEffect() {{
                                        particles = 9;
                                        sizeFrom = 3f;
                                        sizeTo = 0f;
                                        length = 36f;
                                        baseLength = 36f;
                                        lifetime = 30f;
                                        interp = Interp.pow10Out;
                                        sizeInterp = Interp.pow10In;
                                        colorFrom = Color.valueOf("FEEBB3FF");
                                        colorTo = Color.valueOf("FEEBB3FF");
                                    }},
                                    new ParticleEffect() {{
                                        particles = 9;
                                        line = true;
                                        strokeFrom = 3f;
                                        strokeTo = 0f;
                                        lenFrom = 6f;
                                        lenTo = 0f;
                                        length = 36f;
                                        baseLength = 36f;
                                        lifetime = 30f;
                                        interp = Interp.pow5Out;
                                        sizeInterp = Interp.pow5In;
                                        colorFrom = Color.valueOf("FEEBB3FF");
                                        colorTo = Color.valueOf("FEEBB3FF");
                                    }}
                            );
                        }};
                    }}
            );
        }};
        FlyingSwallow = new UnitType("FlyingSwallow") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            lowAltitude = true;
            speed = 5f;
            rotateSpeed = 5f;
            hitSize = 16;
            health = 1000;
            logicControllable = false;
            playerControllable = false;
            allowedInPayloads = false;
            engineOffset = 8;
            engineSize = 2;
            faceTarget = true;
            rotateMoveFirst = true;
            omniMovement = true;
            circleTarget = true;
            useUnitCap = false;
            circleTargetRadius = 250;
            armor = 10f;
            accel = 0.07f;
            drag = 0.03f;
            trailLength = 5;
            weapons.add(
                    new Weapon("magic-FlyingSwallow0") {{
                        x = 0f;
                        y = 0f;
                        mirror = true;
                        reload = 3f;
                        shootSound = MLSounds.JG;
                        inaccuracy = 0f;
                        recoil = 0f;
                        rotate = false;
                        shoot = new ShootAlternate() {{
                            barrels = 2;
                            spread = 2f;
                        }};
                        bullet = new BasicBulletType() {{
                            damage = 90f;
                            pierceCap = 2;
                            pierceBuilding = true;
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            width = 8f;
                            height = 16f;
                            trailLength = 2;
                            trailWidth = 2f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            pierce = true;
                            speed = 16f;
                            lifetime = 30f;
                            hitEffect = new WaveEffect() {{
                                lifetime = 8f;
                                sizeFrom = 0f;
                                sizeTo = 12f;
                                strokeFrom = 1f;
                                strokeTo = 0f;
                                colorFrom = Color.white;
                                colorTo = Color.valueOf("FEEBB3FF");
                            }};
                            despawnEffect = new WaveEffect() {{
                                lifetime = 8f;
                                sizeFrom = 0f;
                                sizeTo = 12f;
                                strokeFrom = 1f;
                                strokeTo = 0f;
                                colorFrom = Color.white;
                                colorTo = Color.valueOf("FEEBB3FF");
                            }};
                        }};
                    }});
        }};
            Tianshu = new UnitType("Tianshu") {{
                constructor = UnitTypes.flare.constructor;
                flying = true;
                faceTarget = true;
                lowAltitude = true;
                rotateMoveFirst = true;
                omniMovement = true;
                rotateSpeed = 1f;
                health = 25000f;
                armor = 30f;
                hitSize = 82f;
                range = 480f;
                speed = 1f;
                accel = 0.07f;
                drag = 0.03f;
                abilities.add(
                        new ForceFieldAbility(
                                240f,     // radius
                                1f,     // regen
                                1600f,    // max
                                360f,     // cooldown
                                5,        // sides
                                45f        // rotation
                        )
                );
                abilities.add(
                        new ForceFieldAbility(
                                240f,
                                1f,
                                1600f,
                                360f,
                                5,
                                0f
                        )
                );
                abilities.add(new EnergyFieldAbility(
                        120f,   // damage
                        90f,   // reload
                        360f   // range
                ) {{
                    healPercent = 1f;
                    x = 0f;
                    y = 1f;
                    maxTargets = 25;
                    effectRadius = 5f;
                    damageEffect = Fx.chainLightning;
                    shootSound = MLSounds.spark;
                    status = StatusEffects.shocked;
                }});
                abilities.add(new CarrierAbility(
                        MLUnitTypes.FlyingSwallow,      // 舰载机类型
                        10,                 // 最大舰载机数
                        480f,              // 索敌范围
                        60f               // 生成舰载机的间隔

                ));

                weapons.add(
                        new RepairBeamWeapon("magic-repair0") {{
                            x = 12f;
                            y = 0f;
                            shootY = 2f;
                            mirror = true;
                            beamWidth = 2.5f;
                            repairSpeed = 5f;
                            bullet = new BulletType() {{
                                maxRange = 240f;
                            }};
                        }},
                        new RepairBeamWeapon("magic-repair0") {{
                            x = 16f;
                            y = -16f;
                            shootY = 2f;
                            mirror = true;
                            beamWidth = 2.5f;
                            repairSpeed = 5f;
                            bullet = new BulletType() {{
                                maxRange = 240f;
                            }};
                        }});
            }};
            //旗舰
        //T1
        Aoba = new UnitType("Aoba"){{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            faceTarget = true;
            lowAltitude = true;
            rotateMoveFirst = true;
            omniMovement = true;
            rotateSpeed = 3f;
            health = 60000f;
            armor = 60f;
            hitSize = 96f;
            range = 960f;
            speed = 1.8f;
            accel = 0.06f;
            drag = 0.04f;
            weapons.add(
                    new Weapon("magic-Aoba0"){{
                        reload = 300f;
                        x = 0f;
                        y = -24f;
                        rotate = true;
                        rotateSpeed = 1.5f;
                        mirror = false;
                        alternate = true;
                        inaccuracy = 0f;
                        shootSound = MLSounds.shootForeshadow;
                        shake = 15f;
                        bullet = new BasicBulletType(){{
                            hitSize = 32f;
                            pierce = true;
                            pierceBuilding = true;
                            hittable = false;
                            reflectable = false;
                            damage = 1500f;
                            speed = 32f;
                            lifetime = 30f;
                            frontColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            sprite = "magic-透彻";
                            trailLength = 10;
                            trailWidth = 5f;
                            trailColor = Color.valueOf("FEEBB3FF");
                            hitSound = MLSounds.shootFuse;
                            width = 32f;
                            height = 64f;
                            hitEffect = Fx.colorSparkBig;
                            despawnEffect = Fx.colorSparkBig;
                        }};
                    }},
                    new Weapon("magic-Aoba1"){{
                        mirror = true;
                        x = 20f;
                        y = -44f;
                        reload = 90f;
                        shoot = new ShootAlternate(){{
                            shots = 15;
                            shotDelay = 3f;
                        }};
                        shootSound = MLSounds.missile;
                        rotate = true;
                        rotateSpeed = 3f;
                        inaccuracy = 30f;
                        controllable = false;
                        autoTarget = true;
                        alternate = false;
                        bullet = new MissileBulletType(){{
                            damage = 60f;
                            splashDamageRadius = 24f;
                            splashDamage = 60f;
                            homingRange = 960f;
                            homingPower = 0.3f;
                            homingDelay = 9f;
                            sprite = "magic-导弹";
                            trailLength = 6;
                            trailWidth = 3f;
                            trailEffect = Fx.none;
                            trailColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            frontColor = Color.valueOf("FEEBB3FF");
                            width = 16f;
                            height = 32f;
                            speed = 8f;
                            lifetime = 120f;
                            hitEffect = new WrapEffect(){{
                                effect = Fx.dynamicSpikes;
                                color = Color.valueOf("FEEBB3FF");
                                rotation = 24f;
                            }};
                        }};
                    }},
                    new Weapon("magic-Aoba1"){{
                        mirror = true;
                        x = 14f;
                        y = 16f;
                        reload = 90f;
                        shoot = new ShootAlternate(){{
                            shots = 15;
                            shotDelay = 3f;
                        }};
                        shootSound = MLSounds.missile;
                        rotate = true;
                        rotateSpeed = 3f;
                        inaccuracy = 30f;
                        controllable = false;
                        autoTarget = true;
                        alternate = false;
                        bullet = new MissileBulletType(){{
                            damage = 60f;
                            splashDamageRadius = 24f;
                            splashDamage = 60f;
                            homingRange = 960f;
                            homingPower = 0.3f;
                            homingDelay = 9f;
                            sprite = "magic-导弹";
                            trailLength = 6;
                            trailWidth = 3f;
                            trailEffect = Fx.none;
                            trailColor = Color.valueOf("FEEBB3FF");
                            backColor = Color.valueOf("FEEBB3FF");
                            frontColor = Color.valueOf("FEEBB3FF");
                            width = 16f;
                            height = 32f;
                            speed = 8f;
                            lifetime = 120f;
                            hitEffect = new WrapEffect(){{
                                effect = Fx.dynamicSpikes;
                                color = Color.valueOf("FEEBB3FF");
                                rotation = 24f;
                            }};
                        }};
                    }});
        }};
        //T2
        MoonRise = new UnitType("MoonRise"){{
                constructor = UnitTypes.flare.constructor;
                flying = true;
                faceTarget = true;
                lowAltitude = true;
                rotateMoveFirst = true;
                omniMovement = true;
                rotateSpeed = 2.5f;
                health = 180000f;
                armor = 90f;
                hitSize = 112f;
                range = 1200f;
                speed = 1.5f;
                accel = 0.06f;
                drag = 0.04f;
                abilities.add(new EnergyFieldAbility(
                        80f,   // damage
                        60f,   // reload
                        600f   // range
                ) {{
                    healPercent = 2f;
                    x = 0f;
                    y = 16f;
                    maxTargets = 40;
                    effectRadius = 8f;
                    damageEffect = Fx.chainLightning;
                    shootSound = MLSounds.spark;
                    status = StatusEffects.shocked;
                }});
            }};
    }
}