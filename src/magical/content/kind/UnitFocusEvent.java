package magical.content;

public class UnitFocusEvent{

    public float x;
    public float y;
    public float time;
    public String message;

    public UnitFocusEvent(float x,float y,float time,String message){
        this.x = x;
        this.y = y;
        this.time = time;
        this.message = message;
    }
}