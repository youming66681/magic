package magical.content;

import mindustry.maps.*;
import mindustry.type.*;

import static mindustry.content.Planets.*;

public class MLSectorPresets{
    public static SectorPreset
            LandingZone;

    public static void load(){

        LandingZone = new SectorPreset("LandingZone", MLPlanets.cecilia, 0){{
            alwaysUnlocked = true;
            captureWave = 10;
            difficulty = 1;
        }};

       /* planetaryTerminal = new SectorPreset("planetaryTerminal", serpulo, 93){{
            difficulty = 10;
            captureWave = 78;
        }};*/
    }
}
