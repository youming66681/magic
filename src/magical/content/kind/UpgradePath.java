package magical.content;

import mindustry.type.UnitType;

public class UpgradePath{

    public String bundleKey;
    public UnitType from;
    public UnitType to;

    public UpgradePath(String bundleKey, UnitType from, UnitType to){
        this.bundleKey = bundleKey;
        this.from = from;
        this.to = to;
    }
}