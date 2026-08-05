package magical.content;

import arc.Core;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.ai.types.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.type.PayloadStack;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.units.UnitAssemblerModule.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

public class FlexAssembler extends PayloadBlock {

    public @Load("@-side1") TextureRegion sideRegion1;
    public @Load("@-side2") TextureRegion sideRegion2;

    public int areaSize = 11;
    public UnitType droneType = UnitTypes.assemblyDrone;
    public int dronesCreated = 4;
    public float droneConstructTime = 60f * 4f;
    public int[] capacities = {};

    public Seq<AssemblerUnitPlan> plans = new Seq<>(4);
    public Map<AssemblerUnitPlan, Integer> planAreaMap = new HashMap<>();
    public Map<AssemblerUnitPlan, Integer> tierRequired = new HashMap<>();

    public Sound createSound = Sounds.unitCreateBig;
    public float createSoundVolume = 1f;

    protected @Nullable ConsumePayloadDynamic consPayload;
    protected @Nullable ConsumeItemDynamic consItem;

    public FlexAssembler(String name) {
        super(name);
        update = solid = true;
        rotate = true;
        rotateDraw = false;
        acceptsPayload = hasItems = true;
        flags = EnumSet.of(BlockFlag.unitAssembler);
        regionRotated1 = 1;
        sync = true;
        group = BlockGroup.units;
        commandable = true;
        quickRotate = false;
        ambientSound = Sounds.loopUnitBuilding;
        ambientSoundVolume = 0.13f;
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
    public void init() {
        updateClipRadius((areaSize + 1) * tilesize);
        consume(consPayload = new ConsumePayloadDynamic((FlexAssemblerBuild build) -> build.plan().requirements));
        consume(consItem = new ConsumeItemDynamic((FlexAssemblerBuild build) -> build.plan().itemReq != null ? build.plan().itemReq : ItemStack.empty));
        super.init();
        initCapacities();
    }

    private void initCapacities() {
        itemCapacity = 10;
        capacities = new int[Vars.content.items().size];
        for (AssemblerUnitPlan plan : plans) {
            if (plan.itemReq != null)
                for (ItemStack stack : plan.itemReq)
                    capacities[stack.item.id] = Math.max(capacities[stack.item.id], stack.amount * 2);
        }
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


    public static class AssemblerUnitPlan {
        public UnitType unit;
        public @Nullable Seq<PayloadStack> requirements;
        public @Nullable ItemStack[] itemReq;
        public @Nullable LiquidStack[] liquidReq;
        public float time;

        public AssemblerUnitPlan(UnitType unit, float time, Seq<PayloadStack> requirements) {
            this.unit = unit; this.time = time; this.requirements = requirements;
        }
    }

    public class FlexAssemblerBuild extends PayloadBlockBuild<Payload> {
        protected IntSeq readUnits = new IntSeq();
        protected IntSeq whenSyncedUnits = new IntSeq();

        public @Nullable Vec2 commandPos;
        public Seq<Unit> units = new Seq<>();
        public Seq<UnitAssemblerModuleBuild> modules = new Seq<>();
        public PayloadSeq blocks = new PayloadSeq();
        public float progress, warmup, droneWarmup, powerWarmup, sameTypeWarmup;
        public float invalidWarmup = 0f;
        public int currentTier = 0;
        public int lastTier = -2;
        public boolean wasOccupied = false;
        public float droneProgress, totalDroneProgress;

        private static final int NO_PLAN = -1;
        private int lockedIndex = NO_PLAN;   // 用户选择的计划索引

        private AssemblerUnitPlan getLockedPlan() {
            if (lockedIndex >= 0 && lockedIndex < plans.size) return plans.get(lockedIndex);
            return null;
        }

        // 当前实际使用的计划（优先锁定，否则根据模块等级自动选择）
        public AssemblerUnitPlan plan() {
            AssemblerUnitPlan locked = getLockedPlan();
            if (locked != null) return locked;
            return plans.get(Math.min(currentTier, plans.size - 1));
        }

        public UnitType unit() { return plan().unit; }

        private void syncArea() {
            AssemblerUnitPlan plan = plan();
            if (plan != null) areaSize = planAreaMap.getOrDefault(plan, areaSize);
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
            for (Building other : proximity)
                if (other instanceof UnitAssemblerModuleBuild mod) modules.add(mod);
            checkTier();
        }

        public void checkTier() {
            modules.sort(b -> b.tier());
            int max = 0;
            for (int i = 0; i < modules.size; i++) {
                var mod = modules.get(i);
                if (mod.tier() == max || mod.tier() == max + 1) max = mod.tier();
                else break;
            }
            currentTier = max;
        }

        @Override
        public void buildConfiguration(Table table) {
            if (Vars.headless) return;

            AssemblerUnitPlan current = getLockedPlan();
            boolean locked = current != null;

            Seq<AssemblerUnitPlan> available = new Seq<>();
            for (AssemblerUnitPlan plan : plans)
                if (tierRequired.getOrDefault(plan, 0) <= currentTier) available.add(plan);

            if (available.isEmpty()) {
                table.label(() -> Core.bundle.get("flexassembler.no-plans")).pad(10);
                if (locked) table.button(Core.bundle.get("flexassembler.deselect"), () -> configure(NO_PLAN));
                return;
            }

            if (locked) table.label(() -> Core.bundle.format("flexassembler.producing", current.unit.localizedName)).padBottom(4).row();
            else table.label(() -> Core.bundle.get("flexassembler.select-unit")).padBottom(4).color(Color.gray).row();

            Table grid = new Table();
            int cols = 4;
            for (int i = 0; i < available.size; i++) {
                if (i % cols == 0 && i != 0) grid.row();
                AssemblerUnitPlan plan = available.get(i);
                boolean isChosen = locked && current == plan;
                int index = plans.indexOf(plan);

                Button btn = new Button(Tex.button);
                btn.table(inner -> {
                    inner.image(plan.unit.uiIcon).size(30f).padBottom(4f);
                    inner.row();
                    inner.add(plan.unit.localizedName).color(isChosen ? Pal.accent : Color.lightGray);
                }).pad(8);

                btn.clicked(() -> configure(index));
                grid.add(btn).size(80f, 80f).pad(4f);
            }
            ScrollPane pane = new ScrollPane(grid);
            table.add(pane).grow().maxHeight(400f).row();
            if (locked) table.button(Core.bundle.get("flexassembler.deselect"), () -> configure(NO_PLAN)).size(120f, 40f).padTop(8).row();
        }

        @Override public Object config() { return lockedIndex; }

        @Override
        public void configure(@Nullable Object value) {
            if (value instanceof Integer) {
                int idx = (Integer) value;
                if (idx == NO_PLAN || (idx >= 0 && idx < plans.size)) {
                    lockedIndex = idx;
                }
            }
            super.configure(value);
            syncArea();
        }

        @Override
        public void updateTile() {
            if (!readUnits.isEmpty()) {
                units.clear();
                readUnits.each(i -> { var u = Groups.unit.getByID(i); if (u != null) units.add(u); });
                readUnits.clear();
            }
            if (lastTier != currentTier) {
                if (lastTier >= 0f) progress = 0f;
                lastTier = lastTier == -2 ? -1 : currentTier;
            }
            if (units.size < dronesCreated && whenSyncedUnits.size > 0) {
                whenSyncedUnits.each(id -> { var u = Groups.unit.getByID(id); if (u != null) units.addUnique(u); });
            }
            units.removeAll(u -> !u.isAdded() || u.dead || !(u.controller() instanceof AssemblerAI));
            if (!allowUpdate()) { progress = 0f; units.each(Unit::kill); units.clear(); }

            float powerStatus = !enabled ? 0f : power == null ? 1f : power.status;
            powerWarmup = Mathf.lerpDelta(powerStatus, powerStatus > 0.0001f ? 1f : 0f, 0.1f);
            droneWarmup = Mathf.lerpDelta(droneWarmup, units.size < dronesCreated ? powerStatus : 0f, 0.1f);
            totalDroneProgress += droneWarmup * delta();
            if (units.size < dronesCreated && enabled && (droneProgress += delta() * state.rules.unitBuildSpeed(team) * powerStatus / droneConstructTime) >= 1f) {
                if (!net.client()) {
                    var unit = droneType.create(team);
                    if (unit instanceof BuildingTetherc bt) bt.building(this);
                    unit.set(x, y); unit.rotation = 90f; unit.add(); units.add(unit);
                    Call.assemblerDroneSpawned(tile, unit.id);
                }
            }
            if (units.size >= dronesCreated) droneProgress = 0f;

            Vec2 spawn = getUnitSpawn();
            if (moveInPayload() && !wasOccupied) { yeetPayload(payload); payload = null; }
            for (int i = 0; i < units.size; i++) {
                var unit = units.get(i);
                var ai = (AssemblerAI)unit.controller();
                ai.targetPos.trns(i * 90f + 45f, areaSize / 2f * Mathf.sqrt2 * tilesize).add(spawn);
                ai.targetAngle = i * 90f + 45f + 180f;
            }
            wasOccupied = checkSolid(spawn, false);
            boolean visualOccupied = checkSolid(spawn, true);
            float eff = (units.count(u -> ((AssemblerAI)u.controller()).inPosition()) / (float) dronesCreated);
            sameTypeWarmup = Mathf.lerpDelta(sameTypeWarmup, wasOccupied && !visualOccupied ? 0f : 1f, 0.1f);
            invalidWarmup = Mathf.lerpDelta(invalidWarmup, visualOccupied ? 1f : 0f, 0.1f);

            var plan = plan();
            if (!wasOccupied && efficiency > 0 && Units.canCreate(team, plan.unit)) {
                warmup = Mathf.lerpDelta(warmup, efficiency, 0.1f);
                if ((progress += edelta() * state.rules.unitBuildSpeed(team) * eff / plan.time) >= 1f) {
                    Call.assemblerUnitSpawned(tile);
                }
            } else warmup = Mathf.lerpDelta(warmup, 0f, 0.1f);
        }

        public void spawned() {
            var plan = plan();
            Vec2 spawn = getUnitSpawn();
            consume();
            var unit = plan.unit.create(team);
            if (unit.isCommandable() && commandPos != null) unit.command().commandPosition(commandPos);
            unit.set(spawn.x + Mathf.range(0.001f), spawn.y + Mathf.range(0.001f));
            unit.rotation = rotdeg();
            var targetBuild = unit.buildOn();
            var payload = new UnitPayload(unit);
            if (targetBuild != null && targetBuild.team == team && targetBuild.acceptPayload(targetBuild, payload))
                targetBuild.handlePayload(targetBuild, payload);
            else if (!net.client()) { unit.add(); Units.notifyUnitSpawn(unit); }
            createSound.at(spawn.x, spawn.y, 1f + Mathf.range(0.06f), createSoundVolume);
            progress = 0f;
            Fx.unitAssemble.at(spawn.x, spawn.y, rotdeg() - 90f, plan.unit);
            blocks.clear();
            Events.fire(new UnitCreateEvent(unit, this));
        }

        // 工具方法
        public Vec2 getUnitSpawn() {
            float len = tilesize * (areaSize + block.size) / 2f;
            return Tmp.v4.set(x + Geometry.d4x(rotation) * len, y + Geometry.d4y(rotation) * len);
        }

        public boolean checkSolid(Vec2 v, boolean same) { var output = unit();
            float hsize = output.hitSize * 1.4f;
            return ((!output.flying && collisions.overlapsTile(Tmp.r1.setCentered(v.x, v.y, output.hitSize), EntityCollisions::solid)) ||
                    Units.anyEntities(v.x - hsize/2f, v.y - hsize/2f, hsize, hsize, u -> (!same || u.type != output) && !u.spawnedByCore &&
                            ((u.type.allowLegStep && output.allowLegStep) || (output.flying && u.isFlying()) || (!output.flying && u.isGrounded())))) return false; }
        public void yeetPayload(Payload payload){
            var spawn = getUnitSpawn();
            blocks.add(payload.content(), 1);
            float rot = payload.angleTo(spawn);
            Fx.shootPayloadDriver.at(payload.x(), payload.y(), rot);
            Fx.payloadDeposit.at(payload.x(), payload.y(), rot, new YeetData(spawn.cpy(), payload.content()));
            Sounds.shootPayload.at(x, y, 1f + Mathf.range(0.1f), 1f);
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            //draw input conveyors
            for(int i = 0; i < 4; i++){
                if(blends(i) && i != rotation){
                    Draw.rect(inRegion, x, y, (i * 90) - 180);
                }
            }

            Draw.rect(rotation >= 2 ? sideRegion2 : sideRegion1, x, y, rotdeg());

            Draw.z(Layer.blockOver);

            payRotation = rotdeg();
            drawPayload();

            Draw.z(Layer.blockOver + 0.1f);

            Draw.rect(topRegion, x, y);

            if(isPayload()) return;

            //draw drone construction
            if(droneWarmup > 0.001f){
                Draw.draw(Layer.blockOver + 0.2f, () -> {
                    Drawf.construct(this, droneType.fullIcon, Pal.accent, 0f, droneProgress, droneWarmup, totalDroneProgress, 14f);
                });
            }

            Vec2 spawn = getUnitSpawn();
            float sx = spawn.x, sy = spawn.y;

            var plan = plan();

            //draw the unit construction as outline
            Draw.draw(Layer.blockBuilding, () -> {
                Draw.color(Pal.accent, warmup);

                Shaders.blockbuild.region = plan.unit.fullIcon;
                Shaders.blockbuild.time = Time.time;
                Shaders.blockbuild.alpha = warmup;
                //margin due to units not taking up whole region
                Shaders.blockbuild.progress = Mathf.clamp(progress + 0.05f);

                Draw.rect(plan.unit.fullIcon, sx, sy, rotdeg() - 90f);
                Draw.flush();
                Draw.color();
                Shaders.blockbuild.alpha = 1f;
            });

            Draw.reset();

            Draw.z(Layer.buildBeam);

            //draw unit silhouette
            Draw.mixcol(Tmp.c1.set(Pal.accent).lerp(Pal.remove, invalidWarmup), 1f);
            Draw.alpha(Math.min(powerWarmup, sameTypeWarmup));
            Draw.rect(plan.unit.fullIcon, spawn.x, spawn.y, rotdeg() - 90f);

            //build beams do not draw when invalid
            Draw.alpha(Math.min(1f - invalidWarmup, warmup));

            //draw build beams
            for(var unit : units){
                if(!((AssemblerAI)unit.controller()).inPosition()) continue;

                float
                        px = unit.x + Angles.trnsx(unit.rotation, unit.type.buildBeamOffset),
                        py = unit.y + Angles.trnsy(unit.rotation, unit.type.buildBeamOffset);

                Drawf.buildBeam(px, py, spawn.x, spawn.y, plan.unit.hitSize/2f);
            }

            //fill square in middle
            Fill.square(spawn.x, spawn.y, plan.unit.hitSize/2f);

            Draw.reset();

            Draw.z(Layer.buildBeam);

            float fulls = areaSize * tilesize/2f;

            //draw full area
            Lines.stroke(2f, Pal.accent);
            Draw.alpha(powerWarmup);
            Drawf.dashRectBasic(spawn.x - fulls, spawn.y - fulls, fulls*2f, fulls*2f);

            Draw.reset();

            float outSize = plan.unit.hitSize + 9f;

            if(invalidWarmup > 0){
                //draw small square for area
                Lines.stroke(2f, Tmp.c3.set(Pal.accent).lerp(Pal.remove, invalidWarmup).a(invalidWarmup));
                Drawf.dashSquareBasic(spawn.x, spawn.y, outSize);
            }

            Draw.reset();
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