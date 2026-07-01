package magical.world.blocks.production;

import arc.struct.Seq;
import mindustry.world.blocks.production.GenericCrafter;

public class MultipleCrafter extends GenericCrafter{

    public final Seq<Formula> formulas = new Seq<>();

    public MultipleCrafter(String name){
        super(name);

        configurable = true;
        saveConfig = true;
        sync = true;

        config(Integer.class, (MultipleCrafterBuild build, Integer id) -> {
            build.setRecipe(id);
        });

        buildType = MultipleCrafterBuild::new;
    }

    public Formula addFormula(Formula formula){
        formulas.add(formula);
        return formula;
    }

    public Formula formula(int index){
        return formulas.get(index);
    }

    public int formulaCount(){
        return formulas.size;
    }
}