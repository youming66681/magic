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
import arc.math.Mathf;

import static mindustry.Vars.*;

//by youming

public class MLSpawnUnits {
    public static void load() {
        Events.on(UnitSpawnEvent.class, e -> {
            Unit unit = e.unit;
            if (unit == null || unit.type == null) return;
            // 为不同的单位设置不同的延迟和特效
            if (unit.type == MLUnitTypes.Starlight) {
                float delay = 1f + Mathf.random(0f, 2f);
                float x = unit.x + Mathf.random(-8f, 8f);
                float y = unit.y + Mathf.random(-8f, 8f);
                UnitType type = unit.type;
                // 立即移除原始单位，避免它短暂出现
                unit.remove();
                // 延迟后重新创建单位
                Time.run(delay * 30f, () -> {
                    // 使用波次队伍创建单位，确保攻击正确
                    Unit newUnit = type.create(state.rules.waveTeam);
                    newUnit.set(x, y);
                    newUnit.add();
                    MLFx.shrinkLightBeam.at(x, y);
                });
            }
        });
    }
}