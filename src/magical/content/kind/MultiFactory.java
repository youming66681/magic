package magical.content;

import arc.struct.Seq;
import mindustry.world.Block;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;

public class MultiFactory extends Block {

    public Seq<Recipe> recipes = new Seq<>();

    public float craftTime = 60f;

    public MultiFactory(String name){
        super(name);
        hasItems = true;
        update = true;
        solid = true;
    }

    // 添加配方
    public void addRecipe(Recipe r){
        recipes.add(r);
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);
        stats.add(Stat.output, "Multiple Recipes Supported");
    }

    public static class Recipe{
        public ItemStack[] input;
        public ItemStack[] output;
        public float time;

        public Recipe(ItemStack[] input, ItemStack[] output, float time){
            this.input = input;
            this.output = output;
            this.time = time;
        }
    }

    public class MultiFactoryBuild extends Building {

        public int selected = 0;
        public float progress = 0f;

        public Recipe current(){
            return recipes.get(Math.min(selected, recipes.size - 1));
        }

        @Override
        public void updateTile(){

            if(recipes.size == 0) return;

            Recipe r = current();

            if(consValid(r.input)){
                progress += edelta();

                if(progress >= r.time){
                    consumeItems(r.input);
                    for(ItemStack out : r.output){
                        offload(out.item, out.amount);
                    }
                    progress = 0f;
                }
            }else{
                progress = 0f;
            }
        }


        public boolean consValid(ItemStack[] req){
            for(ItemStack s : req){
                if(items.get(s.item) < s.amount) return false;
            }
            return true;
        }

        @Override
        public void buildConfiguration(Table table){
            for(int i = 0; i < recipes.size; i++){
                int id = i;
                table.button("Recipe " + i, () -> selected = id).width(140f);
                table.row();
            }
        }
    }
}