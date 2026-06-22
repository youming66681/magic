package magical.content;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.meta.Stat;

public class DualReconstructor extends Block {

    public static class UpgradePath {
        public String nameKey;

        public UnitType from;
        public UnitType to;

        public float time;

        public UpgradePath(String nameKey, UnitType from, UnitType to, float time) {
            this.nameKey = nameKey;
            this.from = from;
            this.to = to;
            this.time = time;
        }
    }

    public Seq<UpgradePath> paths = new Seq();

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
                table.add(Core.bundle.get(p.nameKey));
                table.add(" : " + p.time / 60f + "s");
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

        public Unit targetUnit;

        public UpgradePath path() {
            return paths.get(mode);
        }

        public void nextMode() {
            mode = (mode + 1) % paths.size;
            progress = 0f;
        }

        public boolean acceptUnit(Unit unit) {
            return unit != null && unit.type == path().from;
        }

        @Override
        public void updateTile() {

            if (targetUnit == null) return;

            UpgradePath p = path();

            if (targetUnit.type != p.from) return;

            progress += delta() / p.time;

            if (progress >= 1f) {

                Unit u = p.to.create(team);
                u.set(targetUnit.x, targetUnit.y);
                u.rotation = targetUnit.rotation;
                u.velocity().set(targetUnit.velocity());

                targetUnit.remove();
                u.add();

                targetUnit = null;
                progress = 0f;
            }
        }

        @Override
        public void handleUnitPayload(Unit unit) {
            if (targetUnit == null && unit.type == path().from) {
                targetUnit = unit;
            }
        }

        @Override
        public void buildConfiguration(Table table) {

            table.button(b -> {
                b.label(() ->
                        Core.bundle.get("upgrade.mode") + ": " +
                                Core.bundle.get(path().nameKey)
                );
            }, () -> {
                nextMode();
            }).width(240f).row();
        }

        @Override
        public void displayStats(Table table) {

            UpgradePath p = path();

            table.add(Core.bundle.get("upgrade.from") + ": " + p.from.localizedName).row();
            table.add(Core.bundle.get("upgrade.to") + ": " + p.to.localizedName).row();
            table.add(Core.bundle.get("upgrade.time") + ": " + p.time / 60f + "s").row();
        }

        @Override
        public void setBars() {
            super.setBars();

            addBar("upgrade", (UpgradeBuild b) ->
                    new Bar(
                            () -> Core.bundle.get("upgrade.progress"),
                            () -> mindustry.ui.Bar.green,
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