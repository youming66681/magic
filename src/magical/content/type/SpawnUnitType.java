package magical.content;

import mindustry.type.UnitType;
import mindustry.world.meta.Env;
import mindustry.world.meta.Stat;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.gen.UnitEntity;
import arc.graphics.g2d.Draw;
import mindustry.content.Fx;

public class SpawnUnitType extends UnitType {
    public SpawnUnitType(String name) {
        super(name);
        health = 10000;
        hitSize = drownTimeMultiplier = -1f;
        reload = rotateSpeed = speed = fogRadius = lightRadius = range = buildSpeed = dpsEstimate = 0f;
        flying = lowAltitude = hidden = true;
        targetable = isEnemy = canDrown = alwaysUnlocked = physics = wobble = hittable = drawCell = drawShields = drawMinimap = useUnitCap = playerControllable = logicControllable = false;
        createScorch = createWreck = false;
        deathSound = wreckSound = Sounds.none;
        deathExplosionEffect = Fx.none;
        envDisabled = Env.space;
        flyingLayer = 0f;
        engineSize = 0f;
        constructor = UnitEntity::create;
    }

    @Override
    public void setStats() {
        stats.add(Stat.health, health);
    }
    
    @Override
    public void drawBody(Unit unit) {
        super.drawBody(unit);
        Draw.alpha(0f);
    }

    @Override
    public void drawWeapons(Unit unit) {
        super.drawWeapons(unit);
        Draw.alpha(0f);
    }

    @Override
    public void drawWeaponOutlines(Unit unit) {
        super.drawWeaponOutlines(unit);
        Draw.alpha(0f);
    }

    @Override
    public void drawEngines(Unit unit) {
        super.drawEngines(unit);
        Draw.alpha(0f);
    }

    @Override
    public void applyColor(Unit unit) {
        super.applyColor(unit);
        Draw.alpha(0f);
    }

    @Override
    public void applyOutlineColor(Unit unit) {
        super.applyOutlineColor(unit);
        Draw.alpha(0f);
    }

    @Override
    public void drawSoftShadow(Unit unit) {
        super.drawSoftShadow(unit);
        Draw.alpha(0f);
    }
}
