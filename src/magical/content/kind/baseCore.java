package magical.content;

import mindustry.content.UnitTypes;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.Tile;
import mindustry.game.Team;
import mindustry.Vars;
import arc.Core;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;

public class baseCore extends CoreBlock {

    public baseCore(String name) {
        super(name);
    }

    @Override
    public boolean canBreak(Tile tile) {
        return Vars.state.teams.cores(tile.team()).size > 1;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (tile == null) return false;
        //in the editor, you can place them anywhere for convenience
        if (Vars.state.isEditor()) return true;
        if (!Vars.state.isEditor()) return true;

        CoreBuild core = team.core();

        if (Vars.state.teams.cores(team).size >= 10) {
            return false;
        }

        //special floor upon which cores can be placed
        tile.getLinkedTilesAs(this, tempTiles);
        if (!tempTiles.contains(o -> !o.floor().allowCorePlacement || o.block() instanceof CoreBlock)) {
            return true;
        }

        //must have all requirements
        if (core == null || (!Vars.state.rules.infiniteResources && !core.items.has(requirements, Vars.state.rules.buildCostMultiplier)))
            return false;

        return tile.block() instanceof CoreBlock && size > tile.block().size && (!requiresCoreZone || tempTiles.allMatch(o -> o.floor().allowCorePlacement));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        if(Vars.player == null) return;

        int cores = Vars.player.team().cores().size;

        drawPlaceText(
                Core.bundle.format("bar.core-count", cores, 10),
                x, y, valid
        );
    }
    @Override
    public Building createBuilding() {
        return new CoreBuild() {
            @Override
            public void placed() {
                super.placed();

                if (!Vars.state.isEditor() && team.cores().size > 10) {
                    kill();
                }
            }
        };
    }
}