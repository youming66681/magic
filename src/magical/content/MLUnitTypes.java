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
            drizzle, Drizzle;

    public static void load(){
        drizzle = new UnitType("drizzle") {{
            constructor = UnitTypes.stell.constructor;
            omniMovement = false;
            rotateMoveFirst = false;
            squareShape = true;
            rotateSpeed = 4;
            speed = 2f;
            hitSize = 8f;

            health = 600;
            armor = 3;
            drag = 0.02f;
            accel = 0.12f;
            itemCapacity = 10;
            faceTarget = false;
            weapons.add(new Weapon("magic-drizzle1") {{
                reload = 120f;
                recoil = 3;
                x = y = 0;
                shootY = 2f;
                mirror = false;
                rotate = true;
                rotateSpeed = 3;
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
            hitSize = 16f;

            health = 1200;
            armor = 6;
            drag = 0.04f;
            accel = 0.1f;
            itemCapacity = 20;
            faceTarget = false;
            weapons.add(new Weapon("magic-Drizzle1") {{
                reload = 20f;
                recoil = 0;
                x = y = 0;
                shootY = 2f;
                mirror = false;
                rotate = true;
                rotateSpeed = 2;
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
                            progress = PartProgress.recoil;
                            moveY = -2;
                        }},
                        new RegionPart("-r") {{
                            mirror = false;
                            heatProgress = PartProgress.recoil;
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
    }
}