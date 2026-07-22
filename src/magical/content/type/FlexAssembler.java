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

    // ✅ setStats：展览信息
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
        public int myAreaSize = areaSize; // 视觉与生产点面积，不影响模块

        // 初始默认面积跟随第一个可用配方（仅视觉）
        @Override
        public void created() {
            super.created();
            if (!selected) {
                AssemblerUnitPlan defaultPlan = getDefaultPlan();
                if (defaultPlan != null) myAreaSize = planAreaMap.getOrDefault(defaultPlan, areaSize);
            }
        }

        // 获取当前tier下第一个可用配方
        private AssemblerUnitPlan getDefaultPlan() {
            for (AssemblerUnitPlan plan : plans) {
                if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                    return plan;
                }
            }
            return plans.first(); // fallback
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
                    myAreaSize = planAreaMap.getOrDefault(plan, areaSize); // 仅更新动态面积，不修改areaSize
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
                    // 恢复默认视觉面积
                    AssemblerUnitPlan defaultPlan = getDefaultPlan();
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
                AssemblerUnitPlan defaultPlan = getDefaultPlan();
                myAreaSize = defaultPlan != null ? planAreaMap.getOrDefault(defaultPlan, areaSize) : areaSize;
            }
            super.configure(value);
        }

        // ---------- 生产核心 ----------
        @Override
        public AssemblerUnitPlan plan() {
            if (selected && chosenPlan != null) return chosenPlan;
            // 未选择时使用默认配方（保持与原版兼容）
            return getDefaultPlan() != null ? getDefaultPlan() : super.plan();
        }

        @Override
        public void updateTile() {
            // 模块降级保护（仅当模块确实减少时）
            if (selected && chosenPlan != null) {
                if (tierRequired.getOrDefault(chosenPlan, 0) > currentTier) {
                    selected = false;
                    chosenPlan = null;
                    AssemblerUnitPlan defaultPlan = getDefaultPlan();
                    myAreaSize = defaultPlan != null ? planAreaMap.getOrDefault(defaultPlan, areaSize) : areaSize;
                }
            }

            // 调用原版更新，areaSize 保持原始值，确保模块连接稳定
            super.updateTile();
        }

        // ---------- 绘制重写：使用 myAreaSize 临时替换 areaSize 以获得自定义方框 ----------
        @Override
        public void draw() {
            int prevArea = areaSize;
            areaSize = myAreaSize;        // 仅绘制期间修改
            super.draw();
            areaSize = prevArea;          // 立即恢复
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
            // 生成点使用动态面积，不影响模块检测
            float len = tilesize * (myAreaSize + block.size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }

        // getRect 不重写，使用原始 areaSize，保证模块连接检测正确

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