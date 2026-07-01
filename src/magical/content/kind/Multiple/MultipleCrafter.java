package magical.content.kind.multiple;

import arc.struct.Seq;
import mindustry.world.Block;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class MultiCrafterBlock extends Block {

    public Seq<Recipe> recipes = new Seq<>();

    public MultiCrafterBlock(String name){
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        hasPower = true;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.output, table -> {
            for(Recipe r : recipes){
                table.row();
                table.add(r.name + "[]").left();
                table.row();

                table.add(r.time / 60f + "s").left();
                table.row();

                table.add(r.power + " power/t").left();
                table.row();

                table.add(r.output).left();
            }
        });
    }

    public static class Recipe{
        public String name;
        public float time = 60f;
        public float power = 1f;

        public mindustry.type.ItemStack[] inputs;
        public mindustry.type.ItemStack output;

        public Recipe(String name){
            this.name = name;
        }
    }

public class MultiCrafterBuild extends GenericCrafter.GenericCrafterBuild {

    public int recipeIndex = 0;
    public float progress = 0f;

    @Override
    public void updateTile(){

        MultiCrafterBlock block = (MultiCrafterBlock) this.block;
        var recipe = block.recipes.get(recipeIndex);

        // 电力
        float eff = power.status;

        if(eff <= 0f) return;

        // 检查输入
        if(!hasItems(recipe)) return;

        progress += edelta() * eff;

        if(progress >= recipe.time){
            consumeItems(recipe);
            if(outputItems != null){
                outputItems.add(recipe.output.item, recipe.output.amount);
            }
            progress = 0f;
        }
    }

    boolean hasItems(MultiCrafterBlock.Recipe r){
        for(var i : r.inputs){
            if(items.get(i.item) < i.amount){
                return false;
            }
        }
        return true;
    }

    void consumeItems(MultiCrafterBlock.Recipe r){
        for(var i : r.inputs){
            items.remove(i);
        }
    }

    @Override
    public void buildConfiguration(mindustry.ui.BuildConfig config){
        MultiCrafterBlock block = (MultiCrafterBlock) this.block;

        config.button("切换配方: " + block.recipes.get(recipeIndex).name, () -> {
            recipeIndex++;
            if(recipeIndex >= block.recipes.size) recipeIndex = 0;
            });
        }
    }
}