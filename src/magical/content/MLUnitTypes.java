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
    drizzle, Drizzle, drizzlingRain,
    //二级
    war, BeaconFire, War,
    //空
    //一级
    Breeze, SlantingWind, Gale,
    //二级
    BlazingFire, glow, blazing,
    //海
    //一级
    StillWater, ripple, Turbulence,
    //核心机
    Popular;

    public static void load(){
        //幻境陆军
        //一级
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
            drag = 0.02f;
            accel = 0.12f;
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
            drag = 0.04f;
            accel = 0.1f;
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
                shoot = new ShootAlternate() {{
                    barrels = 2;
                    spread = 2;
                }};
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
                drag = 0.06f;
                accel = 0.08f;
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
            //二级
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
            weapons.add(new Weapon("magic-war0"){{
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
                bullet = new BasicBulletType(8f, 20f){{
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
            range = 176;
            abilities.add(new ShieldArcAbility(){{
                whenShooting = true;
                radius = 12f;
                width = 6f;
                max = 500f;
                regen = 0.5f;
                cooldown = 360f;
                angle = 180f;
                angleOffset = 0f;
            }});
            weapons.add(new Weapon("magic-BeaconFire0"){{
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
                bullet = new ContinuousLaserBulletType(250f){{
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
                immunities.addAll(
                        StatusEffects.burning,
                        StatusEffects.melting
                );
                abilities.add(new ShieldArcAbility(){{
                    whenShooting = true;
                    radius = 24f;
                    width = 6f;
                    max = 100f;
                    regen = 1f;
                    cooldown = 180f;
                    angle = 120f;
                    angleOffset = 0f;
                }});
                weapons.add(new Weapon("magic-War0"){{
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
                        shoot = new ShootAlternate(){{
                            shots = 9;
                            shotDelay = 3f;
                            barrels = 1;
                            spread = 0f;
                        }};
                        bullet = new BasicBulletType(8f, 90f){
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
                            }};
                }});
            }};
        //幻境空军
        //一级
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
        Gale = new UnitType("Gale") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            lowAltitude = false;
            circleTarget = true;
            faceTarget = true;
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
            weapons.add(new Weapon("magic-Gale1") {{
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
        //二级
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
            weapon = new Weapon("magic-BlazingFire0"){{
                x = 0f;
                reload = 60f;
                mirror = false;
                rotate = true;
                shootSound = MLSounds.explosion;
                bullet = new BasicBulletType(0f, 160f){{
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
                            new WaveEffect(){{
                                lifetime = 15f;
                                sizeFrom = 0f;
                                sizeTo = 64f;
                                strokeFrom = 0f;
                                strokeTo = 5f;
                                colorFrom = Color.valueOf("FEEBB3FF");
                                colorTo = Color.valueOf("FEEBB3FF");
                            }},
                            new ParticleEffect(){{
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
                            new ParticleEffect(){{
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
            }};
        }};
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
            weapon = new Weapon("magic-BlazingFire0"){{
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
                bullet = new BasicBulletType(8f, 50f){{
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
                    ){{
                        rotation = 16f;
                    }};
                    status = StatusEffects.slow;
                    statusDuration = 240f;
                    makeFire = true;
                    incendAmount = 4;
                    incendSpread = 8f;
                    incendChance = 0.5f;
                }};
            }};
        }};
        blazing = new UnitType("blazing") {{
            constructor = UnitTypes.flare.constructor;
            flying = true;
            circleTarget = true;
            faceTarget = true;
            lowAltitude = true;
            rotateSpeed = 6.75f;
            speed = 4.5f;
            drag = 0.03f;
            accel = 0.08f;
            hitSize = 16;
            health = 2880;
            armor = 8;
            engineOffset = 16;
            engineSize = 8f;
            range = 320;
            trailLength = 9;
            abilities.add(new ShieldArcAbility(){{
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
            weapon = new Weapon("magic-blazing0"){{
            x = -6f;
            y = 0f;
            mirror = true;
            reload = 6f;
            rotate = false;
            recoil = 3f;
            inaccuracy = 3f;
            shootSound = MLSounds.JG;
            bullet = new BasicBulletType(16f, 30f){{
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
                hitEffect = new WaveEffect(){{
                    lifetime = 8f;
                    sizeFrom = 0f;
                    sizeTo = 8f;
                    strokeFrom = 0f;
                    strokeTo = 1f;
                    colorFrom = Color.white;
                    colorTo = Color.valueOf("FEEBB3FF");
                }};
                despawnEffect = new WaveEffect(){{
                    lifetime = 8f;
                    sizeFrom = 0f;
                    sizeTo = 8f;
                    strokeFrom = 0f;
                    strokeTo = 1f;
                    colorFrom = Color.white;
                    colorTo = Color.valueOf("FEEBB3FF");
                }};
            }};
        }};
         weapon = new Weapon("magic-blazing1"){{
                    x = 0f;
                    y = 0f;
                    reload = 300f;
                    mirror = false;
                    alternate = false;
                    shootSound = MLSounds.missile;
                    shoot = new ShootBarrel(){{
                        shots = 2;
                        shotDelay = 0f;
                        barrels = new float[]{
                                10f,0f,0f,
                                -10f,0f,0f
                        };
                    }};
                    inaccuracy = 0f;
                    shootCone = 180f;
                    bullet = new MissileBulletType(8f,80f){{
                        hitSound = MLSounds.plasmaboom;
                        shrinkY = 0f;
                        homingRange = 180f;
                        homingPower = 0.06f;
                        splashDamageRadius = 24f;
                        splashDamage = 160f;
                        makeFire = true;
                        incendAmount = 4;
                        incendSpread = 8f;
                        incendChance = 0.5f;
                        hitEffect = new MultiEffect(
                                new WaveEffect(){{
                                    lifetime = 25f;
                                    sizeFrom = 0f;
                                    sizeTo = 48f;
                                    strokeFrom = 0f;
                                    strokeTo = 2f;
                                    colorFrom = Color.valueOf("FEEBB3FF");
                                    colorTo = Color.valueOf("FEEBB3FF");
                                }},
                                new ParticleEffect(){{
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
                                new ParticleEffect(){{
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
                }};
        }};
        //幻境海军
        //一级
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
    }
}