package magical.content;

import arc.Events;
import mindustry.game.EventType.UnitSpawnEvent;
import mindustry.gen.Unit;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Timer;

public class MLSpawnUnits {
    public static void load() {
    ObjectMap<UnitType, Float> entryDelay = new ObjectMap<>();
    ObjectMap<UnitType, Effect> entryEffect = new ObjectMap<>();

    entryDelay.put(Starlight, 1f);
    entryEffect.put(Starlight, MLFx.shrinkLightBeam);
        Events.on(UnitCreateEvent.class, e -> {
            Unit unit = e.unit;
            // 只对波次单位生效
            if (e.spawner != null || unit.team != state.rules.waveTeam) return;
            Float delay = entryDelay.get(unit.type);
            if (delay == null) return;
            // 暂存单位当前位置
            float startX = unit.x, startY = unit.y;
            float originDelay = delay;
            // 让单位暂时不可见（将单位移到很远的地方，等时间到再移回）
            unit.set(-10000f, -10000f);   // 移出屏幕
            unit.vel.setZero();
            // 同时，在原位置播放一个“预警”特效（可选）
            warnFx.at(startX, startY);
            // 定时器：延迟后真正激活单位
            Timer.schedule(() -> {
                // 将单位移回原位置
                unit.set(startX, startY);
                // 播放入场特效
                Effect fx = entryEffect.get(unit.type);
                if (fx != null) fx.at(startX, startY);
                // 给单位一个初始速度或动作（可选）
                unit.vel.y = -2f;   // 稍微弹起
            }, delay);
        });
    }
}