package magical.content;

import arc.graphics.*;
import arc.math.Interp;
import mindustry.content.StatusEffects;
import mindustry.entities.effect.ParticleEffect;
import mindustry.type.*;

import static mindustry.content.Liquids.*;

public class MLLiquids {
    public static Liquid PhantomSteelSolution;

    public static void load(){

        PhantomSteelSolution = new Liquid("PhantomSteelSolution", Color.valueOf("97B5EDFF")){{
            heatCapacity = 0.4f;
            temperature = 0.8f;
            viscosity = 0.8f;
            flammability = 0.1f;
            effect = StatusEffects.burning;
        }};
    }
}
