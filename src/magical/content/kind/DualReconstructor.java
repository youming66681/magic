package magical.content;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.graphics.Color;

import mindustry.gen.Icon;
import mindustry.type.UnitType;
import mindustry.type.ItemStack;
import mindustry.ui.Styles;
import mindustry.world.blocks.units.Reconstructor;

public class DualReconstructor extends Reconstructor{

    // ====== MODE DATA ======

    public Seq<UnitType[]> firstUpgrades = new Seq<>();
    public Seq<UnitType[]> secondUpgrades = new Seq<>();

    public ItemStack[] firstReq = ItemStack.empty;
    public ItemStack[] secondReq = ItemStack.empty;

    public float firstTime = 15f * 60f;
    public float secondTime = 45f * 60f;

    public float firstPower = 5f;
    public float secondPower = 15f;

    public DualReconstructor(String name){
        super(name);

        configurable = true;

        config(Integer.class, (DualReconstructorBuild build, Integer v) -> {
            build.mode = v;
            build.progress = 0f;
        });
    }

    public class DualReconstructorBuild extends ReconstructorBuild{

        public int mode = 0;

        public boolean isFirst(){
            return mode == 0;
        }

        public float time(){
            return isFirst() ? firstTime : secondTime;
        }

        public float powerUse(){
            return isFirst() ? firstPower : secondPower;
        }

        public ItemStack[] req(){
            return isFirst() ? firstReq : secondReq;
        }

        public Seq<UnitType[]> upgrades(){
            return isFirst() ? firstUpgrades : secondUpgrades;
        }

        public String modeName(){
            return Core.bundle.get(isFirst() ? "mode.first" : "mode.second");
        }

        public Color modeColor(){
            return isFirst() ? Color.sky : Color.orange;
        }

        @Override
        public UnitType upgrade(UnitType type){
            UnitType[] r = upgrades().find(u -> u[0] == type);
            return r == null ? null : r[1];
        }

        public boolean hasReq(){
            for(ItemStack s : req()){
                if(items.get(s.item) < s.amount){
                    return false;
                }
            }
            return true;
        }

        public void consumeReq(){
            for(ItemStack s : req()){
                items.remove(s.item, s.amount);
            }
        }

        @Override
        public void updateTile(){
            super.updateTile();

            if(payload == null) return;

            UnitType result = upgrade(payload.unit.type);
            if(result == null) return;

            if(!hasReq()) return;

            if(power == null || power.status <= 0.01f) return;

            float p = power.status;

            progress += edelta() * p;

            if(progress >= time()){
                consumeReq();
                payload.unit.type = result;
                progress = 0f;
            }
        }

        @Override
        public void draw(){
            super.draw();

            if(payload == null || payload.unit == null) return;

            float f = progress / time();

            arc.graphics.g2d.Draw.alpha(1f - f);

            mindustry.graphics.Drawf.construct(
                    this,
                    upgrade(payload.unit.type),
                    payload.unit.rotation - 90f,
                    f,
                    speedScl,
                    time()
            );
        }

        // ===== UI =====

        @Override
        public void buildConfiguration(Table table){

            table.button(
                    modeName(),
                    Icon.refresh,
                    Styles.cleart,
                    () -> {
                        configure(isFirst() ? 1 : 0);
                        progress = 0f;
                    }
            ).size(160f, 50f);

            table.row();

            table.label(() ->
                    Core.bundle.format("bar.mode", modeName())
            );
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
                StringBuilder sb = new StringBuilder();
                for(UnitType[] u : upgrades()){
                    sb.append(u[0].localizedName)
                            .append(" → ")
                            .append(u[1].localizedName)
                            .append("\n");
                }
                return sb.toString();
            });
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