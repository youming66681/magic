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

public class DualReconstructor extends Reconstructor{

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

    public class DualReconstructorBuild extends ReconstructorBuild{

        public int mode = 0;

        public Seq<UnitType[]> currentUpgrades(){
            return mode == 0 ? firstUpgrades : secondUpgrades;
        }

        public float currentConstructTime(){
            return mode == 0 ? firstConstructTime : secondConstructTime;
        }

        public String modeName(){
            return Core.bundle.get(
                    mode == 0 ? "mode.first" : "mode.second"
            );
        }

        @Override
        public void buildConfiguration(Table table){
            super.buildConfiguration(table);

            table.row();

            table.button(
                    Icon.refresh,
                    Styles.cleari,
                    () -> configure(mode == 0 ? 1 : 0)
            ).size(50f);

            table.row();

            table.label(() ->
                    Core.bundle.format(
                            "bar.mode",
                            modeName()
                    )
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
    }
    public Seq<UnitType[]> currentUpgrades(){
        return mode == 0 ?
                firstUpgrades :
                secondUpgrades;
    }

    public float currentConstructTime(){
        return mode == 0 ?
                firstConstructTime :
                secondConstructTime;
    }
    @Override
    public UnitType upgrade(UnitType type){
        UnitType[] r = currentUpgrades().find(u -> u[0] == type);
        return r == null ? null : r[1];
    }
    @Override
    public float fraction(){
        return progress / currentConstructTime();
    }
    Draw.alpha(1f - progress / currentConstructTime());

     Drawf.construct(
        this,
    upgrade(payload.unit.type),
    payload.rotation() - 90f,
    progress / currentConstructTime(),
    speedScl,
    time
    );

    progress += edelta() * state.rules.unitBuildSpeed(team);

    if(progress >= currentConstructTime()){

    }
    @Override
    public void display(Table table){

        super.display(table);

        table.row();

        table.label(() ->
                Core.bundle.format(
                        "bar.mode",
                        modeName()
                )
        );
    }
}