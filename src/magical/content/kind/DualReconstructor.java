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

    public ModeConfig[] modes = new ModeConfig[2];

    public static class ModeConfig{
        public Seq<UnitType[]> upgrades = new Seq<>();
        public float constructTime;
        public float power;
        public ItemStack[] items;
    }

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

        ModeConfig cfg(){
            return modes[mode];
        }

        @Override
        public UnitType upgrade(UnitType type){
            UnitType[] r = cfg().upgrades.find(u -> u[0] == type);
            return r == null ? null : r[1];
        }

        @Override
        public void updateTile(){
            super.updateTile();

            // 动态耗电（真正有效方式）
            this.powerUse = cfg().power;

            if(progress >= cfg().constructTime){
                progress = 0f;
            }
        }

        @Override
        public void draw(){
            super.draw();

            if(payload == null || payload.unit == null) return;

            float f = progress / cfg().constructTime;

            arc.graphics.g2d.Draw.alpha(1f - f);

            mindustry.graphics.Drawf.construct(
                    this,
                    upgrade(payload.unit.type),
                    payload.unit.rotation - 90f,
                    f,
                    speedScl,
                    time
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
    public String modeName(){
        return mode == 0
                ? Core.bundle.get("mode.first")
                : Core.bundle.get("mode.second");
    }
}