package mindustry.content;

import mindustry.maps.*;
import mindustry.type.*;

import static mindustry.content.Planets.*;

public class SectorPresets{
    public static SectorPreset
            LandingZone,

    public static void load(){

        LandingZone = new SectorPreset("LandingZone", cecilia, 0){{
            alwaysUnlocked = true;
            addStartingItems = true;
            captureWave = 10;
            difficulty = 1;
            overrideLaunchDefaults = true;
            noLighting = true;
            isLastSector = true;
            startWaveTimeMultiplier = 2f;
        }};

       /* planetaryTerminal = new SectorPreset("planetaryTerminal", serpulo, 93){{
            difficulty = 10;
            captureWave = 78;
            isLastSector = true;
        }};*/
    }
}
