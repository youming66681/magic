package magical.content;

import mindustry.type.ItemStack;
import mindustry.type.UnitType;

public class UpgradePath{

    public String bundleKey;

    public UnitType from;
    public UnitType to;

    public float consumePower();
    public consumeItems(ItemStack.with(new Object[]{}));

    public float constructTime;

    public UpgradePath(
            String bundleKey,
            UnitType from,
            UnitType to,
            float constructTime,
            float consumePower,
            consumeItems(ItemStack.with(new Object[]{}))
    ){
        this.bundleKey = bundleKey;
        this.from = from;
        this.to = to;

        this.constructTime = constructTime;
        this.consumePower = consumePower;
        this.consumeItems(ItemStack.with(new Object[]{})) = consumeItems(ItemStack.with(new Object[]{}));
    }
}