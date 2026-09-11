package magical.content;

import mindustry.gen.Unit;

public class UnitFocusEvent{
    public Unit unit;
    public float time;

    public UnitFocusEvent(Unit unit,float time){
        this.unit = unit;
        this.time = time;
    }
}