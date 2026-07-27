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
        // 波次专属入场特效（不同单位不同特效）
       /* for (SpawnGroup group : state.rules.spawns) {
            if (group.type == MLUnitTypes.Starlight) {
                group.spawnEffect = MLFx.shrinkLightBeam;
            } else if (group.type == MLUnitTypes.另一个单位) {
                group.spawnEffect = Fx.spawnWave;
            }
        }*/
    }
}