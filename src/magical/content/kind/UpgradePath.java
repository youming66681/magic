package magical.content;

import mindustry.type.UnitType;

public class UpgradePath{

    public String bundleKey;
    public UnitType from;
    public UnitType to;

    public consumePower();
    public consumeItems(ItemStack.with(new Object[]{MLItems.phantomSteel, 60, Items.graphite, 30}));

    public constructTime = 60f * 15f;

    public UpgradePath(String bundleKey, UnitType from, UnitType to){
        this.bundleKey = bundleKey;
        this.from = from;
        this.to = to;
    }
}