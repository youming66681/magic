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
import mindustry.type.ammo.*;
import mindustry.type.unit.*;
import mindustry.type.weapons.*;
import mindustry.world.meta.*;
import mindustry.type.UnitType;
import mindustry.content.UnitTypes;
import mindustry.content.Fx;

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
    drizzle, Drizzle, drizzlingRain,
    Breeze, SlantingWind, Gale;

    public static void load(){
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
                    rotateSpeed = 1.5f;
                    inaccuracy = 0f;
                    ejectEffect = Fx.casing3;
                    layerOffset = 0.001f;
                    shootSound = MLSounds.shootArtillery;
                    bullet = new BasicBulletType(12, 60) {{
                        lifetime = 20;
                        width = 12;
                        height = 24;
                        splashDamageRadius = 32;
                        splashDamage = 60;
                        hitEffect = despawnEffect = MLFx.Explosion3;
                        hitSound = MLSounds.explosion;
                    }};
                }});
            }};
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
            lowAltitude = true;
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
            autoDropBombs = true;
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
    }
}