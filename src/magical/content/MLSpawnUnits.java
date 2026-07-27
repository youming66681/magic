package magical.content;

import arc.Events;
import arc.graphics.Color;
import arc.struct.ObjectMap;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.effect.WaveEffect;
import mindustry.game.EventType.UnitCreateEvent;
import mindustry.gen.Unit;
import mindustry.type.UnitType;

import static mindustry.Vars.*;

public class MLSpawnUnits {
    // 延迟时间映射 (单位类型 → 延迟秒数)
    public static ObjectMap<UnitType, Float> entryDelay = new ObjectMap<>();
    // 入场特效映射 (单位类型 → 特效)
    public static ObjectMap<UnitType, Effect> entryEffect = new ObjectMap<>();

    public static void load() {
        // 设置需要特殊入场的单位及其延迟和特效
        entryDelay.put(MLUnitTypes.Starlight, 1f);
        entryEffect.put(MLUnitTypes.Starlight, MLFx.shrinkLightBeam); // 若特效不存在请改为 Fx.spawn

        // 监听单位创建事件
        Events.on(UnitCreateEvent.class, e -> {
            Unit unit = e.unit;
            // 只处理波次生成的单位（没有 spawner，不是玩家队伍，不是核心产出）
            if (e.spawner != null || unit.team == player.team() || unit.spawnedByCore) return;

            Float delay = entryDelay.get(unit.type);
            if (delay == null) return;

            float startX = unit.x, startY = unit.y;
            UnitType type = unit.type;

            // 移除原始单位，防止它立即出现
            unit.remove();

            // 播放入场特效（可覆盖整个等待时间）
            Effect fx = entryEffect.get(type);
            if (fx != null) {
                fx.at(startX, startY);
            } else {
                // 如果没有定义特效，播放一个默认的预警光环
                new WaveEffect() {{
                    lifetime = delay * 60f;
                    sizeFrom = 10f; sizeTo = 40f;
                    colorFrom = Color.valueOf("FFFFFF");
                    colorTo = Color.valueOf("FFFFFF");
                }}.at(startX, startY);
            }

            // 延迟后重新创建单位
            Time.run(delay * 60f, () -> {
                Unit newUnit = type.create(unit.team);
                newUnit.set(startX, startY);
                newUnit.add();
                newUnit.vel.y = -2f; // 轻微弹跳
            });
        });
    }
}