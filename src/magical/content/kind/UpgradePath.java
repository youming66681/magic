package magical.content;

import mindustry.type.UnitType;

public class UpgradePath {

    public String nameKey;
    public UnitType from, to;
    public float time;

    public UpgradePath(String nameKey, UnitType from, UnitType to, float time){
        this.nameKey = nameKey;
        this.from = from;
        this.to = to;
        this.time = time;
    }
}