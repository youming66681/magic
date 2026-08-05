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
        public boolean selected = false;
        public AssemblerUnitPlan chosenPlan;
        public AssemblerUnitPlan lockedPlan;
        private void syncArea(AssemblerUnitPlan plan) {
            if (plan != null) {
                areaSize = planAreaMap.getOrDefault(plan, areaSize);
            }
        }
        private AssemblerUnitPlan getDefaultPlan() {
            for (AssemblerUnitPlan plan : plans) {
                if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                    return plan;
                }
            }
            return plans.isEmpty() ? null : plans.first();
        }
        @Override
        public void created() {
            super.created();
            // 读档时 lockedPlan 可能已从 read() 中恢复
            if (lockedPlan == null) {
                lockedPlan = getDefaultPlan();
            }
            chosenPlan = lockedPlan;
            syncArea(lockedPlan);
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
            Table grid = new Table();
            int count = 0;
            for (AssemblerUnitPlan plan : plans) {
                if (tierRequired.getOrDefault(plan, 0) > currentTier) continue;
                if (count % 4 == 0) grid.row();
                boolean checked = plan == lockedPlan;
                Button button = new Button(Tex.button);
                button.table(t -> {
                    t.image(plan.unit.uiIcon).size(36);
                    t.row();
                    t.add(plan.unit.localizedName).color(checked ? Pal.accent : Color.white);
                });
                int index = plans.indexOf(plan);
                button.clicked(() -> configure(index));
                grid.add(button).size(90, 90).pad(4);
                count++;
            }
            table.add(new ScrollPane(grid)).grow().maxHeight(400).row();
            if (lockedPlan != null) {
                table.label(() -> Core.bundle.format("flexassembler.producing", lockedPlan.unit.localizedName)).row();
            }
            // 添加取消按钮
            if (lockedPlan != null) {
                table.row();
                table.button(Core.bundle.get("flexassembler.deselect"), () -> configure(NO_PLAN)).size(120, 40).padTop(8).row();
            }
        }
        @Override
        public Object config() {
            if (lockedPlan == null) return NO_PLAN;
            return plans.indexOf(lockedPlan);
        }
        @Override
        public void configure(@Nullable Object value) {
            if (!(value instanceof Integer)) return;
            int index = (Integer) value;
            if (index == NO_PLAN) {
                // 取消锁定
                lockedPlan = null;
                chosenPlan = null;
                selected = false;
                // 重新获取默认计划以保持生产
                AssemblerUnitPlan def = getDefaultPlan();
                if (def != null) syncArea(def);
            } else if (index >= 0 && index < plans.size) {
                // 锁定新计划
                lockedPlan = plans.get(index);
                chosenPlan = lockedPlan;
                selected = true;
                syncArea(lockedPlan);
            }
            super.configure(config());
        }
        @Override
        public AssemblerUnitPlan plan() {
            if (lockedPlan != null) return lockedPlan;
            // 未锁定时，尝试维持 chosenPlan，否则使用默认
            if (chosenPlan != null) return chosenPlan;
            lockedPlan = getDefaultPlan();
            chosenPlan = lockedPlan;
            return lockedPlan;
        }
        @Override
        public void updateTile() {
            // 检测服务端是否篡改了 plan：如果 lockedPlan 存在但 super.plan() 返回的计划不同，立即重新锁定
            if (lockedPlan != null) {
                AssemblerUnitPlan currentSuperPlan = super.plan();
                if (currentSuperPlan != lockedPlan) {
                    // 服务端偷偷换了计划，强制同步回去（仅调用一次，避免递归）
                    super.configure(config());
                }
            }
            AssemblerUnitPlan p = plan();
            if (p != null) syncArea(p);
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
            write.i(lockedPlan == null ? NO_PLAN : plans.indexOf(lockedPlan));
            write.i(areaSize);
        }
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int index = read.i();
            if (index >= 0 && index < plans.size) {
                lockedPlan = plans.get(index);
                chosenPlan = lockedPlan;
                selected = true;
            } else {
                lockedPlan = getDefaultPlan();
                chosenPlan = lockedPlan;
                selected = false;
            }
            areaSize = read.i();
            syncArea(lockedPlan);
        }
    }
}