package magical.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import arc.struct.GridBits;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Structs;
import arc.util.Tmp;
import arc.util.noise.Ridged;
import arc.util.noise.Simplex;
import mindustry.Vars;
import mindustry.ai.Astar;
import mindustry.content.Blocks;
import mindustry.content.Liquids;
import mindustry.game.Rules;
import mindustry.game.Schematics;
import mindustry.game.Team;
import mindustry.graphics.Pal;
import mindustry.graphics.g3d.GenericMesh;
import mindustry.graphics.g3d.HexSkyMesh;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.type.Weather;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.TileGen;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.SteamVent;
import mindustry.world.blocks.environment.TallBlock;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.meta.Attribute;

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
                new HexSkyMesh(this, 3, 0.6f, 0.15f, 5, Color.valueOf("97B5EDFF").a(0.75f), 2, 0.42f, 1.2f, 0.45f)
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