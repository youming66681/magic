package magical.content;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.meta.Stat;
import mindustry.ui.Bar;
import mindustry.graphics.Pal;

public class DualReconstructor extends Block {

    public java.util.ArrayList<Path> paths = new java.util.ArrayList<>();

    public DualReconstructor(String name){
        super(name);

        update = true;
        solid = true;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.repairTime, t -> {
            for(Path p : paths){
                t.row();
                t.add(Core.bundle.get(p.nameKey) + ": " + (p.time / 60f) + "s");
            }
        });
    }

    @Override
    public Building create(int x, int y){
        return new UpgradeBuild();
    }

    public class UpgradeBuild extends Building {

        public int mode = 0;
        public float progress = 0f;
        public Unit unit;

        public Path path(){
            return paths.get(mode);
        }

        public void nextMode(){
            mode = (mode + 1) % paths.size();
            progress = 0f;
        }

        @Override
        public void updateTile(){

            if(unit == null) return;

            Path p = path();

            if(unit.type != p.from) return;

            progress += delta() / p.time;

            if(progress >= 1f){

                Unit u = p.to.create(team);
                u.set(unit.x, unit.y);
                u.rotation = unit.rotation;
                u.vel.set(unit.vel);

                unit.remove();
                u.add();

                unit = null;
                progress = 0f;
            }
        }

        @Override
        public boolean acceptUnit(Unit u){
            return unit == null && u.type == path().from;
        }

        @Override
        public void handleUnit(Unit u){
            if(unit == null && u.type == path().from){
                unit = u;
            }
        }

        @Override
        public void buildConfiguration(Table table){

            table.button(b -> {
                b.label(() ->
                        Core.bundle.get("upgrade.mode") + ": " +
                                Core.bundle.get(path().nameKey)
                );
            }, () -> nextMode()).width(240f).row();
        }

        @Override
        @Override
        public void init(){
            super.init();

            addBar("progress", b ->
                    new mindustry.ui.Bar(
                            () -> "upgrade.progress",
                            () -> mindustry.graphics.Pal.accent,
                            () -> progress
                    )
            );
        }

        @Override
        public void write(Writes w){
            super.write(w);
            w.i(mode);
            w.f(progress);
        }

        @Override
        public void read(Reads r, byte rev){
            super.read(r, rev);
            mode = r.i();
            progress = r.f();
        }
    }
}