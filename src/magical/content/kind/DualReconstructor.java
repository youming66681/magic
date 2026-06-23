package magical.content;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;

import mindustry.ui.Bar;
import mindustry.graphics.Pal;

import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.meta.Stat;
import mindustry.type.UnitType;

public class DualReconstructor extends Reconstructor{

    public Seq<UpgradePath> first = new Seq<>();
    public Seq<UpgradePath> second = new Seq<>();

    public DualReconstructor(String name){
        super(name);

        configurable = true;

        config(Integer.class, (DualReconstructorBuild tile, Integer value) -> {
            tile.mode = value;
        });
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.output, table -> {

            table.row();
            table.add("[accent]" + Core.bundle.get("mode.first"));

            for(var p : first){
                table.row();
                table.add(p.from.localizedName + " → " + p.to.localizedName);
            }

            table.row();
            table.add("[accent]" + Core.bundle.get("mode.second"));

            for(var p : second){
                table.row();
                table.add(p.from.localizedName + " → " + p.to.localizedName);
            }
        });
    }

    public class DualReconstructorBuild extends ReconstructorBuild{

        public int mode = 0;

        public Seq<UpgradePath> current(){
            return mode == 0 ? first : second;
        }

        @Override
        public void buildConfiguration(Table table){

            table.button(b -> {
                b.label(() ->
                        Core.bundle.get(
                                mode == 0 ?
                                        "mode.first" :
                                        "mode.second"
                        )
                );
            }, () -> {
                configure(mode == 0 ? 1 : 0);
            }).size(220f, 50f);
        }

        @Override
        public UnitType upgrade(UnitType type){

            for(var p : current()){
                if(p.from == type){
                    return p.to;
                }
            }

            return null;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(mode);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            mode = read.i();
        }
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("mode", (DualReconstructorBuild build) ->
                new Bar(
                        () -> Core.bundle.get(
                                build.mode == 0 ?
                                        "mode.first" :
                                        "mode.second"
                        ),
                        () -> Pal.accent,
                        () -> 1f
                )
        );
    }
}