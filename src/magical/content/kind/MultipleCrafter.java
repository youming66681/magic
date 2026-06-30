package magical.content.kind;

import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.world.Block;

public class MultipleCrafter extends Block {

    public FormulaStack formulas = new FormulaStack();

    public MultipleCrafter(String name){
        super(name);
    }

    @Override
    public Building createBuilding(){
        return new MultipleCrafterBuild();
    }

    public class MultipleCrafterBuild extends Building {

        public int formulaIndex = 0;
        public float progress = 0f;

        public Formula current(){

            if(formulas == null || formulas.size() == 0){
                return null;
            }

            // clamp index
            formulaIndex = Math.max(0, Math.min(formulaIndex, formulas.size() - 1));

            Formula f = formulas.get(formulaIndex);

            return (f != null && f.valid()) ? f : formulas.get(0);
        }

        @Override
        public void buildConfiguration(Table table){

            table.clear();

            if(formulas == null || formulas.size() == 0){
                table.label("No formulas").row();
                return;
            }

            for(int i = 0; i < formulas.size(); i++){
                int id = i;
                Formula f = formulas.get(i);

                String name = (f == null || f.name == null) ? "null" : f.name;

                table.button(name, () -> {
                    formulaIndex = id;
                }).width(140).row();
            }
        }

        @Override
        public void updateTile(){

            Formula f = current();

            if(f == null){
                progress = 0f;
                return;
            }

            if(f.craftTime <= 0){
                f.craftTime = 60f;
            }

            progress += edelta() / f.craftTime;

            if(progress >= 1f){
                craftSafe(f);
                progress = 0f;
            }
        }

        private void craftSafe(Formula f){

            if(f == null) return;

            if(f.outputs != null){
                for(int i = 0; i < f.outputs.size; i++){
                    if(f.outputs.get(i) != null){
                        offload(f.outputs.get(i).item, f.outputs.get(i).amount);
                    }
                }
            }
        }

        @Override
        public Object config(){
            return formulaIndex;
        }

        @Override
        public void write(mindustry.io.Writes write){
            super.write(write);
            write.i(formulaIndex);
        }

        @Override
        public void read(mindustry.io.Reads read, byte revision){
            super.read(read, revision);
            formulaIndex = read.i();
        }
    }
}