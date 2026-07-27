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
        Log.info("[MLSpawnUnits] load() called and listener registered.");

        Events.on(UnitCreateEvent.class, e -> {
            Unit unit = e.unit;
            Log.info("[MLSpawnUnits] UnitCreateEvent: type=@, team=@, spawnedByCore=@, spawner=@, isPlayerTeam=@",
                    unit.type,
                    unit.team,
                    unit.spawnedByCore,
                    e.spawner,
                    unit.team == player.team());

            // 只记录波次敌队（非玩家、非核心）的情况
            if (e.spawner == null && !unit.spawnedByCore && unit.team != player.team()) {
                Log.info("[MLSpawnUnits] Wave unit detected, playing test effect at @, @", unit.x, unit.y);
                // 播放一个简单的白圈特效作为测试
                new WaveEffect() {{
                    lifetime = 40f;
                    sizeFrom = 10f;
                    sizeTo = 30f;
                    colorFrom = Color.white;
                    colorTo = Color.white;
                }}.at(unit.x, unit.y);
            }
        });
    }
}