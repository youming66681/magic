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
import arc.Core;

public class DualReconstructor extends Reconstructor {

    public Seq<UnitType[]> firstUpgrades = new Seq<>();
    public Seq<UnitType[]> secondUpgrades = new Seq<>();

    public float firstConstructTime = 15f * 60f;
    public float secondConstructTime = 45f * 60f;

    public DualReconstructor(String name){
        super(name);

        configurable = true;

        config(Integer.class, (DualReconstructorBuild build, Integer value) -> {
            build.mode = value;
        });
    }

    public class DualReconstructorBuild extends ReconstructorBuild {

        public int mode = 0;

        public String modeName(){
            return Core.bundle.get(mode == 0 ? "mode.first" : "mode.second");
        }

        public Seq<UnitType[]> currentUpgrades(){
            return mode == 0 ? firstUpgrades : secondUpgrades;
        }

        public float currentConstructTime(){
            return mode == 0 ? firstConstructTime : secondConstructTime;
        }

        @Override
        public UnitType upgrade(UnitType type){
            UnitType[] r = currentUpgrades().find(u -> u[0] == type);
            return r == null ? null : r[1];
        }

        @Override
        public void buildConfiguration(Table table){
            super.buildConfiguration(table);

            table.button(
                    modeName(),
                    Icon.refresh,
                    Styles.cleart,
                    () -> configure(mode == 0 ? 1 : 0)
            ).size(140f, 50f);
        }

        @Override
        public void display(Table table){
            super.display(table);

            table.row();

            table.label(() ->
                    Core.bundle.format("bar.mode", modeName())
            );
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
        public void updateConsumes(){

            consumes.power(
                    mode == 0 ? 1.5f : 3.0f
            );

            consumes.items(
                    mode == 0
                            ? ItemStack.with(Items.copper, 20, Items.lead, 10)
                            : ItemStack.with(Items.titanium, 30, Items.silicon, 20)
            );
        }
        config(Integer.class, (DualReconstructorBuild build, Integer value) -> {
            build.mode = value;

            build.updateConsumes();
        });
        @Override
        public void init(){
            super.init();

            // 默认消耗（mode 0）
            consumes.power(1.5f);
        }
    }
}