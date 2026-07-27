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
        // 监听 UnitSpawnEvent —— 仅当波次生成单位时触发
        Events.on(UnitSpawnEvent.class, e -> {
                    if (e.unit == null || e.unit.type == null) return;
                    // 为不同单位播放不同特效
                    if (e.unit.type == MLUnitTypes.Starlight) {
                        // 替换为你的自定义特效（若不存在，先用 Fx.spawnWave 测试）
                        MLFx.shrinkLightBeam.at(e.unit.x, e.unit.y);
                    }
                });
    }
}