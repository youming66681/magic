package magical.content;

import arc.Events;
import arc.graphics.Color;
import arc.struct.ObjectMap;
import arc.util.Timer;
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
        // 为特定单位设置延迟和特效
        entryDelay.put(MLUnitTypes.Starlight, 1f);                // 延迟 1 秒
        entryEffect.put(MLUnitTypes.Starlight, MLFx.shrinkLightBeam);  // 你的自定义特效，若不存在可换为 Fx.spawn

        // 监听单位创建事件
        Events.on(UnitCreateEvent.class, e -> {
            Unit unit = e.unit;
            // 只处理由波次生成的单位（spawner 为 null 且队伍为波次敌队）
            if (e.spawner != null || unit.team != state.rules.waveTeam) return;

            Float delay = entryDelay.get(unit.type);
            if (delay == null) return;

            float startX = unit.x, startY = unit.y;
            float originDelay = delay;

            // 暂时将单位移出屏幕以实现隐藏
            unit.set(-10000f, -10000f);
            unit.vel.setZero();

            // 在原位置播放一个预警特效（渐大光环）
            Effect warnFx = new WaveEffect() {{
                lifetime = originDelay * 60f;
                sizeFrom = 10f;
                sizeTo = 40f;
                colorFrom = Color.valueOf("FFFFFF");
                colorTo = Color.valueOf("FFFFFF");
            }};
            warnFx.at(startX, startY);

            // 延迟后出现
            Timer.schedule(() -> {
                unit.set(startX, startY);
                Effect fx = entryEffect.get(unit.type);
                if (fx != null) fx.at(startX, startY);
                unit.vel.y = -2f;                 // 轻微弹起
            }, delay);
        });
    }
}