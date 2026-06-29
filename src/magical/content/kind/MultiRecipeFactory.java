package magical.content.kind;

import arc.func.Cons;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.power.PowerGraph;

class Recipe{
    public String key;
    public ItemStack[] inputItems;
    public LiquidStack[] inputLiquids;
    public ItemStack[] outputItems;
    public LiquidStack[] outputLiquids;
    public float craftTime;

    public Recipe(String key, float craftTime, ItemStack[] inItems, LiquidStack[] inLiquids, ItemStack[] outItems, LiquidStack[] outLiquids){
        this.key = key;
        this.inputItems = inItems;
        this.inputLiquids = inLiquids;
        this.outputItems = outItems;
        this.outputLiquids = outLiquids;
        this.craftTime = craftTime;
    }
}

public class MultiRecipeFactory extends Block {
    public Recipe[] recipes;
    public float powerConsume = 10f;
    public float powerCapacity = 60f;

    public MultiRecipeFactory(String name){
        super(name);
        size = 3;
        solid = true;
        hasItems = true;
        hasLiquids = true;
        hasPower = true;
        itemCapacity = 100;
        liquidCapacity = 60;
        configurable = true;
        saveConfig = true;
        update = true;
        buildType = FactoryBuild::new;
    }

    public class FactoryBuild extends Building {
        public int currentRecipe = 0;
        public float progress;
        public Recipe getCur(){
            return recipes[currentRecipe];
        }

        public void configure(Object value){
            if(value instanceof Integer idx && idx >=0 && idx < recipes.length){
                currentRecipe = idx;
                progress = 0;
            }
        }

        public void buildConfiguration(BaseDialog dialog){
            dialog.cont.table(t -> {
                for(int i = 0; i < recipes.length; i++){
                    int id = i;
                    Recipe r = recipes[i];
                    t.button(Vars.bundle.get(r.key), () -> {
                        configure(id);
                        dialog.hide();
                    }).size(180, 100).margin(8).table(sub -> {
                        sub.left();
                        sub.label(Vars.bundle.get(r.key)).row();
                        sub.table(inT -> {
                            for(ItemStack is : r.inputItems) inT.add(is.item.uiIcon).size(32);
                            for(LiquidStack ls : r.inputLiquids) inT.add(ls.liquid.uiIcon).size(32);
                        }).row();
                        sub.label("→").row();
                        sub.table(outT -> {
                            for(ItemStack is : r.outputItems) outT.add(is.item.uiIcon).size(32);
                            for(LiquidStack ls : r.outputLiquids) outT.add(ls.liquid.uiIcon).size(32);
                        });
                    });
                }
            });
        }

        @Override
        public void updateTile(){
            Recipe r = getCur();
            boolean canCraft = true;

            for(ItemStack stack : r.inputItems){
                if(items.get(stack.item) < stack.amount){
                    canCraft = false;
                    break;
                }
            }
            if(canCraft){
                for(LiquidStack stack : r.inputLiquids){
                    if(liquids.get(stack.liquid) < stack.amount){
                        canCraft = false;
                        break;
                    }
                }
            }
            if(canCraft){
                for(ItemStack stack : r.outputItems){
                    if(items.get(stack.item) + stack.amount > itemCapacity){
                        canCraft = false;
                        break;
                    }
                }
                if(canCraft){
                    for(LiquidStack stack : r.outputLiquids){
                        if(liquids.get(stack.liquid) + stack.amount > liquidCapacity){
                            canCraft = false;
                            break;
                        }
                    }
                }
            }

            float powerNeed = powerConsume * Time.delta;
            PowerGraph graph = power.graph;
            float availablePower = graph.getBalance();
            if(canCraft && availablePower >= powerNeed){
                graph.consumePower(powerNeed);
                progress += Time.delta;

                if(progress >= r.craftTime){
                    progress = 0;
                    // 扣原料
                    for(ItemStack stack : r.inputItems) items.remove(stack.item, stack.amount);
                    for(LiquidStack stack : r.inputLiquids) liquids.remove(stack.liquid, stack.amount);
                    // 出产物
                    for(ItemStack stack : r.outputItems) items.add(stack.item, stack.amount);
                    for(LiquidStack stack : r.outputLiquids) liquids.add(stack.liquid, stack.amount);
                    Fx.smeltsmoke.at(x, y);
                }
            }
        }

        @Override
        public void draw(){
            super.draw();
            float p = progress / getCur().craftTime;
        }
    }
}