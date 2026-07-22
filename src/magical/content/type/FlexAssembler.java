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

    // ✅ setStats：显示配方详情
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

    // ===================== 建筑实例 =====================
    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        public boolean selected = false;
        public AssemblerUnitPlan chosenPlan;
        public int myAreaSize = areaSize;

        // 初始自动设置面积为第一个可用配方的值
        @Override
        public void created() {
            super.created();
            if (!selected) {
                AssemblerUnitPlan defaultPlan = null;
                for (AssemblerUnitPlan plan : plans) {
                    if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                        defaultPlan = plan;
                        break;
                    }
                }
                if (defaultPlan != null) {
                    myAreaSize = planAreaMap.getOrDefault(defaultPlan, areaSize);
                }
            }
        }

        // UI：始终显示单位网格，高亮选中
        @Override
        public void buildConfiguration(Table table) {
            Seq<AssemblerUnitPlan> available = new Seq<>();
            for (AssemblerUnitPlan plan : plans) {
                if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                    available.add(plan);
                }
            }

            if (available.isEmpty()) {
                table.label(() -> Core.bundle.get("flexassembler.no-plans")).pad(10);
                return;
            }

            if (chosenPlan != null) {
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
                    inner.image(plan.unit.uiIcon).size(40f).padBottom(4f);
                    inner.row();
                    inner.add(plan.unit.localizedName).color(isChosen ? Pal.accent : Color.lightGray);
                }).pad(8);

                btn.clicked(() -> {
                    chosenPlan = plan;
                    selected = true;
                    myAreaSize = planAreaMap.getOrDefault(plan, areaSize);
                    configure(plan.unit.id);
                    table.clear();
                    buildConfiguration(table);
                });
                grid.add(btn).size(90f, 90f).pad(4f);
            }

            ScrollPane pane = new ScrollPane(grid);
            table.add(pane).grow().maxHeight(400f).row();

            if (chosenPlan != null) {
                table.row();
                table.button(Core.bundle.get("flexassembler.deselect"), () -> {
                    selected = false;
                    chosenPlan = null;
                    // 取消选择后，恢复为第一个可用配方的面积（而不是硬编码的areasi在）
                    AssemblerUnitPlan defaultPlan = null;
                    for (AssemblerUnitPlan plan : plans) {
                        if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                            defaultPlan = plan;
                            break;
                        }
                    }
                    myAreaSize = defaultPlan != null ? planAreaMap.getOrDefault(defaultPlan, areaSize) : areaSize;
                    configure(null);
                    table.clear();
                    buildConfiguration(table);
                }).size(120f, 40f).padTop(8).row();
            }
        }

        // ---------- 配置序列化 ----------
        @Override
        public Object config() {
            return (selected && chosenPlan != null) ? chosenPlan.unit.id : null;
        }

        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                UnitType type = content.getByID(ContentType.unit, (Integer) value);
                if (type != null) {
                    for (AssemblerUnitPlan p : plans) {
                        if (p.unit == type) {
                            chosenPlan = p;
                            selected = true;
                            myAreaSize = planAreaMap.getOrDefault(p, areaSize);
                            break;
                        }
                    }
                }
            } else if (value == null) {
                selected = false;
                chosenPlan = null;
                // 取消选择后使用默认面积（第一个可用配方）
                AssemblerUnitPlan defaultPlan = null;
                for (AssemblerUnitPlan plan : plans) {
                    if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                        defaultPlan = plan;
                        break;
                    }
                }
                myAreaSize = defaultPlan != null ? planAreaMap.getOrDefault(defaultPlan, areaSize) : areaSize;
            }
            super.configure(value);
        }

        // ---------- 生产核心 ----------
        @Override
        public AssemblerUnitPlan plan() {
            if (selected && chosenPlan != null) return chosenPlan;
            // 未选择时使用第一个可用配方（与原版自动选择逻辑一致）
            for (AssemblerUnitPlan plan : plans) {
                if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                    return plan;
                }
            }
            return super.plan();
        }

        @Override
        public void updateTile() {
            // 模块降级保护
            if (selected && chosenPlan != null) {
                if (tierRequired.getOrDefault(chosenPlan, 0) > currentTier) {
                    selected = false;
                    chosenPlan = null;
                    // 降级后自动使用当前tier下第一个配方
                    AssemblerUnitPlan defaultPlan = null;
                    for (AssemblerUnitPlan plan : plans) {
                        if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                            defaultPlan = plan;
                            break;
                        }
                    }
                    myAreaSize = defaultPlan != null ? planAreaMap.getOrDefault(defaultPlan, areaSize) : areaSize;
                }
            }

            // 动态设置面积，保证无人机、生产逻辑使用正确值
            AssemblerUnitPlan plan = plan();
            myAreaSize = planAreaMap.getOrDefault(plan, areaSize);
            int prevArea = areaSize;
            areaSize = myAreaSize;
            super.updateTile();
            areaSize = prevArea;
        }

        // ---------- 绘制重写：临时替换 areaSize，使所有视觉基于 myAreaSize ----------
        @Override
        public void draw() {
            int prevArea = areaSize;
            areaSize = myAreaSize;
            super.draw();
            areaSize = prevArea;
        }

        @Override
        public void drawSelect() {
            int prevArea = areaSize;
            areaSize = myAreaSize;
            super.drawSelect();
            areaSize = prevArea;
        }

        @Override
        public Vec2 getUnitSpawn() {
            float len = tilesize * (myAreaSize + block.size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }

        // ---------- 存档 ----------
        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(selected);
            if (selected && chosenPlan != null) write.i(chosenPlan.unit.id);
            write.i(myAreaSize);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            selected = read.bool();
            if (selected) {
                int id = read.i();
                UnitType type = content.getByID(ContentType.unit, id);
                if (type != null) {
                    for (AssemblerUnitPlan p : plans) {
                        if (p.unit == type) {
                            chosenPlan = p;
                            break;
                        }
                    }
                }
                if (chosenPlan == null) selected = false;
            }
            myAreaSize = read.i();
            if (!selected) myAreaSize = areaSize;
        }
    }
}