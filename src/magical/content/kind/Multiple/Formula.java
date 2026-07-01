package magical.content.kind.Multiple;

import arc.struct.Seq;
import mindustry.entities.Effect;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.content.Fx;

public class Formula{

    /** 输入物品 */
    public final Seq<ItemStack> inputItems = new Seq<>();

    /** 输出物品 */
    public final Seq<ItemStack> outputItems = new Seq<>();

    /** 输入液体 */
    public final Seq<LiquidStack> inputLiquids = new Seq<>();

    /** 输出液体 */
    public final Seq<LiquidStack> outputLiquids = new Seq<>();

    /** 生产时间 */
    public float craftTime = 60f;

    /** 每秒耗电 */
    public float powerUse = 0f;

    /** 更新特效 */
    public Effect updateEffect = Fx.none;

    /** 合成特效 */
    public Effect craftEffect = Fx.none;

    /** 绘制器 */
    public DrawBlock drawer = new DrawDefault();


    public Formula input(ItemStack... stacks){
        inputItems.add(stacks);
        return this;
    }

    public Formula output(ItemStack... stacks){
        outputItems.add(stacks);
        return this;
    }

    public Formula input(LiquidStack... stacks){
        inputLiquids.add(stacks);
        return this;
    }

    public Formula output(LiquidStack... stacks){
        outputLiquids.add(stacks);
        return this;
    }

    public Formula craftTime(float time){
        craftTime = time;
        return this;
    }

    public Formula power(float power){
        powerUse = power;
        return this;
    }

    public Formula drawer(DrawBlock draw){
        drawer = draw;
        return this;
    }

    public Formula craftEffect(Effect effect){
        craftEffect = effect;
        return this;
    }

    public Formula updateEffect(Effect effect){
        updateEffect = effect;
        return this;
    }
}