package magical.content;

import mindustry.type.Item;
import mindustry.world.consumers.ConsumeItemFilter;

public class ConsumeItemExplode extends ConsumeItemFilter {

    public ConsumeItemExplode() {
        super(item -> item.explosiveness > 0.01f);
    }
}