package magical.content;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.ui.Cicon;
import mindustry.ui.ItemImage;
import mindustry.ui.LiquidImage;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.meta.BlockStat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.*;

class Recipe{
    public ItemStack[] inputItems;
    public LiquidStack[] inputLiquids;
    public ItemStack[] outputItems;
    public LiquidStack[] outputLiquids;
    public float craftTime;
    public String name;

    public Recipe(String name, float craftTime, ItemStack[] inItems, LiquidStack[] inLiquids, ItemStack[] outItems, LiquidStack[] outLiquids){
        this.name = name;
        this.craftTime = craftTime;
        this.inputItems = inItems;
        this.inputLiquids = inLiquids;
        this.outputItems = outItems;
        this.outputLiquids = outLiquids;
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

    @Override
    public void setStats(){
        super.setStats();
        stats.add(BlockStat.powerUse, powerConsume, StatUnit.powerSecond);
    }

    public class FactoryBuild extends Building {
        public int currentRecipe = 0;
        public float progress;
        public Recipe getCur(){
            return recipes[currentRecipe];
        }

        @Override
        public void configure(Object value){
            if(value instanceof Integer idx && idx >=0 && idx < recipes.length){
                currentRecipe = idx;
                progress = 0;
            }
        }

        @Override
        public void buildConfiguration(BaseDialog dialog){
            dialog.cont.table(t -> {
                for(int i = 0; i < recipes.length; i++){
                    int id = i;
                    Recipe r = recipes[i];
                    t.button(b -> {
                        b.left();
                        b.label(r.name);
                        b.row();
                        b.table(in -> {
                            for(ItemStack is : r.inputItems) in.add(new ItemImage(is));
                            for(LiquidStack ls : r.inputLiquids) in.add(new LiquidImage(ls));
                        });
                        b.row();
                        b.label("→");
                        b.row();
                        b.table(out -> {
                            for(ItemStack is : r.outputItems) out.add(new ItemImage(is));
                            for(LiquidStack ls : r.outputLiquids) out.add(new LiquidImage(ls));
                        });
                    }).size(180, 100).margin(8).clicked(() -> {
                        configure(id);
                        dialog.hide();
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

            if(canCraft && power.graph.getPower() >= powerConsume * Time.delta){
                power.graph.consumePower(powerConsume * Time.delta);
                progress += Time.delta;

                if(progress >= r.craftTime){
                    progress = 0;
                    for(ItemStack stack : r.inputItems) items.remove(stack.item, stack.amount);
                    for(LiquidStack stack : r.inputLiquids) liquids.remove(stack.liquid, stack.amount);
                    for(ItemStack stack : r.outputItems) items.add(stack.item, stack.amount);
                    for(LiquidStack stack : r.outputLiquids) liquids.add(stack.liquid, stack.amount);
                    Fx.factorySmoke.at(x, y);
                }
            }
        }

        @Override
        public void displayBars(Bar[] bars){
            super.displayBars(bars);
            bars[bars.length - 1].set(progress / getCur().craftTime);
        }

        @Override
        public void draw(){
            super.draw();
            Draw.color();
            Draw.rect(block.region, x, y);
            float p = progress / getCur().craftTime;
            Draw.color(0.2f, 0.8f, 1f);
            Draw.rect(block.region, x, y, size * tilesize * p, size * tilesize / 4);
            Draw.color();
        }
    }
}