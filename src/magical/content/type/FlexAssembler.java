package magical.content;

import arc.graphics.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.ai.types.*;
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

    public Map<AssemblerUnitPlan, Integer> tierRequired = new HashMap<>();
    public Map<AssemblerUnitPlan, Integer> planAreaMap = new HashMap<>();

    public FlexAssembler(String name) {
        super(name);
    }

    /**
     * 添加配方。
     * @param groupLabel    分组标签（如 "T1"），相同标签的配方会放在同一个标签页下
     * @param output        输出单位
     * @param time          耗时（秒）
     * @param customArea    采摘区域大小（格）
     * @param requiredTier  需要的最低模块数量
     * @param requirements  载荷需求
     */
    public void addPlan(String groupLabel, UnitType output, float time, int customArea, int requiredTier, PayloadStack... requirements) {
        Seq<PayloadStack> reqSeq = new Seq<>(requirements);
        AssemblerUnitPlan plan = new AssemblerUnitPlan(output, time, reqSeq);
        plans.add(plan);
        tierRequired.put(plan, requiredTier);
        planAreaMap.put(plan, customArea);
    }

    // ==================== 建筑实例 ====================
    public class FlexAssemblerBuild extends UnitAssemblerBuild {
        public boolean selected = false;
        public AssemblerUnitPlan chosenPlan;
        public int myAreaSize = areaSize;   // 实例专属区域大小

        // ---------- 配置 UI ----------
        @Override
        public void buildConfiguration(Table table) {
            if (!selected) {
                // 收集当前模块等级可用的配方，按 groupLabel 分组
                Map<String, Seq<AssemblerUnitPlan>> grouped = new LinkedHashMap<>();
                for (AssemblerUnitPlan plan : plans) {
                    if (tierRequired.getOrDefault(plan, 0) <= currentTier) {
                        // 使用计划对应的标签（如果未指定，用默认的 "T"+tier）
                        String label = plan.unit.localizedName; // 简单起见，直接用单位名作为标签
                        // 如果你希望按传入的 groupLabel 分组，可以把 groupLabel 存入一个 map，然后从那里获取。
                        // 这里提供一个简化版本：统一显示所有可用单位，不分组（保证能点击）
                        // 如果你确实需要分组，请取消下面注释并传入正确的 groupLabel
                        /*
                        String group = planLabelMap.getOrDefault(plan, "T" + currentTier);
                        Seq<AssemblerUnitPlan> list = grouped.get(group);
                        if(list == null){
                            list = new Seq<>();
                            grouped.put(group, list);
                        }
                        list.add(plan);
                        */
                        // 临时：不分组，直接添加到一个默认列表
                        Seq<AssemblerUnitPlan> list = grouped.get("Available");
                        if(list == null){
                            list = new Seq<>();
                            grouped.put("Available", list);
                        }
                        list.add(plan);
                    }
                }

                if (grouped.isEmpty()) {
                    table.label(() -> "No plans (need more modules)").pad(10);
                    return;
                }

                // 直接显示所有可用单位的图标（无标签页）
                Table grid = new Table();
                int cols = 4;
                int idx = 0;
                for (Seq<AssemblerUnitPlan> list : grouped.values()) {
                    for (AssemblerUnitPlan plan : list) {
                        if (idx % cols == 0 && idx != 0) grid.row();
                        ImageButton btn = new ImageButton(plan.unit.uiIcon, Styles.cleari);
                        btn.resizeImage(40f);
                        btn.clicked(() -> choosePlan(plan));
                        btn.row();
                        btn.add(plan.unit.localizedName).color(Color.lightGray);
                        grid.add(btn).pad(4);
                        idx++;
                    }
                }

                ScrollPane pane = new ScrollPane(grid);
                table.add(pane).grow().maxHeight(400f).row();
                table.label(() -> "Select a unit to produce").padTop(4).color(Color.gray);
            } else {
                // 已选择
                table.label(() -> "Producing: " + chosenPlan.unit.localizedName).padBottom(4).row();
                table.button("Change", () -> {
                    selected = false;
                    chosenPlan = null;
                    myAreaSize = areaSize;   // 恢复默认面积
                }).size(100f, 40f).row();
            }
        }

        /** 选定配方 */
        public void choosePlan(AssemblerUnitPlan plan) {
            chosenPlan = plan;
            selected = true;
            myAreaSize = planAreaMap.getOrDefault(plan, areaSize);
            configure(plan.unit.id);   // 保存到 config
        }

        // ---------- 序列化 ----------
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
            }
            super.configure(value);
        }

        // ---------- 生产核心 ----------
        @Override
        public AssemblerUnitPlan plan() {
            if (selected && chosenPlan != null) return chosenPlan;
            return super.plan();
        }

        // 重写 findPlan（部分旧版本可能用此方法检查计划）
        public AssemblerUnitPlan findPlan() {
            if (selected && chosenPlan != null) return chosenPlan;
            return super.plan(); // 与原版保持一致
        }

        // ---------- 区域绘制 ----------
        @Override
        public void drawSelect() {
            float fulls = myAreaSize * tilesize / 2f;
            Vec2 spawn = getUnitSpawn();
            Drawf.dashRect(Pal.accent, Tmp.r1.set(spawn.x - fulls, spawn.y - fulls, fulls * 2f, fulls * 2f));
        }

        @Override
        public Vec2 getUnitSpawn() {
            float len = tilesize * (myAreaSize + block.size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }

        // 覆盖更新逻辑，使用实例专属面积
        @Override
        public void updateTile() {
            // 以下复制自 UnitAssemblerBuild.updateTile()，将所有 areaSize 替换为 myAreaSize
            // 并插入 selected/chosenPlan 检查
            if(!readUnits.isEmpty()){
                units.clear();
                readUnits.each(i -> {
                    var unit = Groups.unit.getByID(i);
                    if(unit != null){
                        units.add(unit);
                    }
                });
                readUnits.clear();
            }

            if(lastTier != currentTier){
                if(lastTier >= 0f) progress = 0f;
                lastTier = lastTier == -2 ? -1 : currentTier;
            }

            if(units.size < dronesCreated && whenSyncedUnits.size > 0){
                whenSyncedUnits.each(id -> {
                    var unit = Groups.unit.getByID(id);
                    if(unit != null){
                        units.addUnique(unit);
                    }
                });
            }

            units.removeAll(u -> !u.isAdded() || u.dead || !(u.controller() instanceof AssemblerAI));

            if(!allowUpdate()){
                progress = 0f;
                units.each(Unit::kill);
                units.clear();
            }

            float powerStatus = !enabled ? 0f : power == null ? 1f : power.status;
            powerWarmup = Mathf.lerpDelta(powerStatus, powerStatus > 0.0001f ? 1f : 0f, 0.1f);
            droneWarmup = Mathf.lerpDelta(droneWarmup, units.size < dronesCreated ? powerStatus : 0f, 0.1f);
            totalDroneProgress += droneWarmup * delta();

            if(units.size < dronesCreated && enabled && (droneProgress += delta() * state.rules.unitBuildSpeed(team) * powerStatus / droneConstructTime) >= 1f){
                if(!net.client()){
                    var unit = droneType.create(team);
                    if(unit instanceof BuildingTetherc bt){
                        bt.building(this);
                    }
                    unit.set(x, y);
                    unit.rotation = 90f;
                    unit.add();
                    units.add(unit);
                    Call.assemblerDroneSpawned(tile, unit.id);
                }
            }

            if(units.size >= dronesCreated){
                droneProgress = 0f;
            }

            Vec2 spawn = getUnitSpawn();

            if(moveInPayload() && !wasOccupied){
                yeetPayload(payload);
                payload = null;
            }

            // 无人机位置计算（使用 myAreaSize）
            for(int i = 0; i < units.size; i++){
                var unit = units.get(i);
                var ai = (AssemblerAI)unit.controller();
                ai.targetPos.trns(i * 90f + 45f, myAreaSize / 2f * Mathf.sqrt2 * tilesize).add(spawn);
                ai.targetAngle = i * 90f + 45f + 180f;
            }

            wasOccupied = checkSolid(spawn, false);
            boolean visualOccupied = checkSolid(spawn, true);
            float eff = (units.count(u -> ((AssemblerAI)u.controller()).inPosition()) / (float)dronesCreated);

            sameTypeWarmup = Mathf.lerpDelta(sameTypeWarmup, wasOccupied && !visualOccupied ? 0f : 1f, 0.1f);
            invalidWarmup = Mathf.lerpDelta(invalidWarmup, visualOccupied ? 1f : 0f, 0.1f);

            var plan = plan(); // 使用我们重写的 plan()

            // 生产检查与进度
            if(!wasOccupied && efficiency > 0 && Units.canCreate(team, plan.unit)){
                warmup = Mathf.lerpDelta(warmup, efficiency, 0.1f);
                if((progress += edelta() * state.rules.unitBuildSpeed(team) * eff / plan.time) >= 1f){
                    Call.assemblerUnitSpawned(tile);
                }
            }else{
                warmup = Mathf.lerpDelta(warmup, 0f, 0.1f);
            }

            // 如果模块等级下降导致配方不可用，重置
            if(selected && chosenPlan != null && tierRequired.getOrDefault(chosenPlan, 0) > currentTier){
                selected = false;
                chosenPlan = null;
                myAreaSize = areaSize;
            }
        }

        // 覆盖 checkSolid 以使用 myAreaSize 判断
        public boolean checkSolid(Vec2 v, boolean same){
            var output = unit();
            float hsize = output.hitSize * 1.4f;
            return ((!output.flying && collisions.overlapsTile(Tmp.r1.setCentered(v.x, v.y, output.hitSize), EntityCollisions::solid)) ||
                    Units.anyEntities(v.x - hsize/2f, v.y - hsize/2f, hsize, hsize, u -> (!same || u.type != output) && !u.spawnedByCore &&
                            ((u.type.allowLegStep && output.allowLegStep) || (output.flying && u.isFlying()) || (!output.flying && u.isGrounded()))));
        }

        // 存档
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