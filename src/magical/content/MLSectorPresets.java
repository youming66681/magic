package magical.content;

import mindustry.maps.*;
import mindustry.type.*;

import static mindustry.content.Planets.*;

public class MLSectorPresets{
    public static SectorPreset
            LandingZone, DeepSecludedJungle, FrozenWinterValley;

    public static void load(){

        LandingZone = new SectorPreset("LandingZone", MLPlanets.cecilia, 1){{
            alwaysUnlocked = true;
            captureWave = 10;
            difficulty = 1;
        }};

        DeepSecludedJungle = new SectorPreset("DeepSecludedJungle", MLPlanets.cecilia, 5){{
            difficulty = 3;
            captureWave = 45;
        }};

        FrozenWinterValley = new SectorPreset("FrozenWinterValley", MLPlanets.cecilia, 10){{
            difficulty = 5;
            captureWave = 45;
        }};
    }
}
