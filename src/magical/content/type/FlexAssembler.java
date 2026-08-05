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

        private AssemblerUnitPlan lockedPlan;   // 手动锁定的计划，null 表示自动模式
        private boolean locked = false;         // 是否处于锁定状态

        // 用于标识本次 configure 是由玩家手动触发的，允许接受
        private transient boolean manualConfig = false;

        private void syncArea(AssemblerUnitPlan plan) {
            if (plan != null) {
                areaSize = planAreaMap.getOrDefault(plan, areaSize);
            }
        }

        @Override
        public void created() {
            super.created();
            syncArea(plan());
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            modules.clear();
            for (Building other : proximity) {
                if (other instanceof UnitAssemblerModuleBuild mod) {
                    modules.add(mod);
                }
            }
            checkTier();
        }

        @Override
        public void buildConfiguration(Table table) {
            if (Vars.headless) return;

            // 当前实际生效的计划
            final AssemblerUnitPlan currentPlan = locked ? lockedPlan : super.plan();

            // 显示当前计划（未锁定时可显示自动选择的单位）
            if (!locked) {
                table.label(() -> Core.bundle.get("flexassembler.auto-mode") + " " + currentPlan.unit.localizedName)
                        .color(Color.gray).padBottom(4).row();
            } else {
                table.label(() -> Core.bundle.format("flexassembler.producing", currentPlan.unit.localizedName))
                        .color(Pal.accent).padBottom(4).row();
            }

            // 构建配方网格（显示所有符合当前模块等级的配方）
            Table grid = new Table();
            int count = 0;
            for (AssemblerUnitPlan plan : plans) {
                if (tierRequired.getOrDefault(plan, 0) > currentTier) continue;
                if (count % 4 == 0) grid.row();
                boolean isChosen = (locked && plan == lockedPlan) || (!locked && plan == currentPlan);
                Button btn = new Button(Tex.button);
                btn.table(t -> {
                    t.image(plan.unit.uiIcon).size(30f).padBottom(4f);
                    t.row();
                    t.add(plan.unit.localizedName).color(isChosen ? Pal.accent : Color.lightGray);
                }).pad(8);
                btn.clicked(() -> {
                    // 手动锁定 – 无论之前是什么状态，现在都会锁定到这个计划
                    lockedPlan = plan;
                    locked = true;
                    manualConfig = true;        // 允许本次配置
                    configure(plans.indexOf(plan));
                    syncArea(plan);
                });
                grid.add(btn).size(80f, 80f).pad(4f);
                count++;
            }

            ScrollPane pane = new ScrollPane(grid);
            table.add(pane).grow().maxHeight(400f).row();

            // 如果当前已锁定，显示“取消选择”按钮
            if (locked) {
                table.row();
                table.button(Core.bundle.get("flexassembler.deselect"), () -> {
                    // 取消锁定，回到自动模式
                    locked = false;
                    lockedPlan = null;
                    manualConfig = true;
                    configure(NO_PLAN);
                    syncArea(super.plan());
                }).size(120f, 40f).padTop(8).row();
            }
        }

        @Override
        public Object config() {
            if (locked && lockedPlan != null) {
                return plans.indexOf(lockedPlan);
            }
            return NO_PLAN;  // 未锁定返回 -1
        }

        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                int idx = (Integer) value;
                if (manualConfig) {
                    // 由玩家手动触发，接受配置
                    if (idx == NO_PLAN) {
                        locked = false;
                        lockedPlan = null;
                    } else if (idx >= 0 && idx < plans.size) {
                        lockedPlan = plans.get(idx);
                        locked = true;
                    }
                    manualConfig = false;
                } else {
                    // 非手动触发（服务端自动同步），如果当前是锁定状态则**忽略**
                    if (locked && idx != (lockedPlan == null ? NO_PLAN : plans.indexOf(lockedPlan))) {
                        // 静默忽略，不调用 super.configure，避免冲突
                        return;
                    }
                }
                // 无论是否手动，都继续调用父类完成网络同步
                super.configure(value);
            } else {
                super.configure(value);
            }
        }

        @Override
        public AssemblerUnitPlan plan() {
            if (locked && lockedPlan != null) {
                return lockedPlan;
            }
            // 自动模式 – 使用原版逻辑
            return super.plan();
        }

        @Override
        public void updateTile() {
            float savedProgress = progress;          // 备份当前进度
            super.updateTile();                      // 原版逻辑（可能会因为模块变化清零进度）
            if (progress < savedProgress && savedProgress > 0f) {
                progress = savedProgress;            // 强制恢复，实现无缝衔接
            }

            // 同步装配面积
            syncArea(plan());
        }

        // ---------- 其他辅助方法 ----------
        @Override
        public Vec2 getUnitSpawn() {
            float len = tilesize * (areaSize + block.size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(locked);
            write.i(lockedPlan == null ? NO_PLAN : plans.indexOf(lockedPlan));
            write.i(areaSize);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            locked = read.bool();
            int index = read.i();
            if (locked && index >= 0 && index < plans.size) {
                lockedPlan = plans.get(index);
            } else {
                locked = false;
                lockedPlan = null;
            }
            areaSize = read.i();
            syncArea(plan());
        }
    }
}