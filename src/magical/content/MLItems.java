package magical.content;

import arc.graphics.*;
//import javafx.scene.paint.Color;
import mindustry.type.*;

import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLTechTree;
import magical.content.MLPlanets;

public class MLItems {
    public static Item
     acrylic, mysticCrystal, phantomSteel, phantomTitaniumSteel, wingedStone, mysticSteel,
     nanoEnergyThread, crystallineCarbon, fluorescentFeatherStone, wingedMetal,
     phantomLuminousAlloy, nanoCarbonAlloy, logicChip, arrayChip, matrixChip;

    public static void load() {
        acrylic = new Item("acrylic", Color.valueOf("404040FF")) {{
            hardness = 1;
            cost = 1.0F;
        }};

        mysticCrystal = new Item("mysticCrystal", Color.valueOf("46649AFF")) {{
            description = "A peculiar crystal";
            hardness = 2;
            cost = 1.0F;
        }};

        phantomSteel = new Item("phantomSteel", Color.valueOf("97B5EDFF")) {{
            description = "Used for the beginning of constructing the illusion industry";
            hardness = 3;
            cost = 1.0F;
        }};

        phantomTitaniumSteel = new Item("phantomTitaniumSteel", Color.valueOf("46649AFF")) {{
            description = "High-strength synthetic material";
            hardness = 4;
            cost = 1.0F;
        }};

        wingedStone = new Item("wingedStone", Color.valueOf("9C88C3FF")) {{
            hardness = 4;
            explosiveness = 0.1F;
            cost = 1.0F;
            charge = 2.0F;
        }};

        mysticSteel = new Item("mysticSteel", Color.valueOf("7595D2FF")) {{
            description = "Relatively hot to the touch";
            hardness = 5;
            cost = 1.0F;
            flammability = 0.1F;
        }};

        nanoEnergyThread = new Item("nanoEnergyThread", Color.valueOf("CCCEDBFF")) {{
            description = "nano-energy-thread";
            hardness = 5;
            cost = 1.0F;
            radioactivity = 2.6F;
            explosiveness = 5.0F;
            charge = 10.0F;
        }};

        crystallineCarbon = new Item("crystallineCarbon", Color.valueOf("8AA3F4FF")) {{
            hardness = 6;
            cost = 1.0F;
            flammability = 5.0F;
            charge = 2.5F;
        }};

        fluorescentFeatherStone = new Item("fluorescentFeatherStone", Color.valueOf("CCCEDBFF")) {{
            description = "A material that is entirely fluorescent white, containing a large amount of energy";
            hardness = 6;
            cost = 1.0F;
            radioactivity = 2.0F;
            explosiveness = 5.5F;
            charge = 10.0F;
        }};

        wingedMetal = new Item("wingedMetal", Color.valueOf("8175A5FF")) {{
            description = "Tungsten carbide neutralized some of the easily discharged properties, turning it into a more stable type of metal.";
            hardness = 7;
            cost = 1.0F;
            charge = 1F;
        }};

        phantomLuminousAlloy = new Item("phantomLuminousAlloy", Color.valueOf("6569C9FF")) {{
            description = "A high-strength alloy";
            hardness = 8;
            cost = 1.0F;
        }};

        nanoCarbonAlloy = new Item("nanoCarbonAlloy", Color.valueOf("D1EFFFFF")) {{
            description = "The hardest material";
            hardness = 20;
            cost = 1.0F;
            radioactivity = 2.6F;
            explosiveness = 6.0F;
            flammability = 1.0F;
            charge = 12.5F;
        }};
        logicChip = new Item("logicChip", Color.valueOf("97B5EDFF")) {{
            description = "";
            hardness = 1;
            cost = 1.0F;
            frames = 6;
            frameTime = 6;
        }};
        arrayChip = new Item("arrayChip", Color.valueOf("9C88C3FF")) {{
            description = "";
            hardness = 2;
            cost = 1.0F;
            frames = 6;
            frameTime = 6;
        }};
        matrixChip = new Item("matrixChip", Color.valueOf("6569C9FF")) {{
            description = "";
            hardness = 3;
            cost = 1.0F;
            frames = 6;
            frameTime = 6;
        }};
    }
}