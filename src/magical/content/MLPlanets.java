package magical.content;

import mindustry.content.Planets;
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
import mindustry.world.blocks.environment.*;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLTechTree;

public class MLPlanets {
    public static Planet cecilia;

    public static void load() {

         cecilia = new Planet("cecilia", Planets.sun, 1f, 3) {{
            loadPlanetData = true;
            generator = new SerpuloPlanetGenerator();
            meshLoader = () -> new HexMesh(this, 6);
            cloudMeshLoader = () -> new MultiMesh(
                    new HexSkyMesh(this, 2, 0.15f, 0.14f, 5, Color.valueOf("97B5EDFF").a(0.75f), 2, 0.42f, 1f, 0.43f),
                    new HexSkyMesh(this, 3, 0.6f, 0.15f, 5, Color.valueOf("97B5EDFF").a(0.75f), 2, 0.42f, 1.2f, 0.45f)
            );

            launchCapacityMultiplier = 0f;
            sectorSeed = 1;
            bloom = false;
            prebuildBase = false;
            tidalLock = false;
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
            orbitRadius = 40;
            rotateTime = 3000;
            startSector = 1;
            alwaysUnlocked = true;
            allowSelfSectorLaunch = false;
            landCloudColor = Pal.spore.cpy().a(0.5f);
            addOreSpawn(MLBlocks.phantomSteelOre, 21f, 0.8f, 4);
        }};
    }
}