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

public class MLSpawnUnits {
    public static void load() {
        Events.on(UnitSpawnEvent.class, e -> {
            Unit unit = e.unit;
            if (unit == null || unit.type == null) return;
            // 为不同单位设置不同的延迟和特效
            if (unit.type == MLUnitTypes.Starlight) {
                // 延迟30帧（0.5秒）后播放光束特效
                Time.run(30f, () -> {
                    // 在单位当前位置播放特效
                    if (unit.isAdded()) {
                        MLFx.shrinkLightBeam.at(unit.x, unit.y);
                    }
                });
            }
            // 添加更多单位...
        });
    }
}