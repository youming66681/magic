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
            Drizzle;

    public static void load(){
        Drizzle = new UnitType("drizzle") {{
            constructor = UnitTypes.stell.constructor;
            omniMovement = false;
            rotateMoveFirst = false;
            rotateSpeed = 3;
            speed = 1.5f;
            hitSize = 8f;

            health = 600;
            armor = 3;
            drag = 0.02f;
            accel = 0.12f;
            itemCapacity = 10;
            faceTarget = false;
            weapons.add(new Weapon("drizzle-1") {{
                reload = 120f;
                recoil = 3;
                x = y = 0;
                shootY = 5.5f;
                mirror = false;
                rotate = true;
                rotateSpeed = 5;
                inaccuracy = 1f;
                ejectEffect = Fx.casing1;
                shootSound = MLSounds.shootArtillery;
                shoot.shots = 6;
                shoot.shotDelay = 6;
                bullet = new BasicBulletType(8, 25) {{
                    lifetime = 20;
                    width = 8;
                    height = 16;
                    splashDamageRadius = 24;
                    splashDamage = 25;
                    hitEffect = despawnEffect = MLFx.smallExplosion2;
                }};
            }});
        }};
    }
}