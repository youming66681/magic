package magical.content;

import arc.Events;
import mindustry.game.EventType.UnitSpawnEvent;
import mindustry.gen.Unit;
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

public class MLSpawnUnits {
    public static SpawnUnitType StarlightSpawn;
    public static void load(){
        StarlightSpawn = new SpawnUnitType("StarlightSpawn"){{
        weapons.add(new Weapon() {{
                alwaysShooting = shootOnDeath = true;
                mirror = false;
                controllable = aiControllable = false;
                x = shootY = 0f;
                shootSound = MLSounds.plasmadrop;
                bullet = new BasicBulletType() {{
                        width = height = shrinkY = 0f;
                        killShooter = ignoreSpawnAngle = true;
                        collides = absorbable = hittable = keepVelocity = false;
                        speed = damage = 0f;
                        lifetime = 90f;
                        hitSound = despawnSound = MLSounds.plasmaboom;
                        hitSoundVolume = 1f;
                        hitShake = 10f;
                        shootEffect = smokeEffect = hitEffect = despawnEffect = Fx.none;
                        parts.addAll(
                                new EffectSpawnerPart(){{
                                    x = 0;
                                    y = 0;
                                    interval = 90f;
                                    effect = MLFx.teleportEnter;
                                }}
                          );
                        despawnUnit = MLUnitTypes.Starlight;
                        despawnUnitRadius = 0f;
                    }};
            }});
    }};
    }
}