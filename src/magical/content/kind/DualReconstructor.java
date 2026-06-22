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

public class DualReconstructor extends Reconstructor{

    public float firstConstructTime = 15f * 60f;
    public float secondConstructTime = 45f * 60f;

    public float firstPowerUse = 5f;
    public float secondPowerUse = 15f;

    public ItemStack[] firstRequirements = ItemStack.empty;
    public ItemStack[] secondRequirements = ItemStack.empty;

    public Seq<UnitType[]> firstUpgrades = new Seq<>();
    public Seq<UnitType[]> secondUpgrades = new Seq<>();

    public DualReconstructor(String name){
        super(name);

        configurable = true;

        config(Integer.class, (DualReconstructorBuild build, Integer value) -> {
            build.mode = value;
        });
    }

    @Override
    public void setBars(){
        super.setBars();
    }

    public class DualReconstructorBuild extends ReconstructorBuild{

        public int mode = 0;

        public String modeName(){
            return Core.bundle.get(
                    mode == 0 ?
                            "mode.first" :
                            "mode.second"
            );
        }

        public Seq<UnitType[]> currentUpgrades(){
            return mode == 0 ?
                    firstUpgrades :
                    secondUpgrades;
        }

        @Override
        public UnitType upgrade(UnitType type){

            UnitType[] found =
                    currentUpgrades().find(
                            u -> u[0] == type
                    );

            return found == null ?
                    null :
                    found[1];
        }

        @Override
        public void buildConfiguration(Table table){

            super.buildConfiguration(table);

            table.row();

            table.label(() ->
                    Core.bundle.format(
                            "bar.mode",
                            modeName()
                    )
            );

            table.row();

            table.button(
                    Core.bundle.get("mode.switch"),
                    Icon.refresh,
                    Styles.cleart,
                    () -> configure(
                            mode == 0 ? 1 : 0
                    )
            ).size(160f, 50f);

            table.row();

            table.label(() -> {

                StringBuilder sb =
                        new StringBuilder();

                for(UnitType[] u : currentUpgrades()){

                    sb.append(
                            u[0].localizedName
                    );

                    sb.append(" → ");

                    sb.append(
                            u[1].localizedName
                    );

                    sb.append("\n");
                }

                return sb.toString();
            });
        }

        @Override
        public void write(Writes write){

            super.write(write);

            write.i(mode);
        }

        @Override
        public void read(
                Reads read,
                byte revision
        ){

            super.read(read, revision);

            mode = read.i();
        }
    }
}