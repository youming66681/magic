package magical.content;

import arc.Events;
import arc.graphics.Color;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.effect.WaveEffect;
import mindustry.game.EventType.UnitCreateEvent;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.game.SpawnGroup;
import mindustry.game.EventType.*;

import static mindustry.Vars.*;

//by youming

public class MLSpawnUnits {
    public static void load() {
        Events.on(UnitSpawnEvent.class, e -> {
            Unit unit = e.unit;
            if (unit == null || unit.type == null) return;
            if (unit.type == MLUnitTypes.Starlight) {
                float delay = 1f;                // 单位延迟出现的时间（秒）
                float x = unit.x, y = unit.y;
                UnitType type = unit.type;
                // 延迟一帧移除原单位（可改为 unit.remove() 立即移除）
                Time.run(1f, () -> {
                    if (unit.isAdded()) {
                        unit.remove();
                    }
                });
                // 立即播放入场特效
                MLFx.shrinkLightBeam.at(x, y);
                // 延迟后重新创建单位
                Time.run(delay * 60f, () -> {
                    Unit newUnit = type.create(state.rules.waveTeam);
                    newUnit.set(x, y);
                    newUnit.add();
                });
            }
        });
    }
}