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

import static mindustry.Vars.*;

public class MLSpawnUnits {
    public static void load() {
        for (SpawnGroup group : state.rules.spawns) {
            if (group.type == MLUnitTypes.Starlight) {
                // 替换为你的自定义特效（可以是 MLFx.shrinkLightBeam）
                group.spawnEffect = MLFx.shrinkLightBeam;
            }
            // 其他单位继续添加...
        }
    }
}