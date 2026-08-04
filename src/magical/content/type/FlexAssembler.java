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
        private int lockedIndex = NO_PLAN;   // 用户在 UI 中选择的计划索引
        private AssemblerUnitPlan lockedPlan; // 对应的计划对象 (用于快速访问)
        // 更新 lockedPlan
        private void refreshLockedPlan() {
            if (lockedIndex >= 0 && lockedIndex < plans.size) {
                lockedPlan = plans.get(lockedIndex);
            } else {
                lockedPlan = null;
            }
        }
        // 获取当前应使用的计划（用户锁定优先，否则返回 null 表示不生产）
        private AssemblerUnitPlan getActivePlan() {
            return lockedPlan;
        }
        // 同步面积
        private void syncArea() {
            if (lockedPlan != null) {
                areaSize = planAreaMap.getOrDefault(lockedPlan, areaSize);
            }
        }
        @Override
        public void created() {
            super.created();
            refreshLockedPlan();
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
        @Override
        public void buildConfiguration(Table table) {
            if (Core.app.isHeadless()) return;
            refreshLockedPlan();
            AssemblerUnitPlan current = lockedPlan;
            boolean locked = current != null;
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
                    table.button(Core.bundle.get("flexassembler.deselect"), () -> {
                        lockedIndex = NO_PLAN;
                        refreshLockedPlan();
                        configure(NO_PLAN);
                        syncArea();
                    });
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
                int indexInPlans = plans.indexOf(plan);
                Button btn = new Button(Tex.button);
                btn.table(inner -> {
                    inner.image(plan.unit.uiIcon).size(30f).padBottom(4f);
                    inner.row();
                    inner.add(plan.unit.localizedName).color(isChosen ? Pal.accent : Color.lightGray);
                }).pad(8);
                btn.clicked(() -> {
                    lockedIndex = indexInPlans;
                    refreshLockedPlan();
                    configure(lockedIndex);
                    syncArea();
                });
                grid.add(btn).size(80f, 80f).pad(4f);
            }
            ScrollPane pane = new ScrollPane(grid);
            table.add(pane).grow().maxHeight(400f).row();
            if (locked) {
                table.row();
                table.button(Core.bundle.get("flexassembler.deselect"), () -> {
                    lockedIndex = NO_PLAN;
                    refreshLockedPlan();
                    configure(NO_PLAN);
                    syncArea();
                });
            }
        }
        @Override
        public Object config() {
            return lockedIndex;
        }
        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                int val = (Integer) value;
                if (val == NO_PLAN) {
                    lockedIndex = NO_PLAN;
                    refreshLockedPlan();
                } else if (val >= 0 && val < plans.size) {
                    // 接受用户选择，也接受服务端发来的相同值，但绝不响应自动切换
                    if (lockedIndex == val || lockedIndex == NO_PLAN) {
                        lockedIndex = val;
                        refreshLockedPlan();
                    } else {
                        // 锁已存在且值不同 → 忽略，并将正确值发回（但不要自己调用 configure 递归）
                        super.configure(lockedIndex);
                        return;
                    }
                }
            }
            super.configure(value);
            syncArea();
        }
        // ---------- 强制计划 ----------
        @Override
        public AssemblerUnitPlan plan() {
            AssemblerUnitPlan active = getActivePlan();
            return active != null ? active : (plans.isEmpty() ? super.plan() : plans.get(0));
        }
        @Override
        public boolean shouldConsume() {
            if (lockedPlan == null) return false;
            int reqTier = tierRequired.getOrDefault(lockedPlan, 0);
            if (reqTier > currentTier) return false;
            return super.shouldConsume();
        }
        @Override
        public void updateTile() {
            // 处理模块等级变化（只重置进度，不改变计划）
            if (lastTier != currentTier) {
                progress = 0f;
                lastTier = currentTier;
            }
            // 面积同步
            syncArea();
            // 基本检查
            if (!allowUpdate()) {
                progress = 0f;
                units.each(Unit::kill);
                units.clear();
            }
            // 电力与无人机（直接从原版复制必要部分）
            float powerStatus = !enabled ? 0f : power == null ? 1f : power.status;
            powerWarmup = Mathf.lerpDelta(powerStatus, powerStatus > 0.0001f ? 1f : 0f, 0.1f);
            droneWarmup = Mathf.lerpDelta(droneWarmup, units.size < dronesCreated ? powerStatus : 0f, 0.1f);
            totalDroneProgress += droneWarmup * delta();
            if (units.size < dronesCreated && enabled &&
                    (droneProgress += delta() * state.rules.unitBuildSpeed(team) * powerStatus / droneConstructTime) >= 1f) {
                if (!net.client()) {
                    var unit = droneType.create(team);
                    if (unit instanceof BuildingTetherc bt) bt.building(this);
                    unit.set(x, y);
                    unit.rotation = 90f;
                    unit.add();
                    units.add(unit);
                    Call.assemblerDroneSpawned(tile, unit.id);
                }
            }
            if (units.size >= dronesCreated) {
                droneProgress = 0f;
            }
            // 无人机位置（使用当前面积）
            Vec2 spawn = getUnitSpawn();
            for (int i = 0; i < units.size; i++) {
                var unit = units.get(i);
                var ai = (mindustry.ai.types.AssemblerAI)unit.controller();
                ai.targetPos.trns(i * 90f + 45f, areaSize / 2f * Mathf.sqrt2 * tilesize).add(spawn);
                ai.targetAngle = i * 90f + 45f + 180f;
            }
            wasOccupied = checkSolid(spawn, false);
            boolean visualOccupied = checkSolid(spawn, true);
            float eff = (units.count(u -> ((mindustry.ai.types.AssemblerAI)u.controller()).inPosition()) / (float) dronesCreated);
            sameTypeWarmup = Mathf.lerpDelta(sameTypeWarmup, wasOccupied && !visualOccupied ? 0f : 1f, 0.1f);
            invalidWarmup = Mathf.lerpDelta(invalidWarmup, visualOccupied ? 1f : 0f, 0.1f);
            AssemblerUnitPlan active = getActivePlan();
            if (active != null && shouldConsume() && efficiency > 0 && Units.canCreate(team, active.unit)) {
                warmup = Mathf.lerpDelta(warmup, efficiency, 0.1f);
                if ((progress += edelta() * state.rules.unitBuildSpeed(team) * eff / active.time) >= 1f) {
                    Call.assemblerUnitSpawned(tile);
                }
            } else {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.1f);
            }
        }
        // 重写 spawned，确保产出正确的单位
        public void spawned() {
            AssemblerUnitPlan active = getActivePlan();
            if (active == null) return;
            Vec2 spawn = getUnitSpawn();
            consume();
            Unit unit = active.unit.create(team);
            unit.fell = true;
            if (unit.isCommandable() && commandPos != null) {
                unit.command().commandPosition(commandPos);
            }
            unit.set(spawn.x + Mathf.range(0.001f), spawn.y + Mathf.range(0.001f));
            unit.rotation = rotdeg();
            Building targetBuild = unit.buildOn();
            var payload = new UnitPayload(unit);
            if (targetBuild != null && targetBuild.team == team && targetBuild.acceptPayload(targetBuild, payload)) {
                targetBuild.handlePayload(targetBuild, payload);
            } else if (!net.client()) {
                unit.add();
                Units.notifyUnitSpawn(unit);
            }
            createSound.at(spawn.x, spawn.y, 1f + Mathf.range(0.06f), createSoundVolume);
            progress = 0f;
            Fx.unitAssemble.at(spawn.x, spawn.y, rotdeg() - 90f, active.unit);
            blocks.clear();
            Events.fire(new UnitCreateEvent(unit, this));
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
            refreshLockedPlan();
        }
    }
}