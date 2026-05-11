package magical.content;

import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.graphics.g3d.PlanetGrid.*;
import mindustry.maps.planet.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLTechTree;
import magical.content.MLPlanets;

public class MLPlanets {

    public static Planet Cecilia = new Planet("塞西利亚", sun, 1f, 3.3f){{
        loadPlanetData = true;
        generator = new SerpuloPlanetGenerator();
        meshLoader = () -> new HexMesh(this, 6);
        cloudMeshLoader = () -> new MultiMesh(
                new HexSkyMesh(this, 2, 0.15f, 0.14f, 5, Color.valueOf("97B5EDFF").a(0.75f), 2, 0.42f, 1f, 0.43f),
                new HexSkyMesh(this, 3, 0.6f, 0.15f, 5, Color.valueOf("97B5EDFF").a(0.75f), 2, 0.42f, 1.2f, 0.45f),
        );

        launchCapacityMultiplier = 0f;
        sectorSeed = 1;
        allowWaves = true;
        allowLegacyLaunchPads = true;
        allowSectorInvasion = true;
        allowLaunchSchematics = true;
        enemyCoreSpawnReplace = true;
        allowLaunchLoadout = true;
        ruleSetter = r -> {
            r.waveTeam = Team.crux;
            r.placeRangeCheck = false;
            r.showSpawns = false;
            r.coreDestroyClear = true;
        };
        showRtsAIRule = true;
        iconColor = Color.valueOf("97B5EDFF");
        atmosphereColor = Color.valueOf("97B5EDFF");
        atmosphereRadIn = 0.02f;
        atmosphereRadOut = 0.3f;
        startSector = 1;
        alwaysUnlocked = true;
        allowSelfSectorLaunch = false;
        landCloudColor = Pal.spore.cpy().a(0.5f);

        sectorCaptureReplacements = ObjectMap.of(
                Blocks.metalTiles12, Blocks.metalTiles11,
                Blocks.metalTiles6, Blocks.metalTiles10
        );
    }};
    public static void load(){}
}