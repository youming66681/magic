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

    public static class UpgradePath {
        public String nameKey;
        public UnitType from, to;
        public float time;

        public UpgradePath(String nameKey, UnitType from, UnitType to, float time) {
            this.nameKey = nameKey;
            this.from = from;
            this.to = to;
            this.time = time;
        }
    }

    public java.util.ArrayList<UpgradePath> paths = new java.util.ArrayList<>();

    public DualReconstructor(String name) {
        super(name);
        update = true;
        solid = true;
    }

    @Override
    public void setStats() {
        super.setStats();

        stats.add(Stat.repairTime, table -> {
            for (UpgradePath p : paths) {
                table.row();
                table.add(Core.bundle.get(p.nameKey) + ": " + (p.time / 60f) + "s");
            }
        });
    }

    @Override
    public Building create(int x, int y) {
        return new UpgradeBuild();
    }

    public class UpgradeBuild extends Building {

        public int mode = 0;
        public float progress = 0f;
        public Unit target;

        public UpgradePath path() {
            return paths.get(mode);
        }

        public void nextMode() {
            mode = (mode + 1) % paths.size();
            progress = 0f;
        }

        @Override
        public void updateTile() {
            if (target == null) return;

            UpgradePath p = path();

            if (target.type != p.from) return;

            progress += delta() / p.time;

            if (progress >= 1f) {

                Unit u = p.to.create(team);
                u.set(target.x, target.y);
                u.rotation = target.rotation;
                u.vel.set(target.vel); // ✔ 修复 velocity()

                target.remove();
                u.add();

                target = null;
                progress = 0f;
            }
        }

        @Override
        public boolean acceptUnit(Unit unit) {
            return target == null && unit.type == path().from;
        }

        @Override
        public void handleUnit(Unit unit) {
            if (target == null && unit.type == path().from) {
                target = unit;
            }
        }

        @Override
        public void buildConfiguration(Table table) {

            table.button(b -> {
                b.label(() ->
                        Core.bundle.get("upgrade.mode") + ": " +
                                Core.bundle.get(path().nameKey)
                );
            }, () -> nextMode()).width(220f).row();
        }
        @Override
        public void setBars() {
            super.setBars();

            addBar("progress", (UpgradeBuild b) ->
                    new Bar(
                            () -> Core.bundle.get("upgrade.progress"),
                            () -> Pal.accent,
                            () -> b.progress
                    )
            );
        }

        @Override
        public void write(Writes w) {
            super.write(w);
            w.i(mode);
            w.f(progress);
        }

        @Override
        public void read(Reads r, byte rev) {
            super.read(r, rev);
            mode = r.i();
            progress = r.f();
        }
    }
}