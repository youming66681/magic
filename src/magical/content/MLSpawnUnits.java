package magical.content;

import arc.Events;
import mindustry.game.EventType.UnitSpawnEvent;
import mindustry.gen.Unit;

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