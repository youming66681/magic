package magical.content;

import arc.util.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.units.*;

import static mindustry.Vars.*;

public class FlexAssemblerModule extends UnitAssemblerModule {

    public FlexAssemblerModule(String name) {
        super(name);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (!super.canPlaceOn(tile, team, rotation)) return false;

        // 查找相邻的 FlexAssembler
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                Tile other = Vars.world.tile(tile.x + dx, tile.y + dy);
                if (other != null && other.build instanceof FlexAssembler.FlexAssemblerBuild assembler) {
                    if (assembler.moduleFits(this, tile.worldx(), tile.worldy(), rotation)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}