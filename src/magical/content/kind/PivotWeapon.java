package magical.type;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.geom.Vec2;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.type.Weapon;

public class PivotWeapon extends Weapon{

    /** 旋转中心偏移（相对于贴图中心） */
    public float pivotX = 0f;
    public float pivotY = 0f;

    private final Vec2 vec = new Vec2();

    public PivotWeapon(String name){
        super(name);
    }

    @Override
    public void draw(Unit unit, WeaponMount mount){

        TextureRegion region = this.region;

        float rotation = unit.rotation - 90f;
        float weaponRotation = rotation + mount.rotation;

        // 武器安装点
        vec.trns(rotation, x, y);
        float wx = unit.x + vec.x;
        float wy = unit.y + vec.y;

        // 根据旋转中心修正
        vec.trns(weaponRotation, -pivotX, -pivotY);

        Draw.rect(
                region,
                wx + vec.x,
                wy + vec.y,
                region.width * Draw.scl,
                region.height * Draw.scl,
                weaponRotation
        );
    }
}