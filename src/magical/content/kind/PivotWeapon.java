package magical.content;

import arc.graphics.g2d.Draw;
import arc.math.geom.Vec2;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.type.Weapon;

public class PivotWeapon extends Weapon{

    /** 自定义旋转中心 */
    public float pivotX = 0f;
    public float pivotY = 0f;

    private final Vec2 v = new Vec2();

    public PivotWeapon(String name){
        super(name);
    }

    @Override
    public void draw(Unit unit, WeaponMount mount){

        // 先调用原版计算
        float rotation = unit.rotation - 90f;
        float weaponRotation = rotation + mount.rotation;

        // 武器安装点
        v.trns(rotation, x, y);

        float wx = unit.x + v.x;
        float wy = unit.y + v.y;

        // 旋转中心补偿
        v.trns(weaponRotation, -pivotX, -pivotY);

        Tmp.v1.trns(weaponRotation, -pivotX, -pivotY);

        Draw.rect(
                region,
                wx + Tmp.v1.x,
                wy + Tmp.v1.y,
                region.width * Draw.scl,
                region.height * Draw.scl,
                weaponRotation
        );
    }
}