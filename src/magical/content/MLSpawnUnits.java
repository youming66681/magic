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

public class MLSpawnUnits {
    public static void load() {
        Events.on(UnitSpawnEvent.class, e -> {
            Unit unit = e.unit;
            if (unit == null || unit.type == null) return;
            // 为不同的单位设置不同的延迟和特效
            if (unit.type == MLUnitTypes.Starlight) {
                float delay = 1f;
                float x = unit.x + Mathf.random(-240f, 240f);
                float y = unit.y + Mathf.random(-240f, 240f);
                UnitType type = unit.type;
                MLFx.smallTeleport.at(x, y);
                // 立即移除原始单位，避免它短暂出现
                unit.remove();
                // 延迟后重新创建单位
                Time.run(delay * 35f, () -> {
                    Unit newUnit = type.create(state.rules.waveTeam);
                    newUnit.set(x, y);
                    newUnit.add();
                });
            }
                else if (unit.type == MLUnitTypes.Qingxiao) {
                    float delay = 1f ;
                    float x = unit.x + Mathf.random(-240f, 240f);
                    float y = unit.y + Mathf.random(-240f, 240f);
                    UnitType type = unit.type;
                    MLFx.smallTeleport.at(x, y);
                    unit.remove();
                    Time.run(delay * 35f, () -> {
                        Unit newUnit = type.create(state.rules.waveTeam);
                        newUnit.set(x, y);
                        newUnit.add();
                    });
                }
        });
    }
}