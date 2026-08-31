package magical.content;
import arc.Core;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.PayloadStack;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.units.*;
import mindustry.world.meta.*;
import java.util.*;
import static mindustry.Vars.*;
public class FlexAssembler extends UnitAssembler {
    public Map<AssemblerUnitPlan, Integer> planAreaMap = new HashMap<>();
    public Map<AssemblerUnitPlan, Integer> tierRequired = new HashMap<>();
    public FlexAssembler(String name) {
        super(name);
        configurable = true;
    }
    public void addPlan(String label, UnitType output, float time, int customArea, int requiredTier, PayloadStack... requirements) {
        Seq<PayloadStack> reqSeq = new Seq<>(requirements);
        AssemblerUnitPlan plan = new AssemblerUnitPlan(output, time, reqSeq);
        plans.add(plan);
        tierRequired.put(plan, requiredTier);
        planAreaMap.put(plan, customArea);
    }
    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.output);
        stats.add(Stat.output, table -> {
            table.row();
            Map<Integer, Seq<AssemblerUnitPlan>> byTier = new HashMap<>();
            for (AssemblerUnitPlan plan : plans) {
                int tier = tierRequired.getOrDefault(plan, 0);
                byTier.computeIfAbsent(tier, k -> new Seq<>()).add(plan);
            }
            int maxTier = byTier.keySet().stream().max(Integer::compareTo).orElse(0);
            for (int tier = 0; tier <= maxTier; tier++) {
                Seq<AssemblerUnitPlan> group = byTier.get(tier);
                if (group == null || group.isEmpty()) continue;
                final int currentTier = tier;
                table.table(Tex.pane, t -> {
                    t.add(Core.bundle.format("flexassembler.tier.stat", currentTier))
                            .pad(5)
                            .left()
                            .growX();
                }).growX().pad(5).row();
                for (AssemblerUnitPlan plan : group) {
                    table.table(Tex.pane, t -> {
                        if (plan.unit.isBanned()) {
                            t.image(Icon.cancel)
                                    .color(Pal.remove)
                                    .size(40)
                                    .pad(10);
                            return;
                        }
                        if (plan.unit.unlockedNow()) {
                            t.image(plan.unit.uiIcon)
                                    .scaling(Scaling.fit)
                                    .size(40)
                                    .pad(10)
                                    .left();
                            t.table(info -> {
                                info.left();
                                info.add(plan.unit.localizedName)
                                        .left();
                                info.row();
                                info.add(
                                        Strings.autoFixed(plan.time / 60f, 1)
                                                + " "
                                                + Core.bundle.get("unit.seconds")
                                ).color(Color.lightGray).left();
                                int required = tierRequired.getOrDefault(plan, 0);
                                if (required > 0) {
                                    info.row();
                                    info.add(
                                            Core.bundle.format(
                                                    "flexassembler.tier.stat",
                                                    required
                                            )
                                    ).color(Color.lightGray).left();
                                }
                                info.row();
                                info.add(
                                        Core.bundle.format(
                                                "flexassembler.area.stat",
                                                planAreaMap.getOrDefault(plan, areaSize)
                                        )
                                ).color(Color.lightGray).left();
                            }).left();
                            t.table(req -> {
                                for (int i = 0; i < plan.requirements.size; i++) {
                                    if (i % 4 == 0) req.row();
                                    req.add(
                                            StatValues.stack(plan.requirements.get(i))
                                    ).pad(5);
                                }
                            }).right();
                        } else {
                            t.image(Icon.lock)
                                    .color(Pal.darkerGray)
                                    .size(40)
                                    .pad(10);
                        }
                    }).growX().pad(5).row();
                }
            }
        });
    }
    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        private static final int NO_PLAN = -1;
        private int lockedIndex = NO_PLAN;
        public int myAreaSize = FlexAssembler.this.areaSize;
        private AssemblerUnitPlan lockedPlan() {
            if (lockedIndex < 0 || lockedIndex >= plans.size) {
                return null;
            }
            AssemblerUnitPlan plan = plans.get(lockedIndex);
            if (plan == null) {
                return null;
            }
            int requiredTier = tierRequired.getOrDefault(plan, 0);
            if (requiredTier > currentTier) {
                return null;
            }
            return plan;
        }
        private boolean hasLockedPlan() {
            return lockedIndex >= 0
                    && lockedIndex < plans.size
                    && plans.get(lockedIndex) != null;
        }
        private void syncArea() {
            AssemblerUnitPlan plan = lockedPlan();
            if (plan != null) {
                myAreaSize = planAreaMap.getOrDefault(
                        plan,
                        FlexAssembler.this.areaSize
                );
            } else {
                myAreaSize = FlexAssembler.this.areaSize;
            }
        }
        @Override
        public void created() {
            super.created();
            syncArea();
        }
        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            syncArea();
        }
        @Override
        public boolean moduleFits(Block other, float ox, float oy, int rotation) {
            float dx = ox
                    + Geometry.d4x(rotation)
                    * (other.size / 2f + 0.5f)
                    * tilesize;
            float dy = oy
                    + Geometry.d4y(rotation)
                    * (other.size / 2f + 0.5f)
                    * tilesize;
            Vec2 spawn = getUnitSpawn();
            if (Tile.relativeTo(ox, oy, spawn.x, spawn.y) != rotation) {
                return false;
            }
            float dst = Math.max(
                    Math.abs(dx - spawn.x),
                    Math.abs(dy - spawn.y)
            );
            return Mathf.equal(
                    dst,
                    tilesize * myAreaSize / 2f - tilesize / 2f
            );
        }
        @Override
        public Vec2 getUnitSpawn() {
            float len = tilesize * (myAreaSize + block.size) / 2f;
            return Tmp.v4.set(
                    x + Geometry.d4x(rotation) * len,
                    y + Geometry.d4y(rotation) * len
            );
        }
        @Override
        public void buildConfiguration(Table table) {
            if (Vars.headless) return;
            AssemblerUnitPlan current = lockedPlan();
            boolean locked = current != null;
            Seq<AssemblerUnitPlan> available = new Seq<>();
            for (AssemblerUnitPlan plan : plans) {
                int requiredTier = tierRequired.getOrDefault(plan, 0);
                if (requiredTier <= currentTier) {
                    available.add(plan);
                }
            }
            if (available.isEmpty()) {
                table.label(
                        () -> Core.bundle.get("flexassembler.no-plans")
                ).pad(10);
                if (hasLockedPlan()) {
                    table.row();
                    table.button(
                            Core.bundle.get("flexassembler.deselect"),
                            () -> configure(NO_PLAN)
                    );
                }
                return;
            }
            if (locked) {
                table.label(
                        () -> Core.bundle.format(
                                "flexassembler.producing",
                                current.unit.localizedName
                        )
                ).padBottom(4).row();
            } else {
                table.label(
                                () -> Core.bundle.get("flexassembler.select-unit")
                        ).padBottom(4)
                        .color(Color.gray)
                        .row();
            }
            Table grid = new Table();
            int cols = 4;
            for (int i = 0; i < available.size; i++) {
                if (i % cols == 0 && i != 0) {
                    grid.row();
                }
                AssemblerUnitPlan plan = available.get(i);
                boolean isChosen = locked && current == plan;
                int index = plans.indexOf(plan);
                Button btn = new Button(Tex.button);
                btn.table(inner -> {
                    inner.image(plan.unit.uiIcon)
                            .size(30f)
                            .padBottom(4f);
                    inner.row();
                    inner.add(plan.unit.localizedName)
                            .color(
                                    isChosen
                                            ? Pal.accent
                                            : Color.lightGray
                            );
                }).pad(8);
                btn.clicked(() -> configure(index));
                grid.add(btn)
                        .size(80f, 80f)
                        .pad(4f);
            }
            ScrollPane pane = new ScrollPane(grid);
            table.add(pane)
                    .grow()
                    .maxHeight(400f)
                    .row();
            if (locked) {
                table.button(
                        Core.bundle.get("flexassembler.deselect"),
                        () -> configure(NO_PLAN)
                ).padTop(5f).row();
            }
        }
        @Override
        public Object config() {
            return lockedIndex;
        }
        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                int idx = (Integer)value;
                if (idx == NO_PLAN) {
                    lockedIndex = NO_PLAN;
                    myAreaSize = FlexAssembler.this.areaSize;
                } else if (idx >= 0 && idx < plans.size) {
                    AssemblerUnitPlan plan = plans.get(idx);
                    if (plan != null) {
                        int requiredTier = tierRequired.getOrDefault(
                                plan,
                                0
                        );
                        if (requiredTier <= currentTier) {
                            lockedIndex = idx;
                            syncArea();
                        }
                    }
                }
            }
            super.configure(value);
        }
        @Override
        public AssemblerUnitPlan plan() {
            AssemblerUnitPlan locked = lockedPlan();
            if (locked != null) {
                return locked;
            }
            if (lockedIndex >= 0) {
                return null;
            }
            return super.plan();
        }
        @Override
        public boolean shouldConsume() {
            AssemblerUnitPlan locked = lockedPlan();
            if (locked != null) {
                return true;
            }
            if (lockedIndex >= 0) {
                return false;
            }
            return super.shouldConsume();
        }
        @Override
        public void updateTile() {
            syncArea();
            int previousArea = areaSize;
            areaSize = myAreaSize;
            super.updateTile();
            areaSize = previousArea;
        }
        @Override
        public void draw() {
            int previousArea = areaSize;
            areaSize = myAreaSize;
            super.draw();
            areaSize = previousArea;
        }
        @Override
        public void drawSelect() {
            int previousArea = areaSize;
            areaSize = myAreaSize;
            super.drawSelect();
            areaSize = previousArea;
        }
        public void spawned() {
            AssemblerUnitPlan plan = lockedPlan();
            if (plan == null) {
                if (lockedIndex >= 0) {
                    return;
                }
                plan = super.plan();
            }
            if (plan == null) return;
            Vec2 spawn = getUnitSpawn();
            consume();
            Unit unit = plan.unit.create(team);
            if (unit.isCommandable() && commandPos != null) {
                unit.command().commandPosition(commandPos);
            }
            unit.set(
                    spawn.x + Mathf.range(0.001f),
                    spawn.y + Mathf.range(0.001f)
            );
            unit.rotation = rotdeg();
            if (!net.client()) {
                unit.add();
            }
            createSound.at(
                    spawn.x,
                    spawn.y,
                    1f + Mathf.range(0.06f),
                    createSoundVolume
            );
            progress = 0f;
            Fx.unitAssemble.at(
                    spawn.x,
                    spawn.y,
                    rotdeg() - 90f,
                    plan.unit
            );
            blocks.clear();
        }
        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(lockedIndex);
        }
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            lockedIndex = read.i();
            syncArea();
        }
    }
}