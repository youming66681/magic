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
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.PayloadStack;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.units.*;
import mindustry.world.blocks.units.UnitAssemblerModule.UnitAssemblerModuleBuild;
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
            for (int tier = 0; tier <= byTier.keySet().stream().max(Integer::compareTo).orElse(0); tier++) {
                Seq<AssemblerUnitPlan> group = byTier.get(tier);
                if (group == null || group.isEmpty()) continue;
                final int currentTier = tier;
                table.table(Tex.pane, t ->
                        t.add(Core.bundle.format("flexassembler.tier.stat", currentTier)).pad(5).left().growX()
                ).growX().pad(5).row();
                for (AssemblerUnitPlan plan : group) {
                    table.table(Tex.pane, t -> {
                        if (plan.unit.isBanned()) {
                            t.image(Icon.cancel).color(Pal.remove).size(40).pad(10);
                            return;
                        }
                        if (plan.unit.unlockedNow()) {
                            t.image(plan.unit.uiIcon).scaling(Scaling.fit).size(40).pad(10f).left();
                            t.table(info -> {
                                info.left();
                                info.add(plan.unit.localizedName).left();
                                info.row();
                                info.add(Strings.autoFixed(plan.time / 60f, 1) + " " + Core.bundle.get("unit.seconds")).color(Color.lightGray).left();
                                if (tierRequired.getOrDefault(plan, 0) > 0) {
                                    info.row();
                                    info.add(Core.bundle.format("flexassembler.tier.stat", tierRequired.get(plan))).color(Color.lightGray).left();
                                }
                                info.row();
                                info.add(Core.bundle.format("flexassembler.area.stat", planAreaMap.getOrDefault(plan, areaSize))).color(Color.lightGray).left();
                            }).left();
                            t.table(req -> {
                                for (int i = 0; i < plan.requirements.size; i++) {
                                    if (i % 4 == 0) req.row();
                                    req.add(StatValues.stack(plan.requirements.get(i))).pad(5);
                                }
                            }).right();
                        } else {
                            t.image(Icon.lock).color(Pal.darkerGray).size(40).pad(10);
                        }
                    }).growX().pad(5).row();
                }
            }
        });
    }

    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        private static final int NO_PLAN = -1;
        private int lockedIndex = NO_PLAN;   // 用户锁定的计划索引
        // 根据索引获取锁定计划
        private AssemblerUnitPlan getLockedPlan() {
            if (lockedIndex >= 0 && lockedIndex < plans.size) {
                return plans.get(lockedIndex);
            }
            return null;
        }
        // 同步装配区域大小
        private void syncArea() {
            AssemblerUnitPlan plan = getLockedPlan();
            if (plan != null) {
                areaSize = planAreaMap.getOrDefault(plan, areaSize);
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
            modules.clear();
            for (Building other : proximity) {
                if (other instanceof UnitAssemblerModuleBuild mod) modules.add(mod);
            }
            checkTier();
        }
        // ---------- 客户端 UI ----------
        @Override
        public void buildConfiguration(Table table) {
            if (Core.app.isHeadless()) return;
            AssemblerUnitPlan current = getLockedPlan();
            boolean locked = current != null;
            // 筛选当前模块等级下可用的配方
            Seq<AssemblerUnitPlan> available = new Seq<>();
            for (AssemblerUnitPlan plan : plans) {
                if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                    available.add(plan);
                }
            }
            if (available.isEmpty()) {
                table.label(() -> Core.bundle.get("flexassembler.no-plans")).pad(10);
                if (locked) {
                    table.row();
                    table.label(() -> Core.bundle.format("flexassembler.tier-low", current.unit.localizedName, tierRequired.getOrDefault(current, 0)))
                            .color(Pal.remove).padTop(4).row();
                    table.button(Core.bundle.get("flexassembler.deselect"), () -> unlock());
                }
                return;
            }
            if (locked) {
                table.label(() -> Core.bundle.format("flexassembler.producing", current.unit.localizedName))
                        .padBottom(4).row();
            } else {
                table.label(() -> Core.bundle.get("flexassembler.select-unit")).padBottom(4).color(Color.gray).row();
            }
            Table grid = new Table();
            int cols = 4;
            for (int i = 0; i < available.size; i++) {
                if (i % cols == 0 && i != 0) grid.row();
                AssemblerUnitPlan plan = available.get(i);
                boolean isChosen = locked && current == plan;
                int planIndex = plans.indexOf(plan);   // 全局索引
                Button btn = new Button(Tex.button);
                btn.table(inner -> {
                    inner.image(plan.unit.uiIcon).size(30f).padBottom(4f);
                    inner.row();
                    inner.add(plan.unit.localizedName).color(isChosen ? Pal.accent : Color.lightGray);
                }).pad(8);
                btn.clicked(() -> lock(planIndex));
                grid.add(btn).size(80f, 80f).pad(4f);
            }
            ScrollPane pane = new ScrollPane(grid);
            table.add(pane).grow().maxHeight(400f).row();
            if (locked) {
                table.row();
                table.button(Core.bundle.get("flexassembler.deselect"), () -> unlock());
            }
        }
        // 锁定某个配方
        private void lock(int index) {
            if (index >= 0 && index < plans.size) {
                lockedIndex = index;
                configure(lockedIndex);
                syncArea();
            }
        }
        // 取消锁定
        private void unlock() {
            lockedIndex = NO_PLAN;
            configure(NO_PLAN);
            syncArea();
        }
        // ---------- 配置序列化 ----------
        @Override
        public Object config() {
            return lockedIndex;
        }
        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                int val = (Integer) value;
                // 仅允许以下情况修改 lockedIndex：
                // 1. 手动取消 (val == NO_PLAN)
                // 2. 当前未锁定且 val 有效
                if (val == NO_PLAN) {
                    lockedIndex = NO_PLAN;
                } else if (lockedIndex == NO_PLAN && val >= 0 && val < plans.size) {
                    lockedIndex = val;
                }
                // 其他任何情况（例如原版试图自动切换）均被忽略，并强制发回当前锁定值
                else {
                    super.configure(lockedIndex);
                    return;
                }
            }
            super.configure(value);
            syncArea();
        }
        // ---------- 生产核心 ----------
        @Override
        public AssemblerUnitPlan plan() {
            AssemblerUnitPlan locked = getLockedPlan();
            return locked != null ? locked : (plans.isEmpty() ? super.plan() : plans.get(0));
        }
        @Override
        public boolean shouldConsume() {
            AssemblerUnitPlan locked = getLockedPlan();
            if (locked == null) return false;
            int reqTier = tierRequired.getOrDefault(locked, 0);
            if (reqTier > currentTier) return false;
            return super.shouldConsume();
        }
        @Override
        public void updateTile() {
            syncArea();
            super.updateTile();
        }
        @Override
        public Vec2 getUnitSpawn() {
            float len = tilesize * (areaSize + block.size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }
        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(lockedIndex);
            write.i(areaSize);
        }
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            lockedIndex = read.i();
            areaSize = read.i();
        }
    }
}