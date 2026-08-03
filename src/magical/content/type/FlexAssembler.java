package magical.content;

import arc.Core;
import arc.graphics.*;
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
        public boolean selected = false;
        public AssemblerUnitPlan chosenPlan;

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
            if (!selected) {
                AssemblerUnitPlan defaultPlan = getDefaultPlan();
                if (defaultPlan != null) syncArea(defaultPlan);
            }
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

        // UI 界面（仅客户端调用，无服务端影响）
        @Override
        public void buildConfiguration(Table table) {
            // 安全措施：若非客户端环境则直接返回（但 buildConfiguration 本身只会在客户端调用）
            if (Vars.headless) return;

            Seq<AssemblerUnitPlan> available = new Seq<>();
            for (AssemblerUnitPlan plan : plans) {
                if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                    available.add(plan);
                }
            }

            if (available.isEmpty()) {
                table.label(() -> Core.bundle.get("flexassembler.no-plans")).pad(10);
                if (chosenPlan != null) {
                    table.row();
                    table.label(() -> Core.bundle.format("flexassembler.tier-low", chosenPlan.unit.localizedName, tierRequired.get(chosenPlan)))
                            .color(Pal.remove).padTop(4).row();
                    table.button(Core.bundle.get("flexassembler.deselect"), () -> {
                        selected = false;
                        chosenPlan = null;
                        AssemblerUnitPlan def = getDefaultPlan();
                        if (def != null) syncArea(def);
                        else areaSize = FlexAssembler.this.areaSize;
                        configure(null);
                        table.clear();
                        buildConfiguration(table);
                    }).size(120f, 40f).padTop(8).row();
                }
                return;
            }

            boolean chosenAvailable = chosenPlan != null && available.contains(chosenPlan);

            if (!chosenAvailable && chosenPlan != null) {
                table.label(() -> Core.bundle.format("flexassembler.tier-low", chosenPlan.unit.localizedName, tierRequired.get(chosenPlan)))
                        .padBottom(4).color(Pal.remove).row();
            } else if (chosenPlan != null) {
                table.label(() -> Core.bundle.format("flexassembler.producing", chosenPlan.unit.localizedName))
                        .padBottom(4).row();
            } else {
                table.label(() -> Core.bundle.get("flexassembler.select-unit")).padBottom(4).color(Color.gray).row();
            }

            Table grid = new Table();
            int cols = 4;
            for (int i = 0; i < available.size; i++) {
                if (i % cols == 0 && i != 0) grid.row();
                AssemblerUnitPlan plan = available.get(i);
                boolean isChosen = Objects.equals(chosenPlan, plan);

                Button btn = new Button(Tex.button);
                btn.table(inner -> {
                    inner.image(plan.unit.uiIcon).size(30f).padBottom(4f);
                    inner.row();
                    inner.add(plan.unit.localizedName).color(isChosen ? Pal.accent : Color.lightGray);
                }).pad(8);

                btn.clicked(() -> {
                    // 只调用 configure，不手动重建 UI，让游戏自动刷新配置面板
                    configure(plan.unit.id);
                });
                grid.add(btn).size(80f, 80f).pad(4f);
            }

            ScrollPane pane = new ScrollPane(grid);
            table.add(pane).grow().maxHeight(400f).row();

            if (chosenPlan != null) {
                table.row();
                table.button(Core.bundle.get("flexassembler.deselect"), () -> {
                    configure(null);
                }).size(120f, 40f).padTop(8).row();
            }
        }

        @Override
        public Object config() {
            return chosenPlan != null ? chosenPlan.unit.id : null;
        }

        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                UnitType type = content.getByID(ContentType.unit, (Integer) value);
                if (type != null) {
                    AssemblerUnitPlan found = null;
                    for (AssemblerUnitPlan p : plans) {
                        if (p.unit == type) {
                            found = p;
                            break;
                        }
                    }
                    if (found != null) {
                        chosenPlan = found;
                        selected = true;
                        syncArea(found);
                    } else {
                        // 配方无效，重置
                        selected = false;
                        chosenPlan = null;
                        syncArea(getDefaultPlan());
                    }
                } else {
                    selected = false;
                    chosenPlan = null;
                    syncArea(getDefaultPlan());
                }
            } else if (value == null) {
                selected = false;
                chosenPlan = null;
                syncArea(getDefaultPlan());
            }
            // 必须调用父类方法触发网络同步
            super.configure(value);
        }

        @Override
        public AssemblerUnitPlan plan() {
            if (selected && chosenPlan != null) return chosenPlan;
            AssemblerUnitPlan def = getDefaultPlan();
            return def != null ? def : (plans.isEmpty() ? super.plan() : plans.first());
        }

        @Override
        public void updateTile() {
            // 同步面积并调用原版更新
            AssemblerUnitPlan current = plan();
            if (current != null) syncArea(current);
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
            write.bool(selected);
            if (chosenPlan != null) write.i(chosenPlan.unit.id);
            write.i(areaSize);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            selected = read.bool();
            if (selected) {
                int id = read.i();
                UnitType type = content.getByID(ContentType.unit, id);
                if (type != null) {
                    AssemblerUnitPlan found = null;
                    for (AssemblerUnitPlan p : plans) {
                        if (p.unit == type) {
                            found = p;
                            break;
                        }
                    }
                    chosenPlan = found;
                    if (found == null) selected = false;
                } else {
                    selected = false;
                }
            }
            areaSize = read.i();
            if (!selected) {
                chosenPlan = null;
                syncArea(getDefaultPlan());
            }
        }
    }
}