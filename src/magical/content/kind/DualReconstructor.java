package magical.content;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;

import mindustry.gen.Icon;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.blocks.units.Reconstructor;

import mindustry.type.ItemStack;
import mindustry.content.Items;
import mindustry.content.UnitTypes;

public class DualReconstructor extends Reconstructor {

    public int mode = 0;

    public DualReconstructor(String name){
        super(name);

        configurable = true;

        config(Integer.class, (DualReconstructorBuild build, Integer value) -> {
            build.mode = value;
            build.progress = 0f;
        });
    }

    public class DualReconstructorBuild extends ReconstructorBuild {

        public int mode = 0;

        @Override
        public void updateTile(){
            super.updateTile();

            if(progress >= constructTime){
                progress = 0f;
            }
        }

        @Override
        public void buildConfiguration(Table table){
            super.buildConfiguration(table);

            table.button(
                    Core.bundle.get("mode.switch"),
                    Icon.refresh,
                    Styles.cleart,
                    () -> {
                        configure(mode == 0 ? 1 : 0);
                        progress = 0f;
                    }
            ).size(140f, 50f);
        }

        @Override
        public void write(Writes w){
            super.write(w);
            w.i(mode);
        }

        @Override
        public void read(Reads r, byte rev){
            super.read(r, rev);
            mode = r.i();
        }
    }
}
    @Override
    public void buildConfiguration(Table table){

        table.row();

        table.label(() ->
                Core.bundle.format("bar.mode", modeName())
        );

        table.row();

        table.button(
                Icon.refresh,
                Styles.cleart,
                () -> {
                    configure(mode == 0 ? 1 : 0);
                    progress = 0f;
                }
        ).size(50f);

        table.row();
    }
    @Override
    public void display(Table table){
        super.display(table);

        table.row();

        table.label(() ->
                Core.bundle.format("bar.mode", modeName())
        );

        table.row();

        table.label(() -> {
            Seq<UnitType[]> list = currentUpgrades();

            StringBuilder sb = new StringBuilder();

            for(UnitType[] u : list){
                sb.append(u[0].localizedName)
                        .append(" → ")
                        .append(u[1].localizedName)
                        .append("\n");
            }

            return sb.toString();
        });
    }
    @Override
    public void setBars(){
        super.setBars();

        addBar("mode", (DualReconstructorBuild build) ->
                new mindustry.ui.Bar(
                        () -> Core.bundle.format("bar.mode", build.modeName()),
                        () -> build.mode == 0 ? 1f : 1f,
                        () -> build.mode == 0 ? arc.graphics.Color.sky : arc.graphics.Color.orange
                )
        );
    }
}