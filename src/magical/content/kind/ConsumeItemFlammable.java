package magical.content;

import mindustry.type.Item;
import mindustry.world.consumers.ConsumeItemFilter;

public class ConsumeItemFlammable extends ConsumeItemFilter {

    public ConsumeItemFlammable() {
        super(item -> item.flammability > 0.01f);
    }
}