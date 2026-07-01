package magical.world.blocks.production;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.world.blocks.production.GenericCrafter;

public class MultipleCrafterBuild extends GenericCrafter.GenericCrafterBuild{

    /** 当前配方编号 */
    protected int recipe = 0;

    /** 当前配方缓存 */
    protected Formula current;

    /** 当前生产进度 */
    protected float progress;

    /** 当前暖机 */
    protected float warmup;

    /** 总进度(动画使用) */
    protected float totalProgress;

    /** 输出时间 */
    protected float dumpTime;

    /** 初始化 */
    @Override
    public void created(){
        super.created();
        updateFormula();
    }

    /** 当前配方 */
    public Formula formula(){
        return current;
    }

    /** 当前编号 */
    public int recipe(){
        return recipe;
    }

    /** 切换配方 */
    public void setRecipe(int id){

        MultipleCrafter block = (MultipleCrafter)this.block;

        if(block.formulas.isEmpty()) return;

        if(id < 0 || id >= block.formulas.size) return;

        if(recipe == id) return;

        recipe = id;

        updateFormula();
    }

    /** 更新缓存 */
    public void updateFormula(){

        MultipleCrafter block = (MultipleCrafter)this.block;

        if(block.formulas.isEmpty()){
            current = null;
            return;
        }

        recipe = Math.max(0,
                Math.min(recipe, block.formulas.size - 1));

        current = block.formulas.get(recipe);
    }

    @Override
    public void updateTile(){

        if(current == null){
            return;
        }

    }

    @Override
    public void write(Writes write){

        super.write(write);

        write.i(recipe);
    }

    @Override
    public void read(Reads read, byte revision){

        super.read(read, revision);

        recipe = read.i();

        updateFormula();
    }
    protected boolean canCraft(){

        if(current == null) return false;

        //检查物品
        for(ItemStack stack : current.inputItems){

            if(items.get(stack.item) < stack.amount){
                return false;
            }

        }

        //检查液体

        for(LiquidStack stack : current.inputLiquids){

            if(liquids.get(stack.liquid) < stack.amount){
                return false;
            }

        }

        return true;
    }
    protected boolean outputAvailable(){

        for(ItemStack stack : current.outputItems){

            if(items.get(stack.item)
                    + stack.amount
                    > block.itemCapacity){

                return false;

            }

        }

        for(LiquidStack stack : current.outputLiquids){

            if(liquids.get(stack.liquid)
                    + stack.amount
                    > block.liquidCapacity){

                return false;

            }

        }

        return true;
    }
    protected boolean canCraft(){

        return current != null
                && inputAvailable()
                && outputAvailable();
    }
    protected boolean hasInput(){

        if(current == null) return false;

        //物品
        for(ItemStack stack : current.inputItems){
            if(items.get(stack.item) < stack.amount){
                return false;
            }
        }

        //液体
        for(LiquidStack stack : current.inputLiquids){
            if(liquids.get(stack.liquid) < stack.amount){
                return false;
            }
        }

        return true;
    }
    protected boolean hasOutputSpace(){

        MultipleCrafter mc = (MultipleCrafter)block;

        for(ItemStack stack : current.outputItems){
            if(items.get(stack.item) + stack.amount > mc.itemCapacity){
                return false;
            }
        }

        for(LiquidStack stack : current.outputLiquids){
            if(liquids.get(stack.liquid) + stack.amount > mc.liquidCapacity){
                return false;
            }
        }

        return true;
    }
    protected void consumeFormula(){

        for(ItemStack stack : current.inputItems){
            items.remove(stack.item, stack.amount);
        }

        for(LiquidStack stack : current.inputLiquids){
            liquids.remove(stack.liquid, stack.amount);
        }
    }
    protected void craftFormula(){

        consumeFormula();

        //输出物品
        for(ItemStack stack : current.outputItems){
            for(int i = 0; i < stack.amount; i++){
                offload(stack.item);
            }
        }

        //输出液体
        for(LiquidStack stack : current.outputLiquids){
            liquids.add(stack.liquid, stack.amount);
        }

        if(current.craftEffect != null){
            current.craftEffect.at(x, y);
        }
    }
    protected void updateCraft(){

        if(!hasInput()) return;
        if(!hasOutputSpace()) return;

        progress += edelta() / current.craftTime;

        warmup = Mathf.approachDelta(warmup, 1f, 0.02f);

        totalProgress += warmup * edelta();

        if(progress >= 1f){

            progress = 0f;

            craftFormula();
        }
    }
    consume(new DynamicConsumePower(build -> {

        MultipleCrafterBuild b = (MultipleCrafterBuild)build;

        Formula f = b.formula();

        return f == null ? 0f : f.powerUse;

    }));
    @Override
    public boolean shouldConsume(){

        return hasInput()
                && hasOutputSpace();
    }
    @Override
    public void updateTile(){

        if(current == null){
            return;
        }

        if(canCraft()){

            progress += efficiency * edelta() / current.craftTime;

            warmup = Mathf.approachDelta(warmup,1f,0.02f);

            if(progress >= 1f){

                progress %= 1f;

                craftFormula();
            }

        }else{

            warmup = Mathf.approachDelta(warmup,0f,0.02f);

        }

        totalProgress += warmup * edelta();

        dumpFormula();
    }
}