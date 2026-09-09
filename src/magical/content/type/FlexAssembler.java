package magical.content;
import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.ai.types.AssemblerAI;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.ConstructBlock.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import java.util.HashMap;
import java.util.Map;
import static mindustry.Vars.*;
public class FlexAssembler extends PayloadBlock{
    public int areaSize = 11;
    public UnitType droneType = UnitTypes.assemblyDrone;
    public int dronesCreated = 4;
    public float droneConstructTime = 60f * 4f;
    public int[] capacities = {};
    public Seq<AssemblerUnitPlan> plans = new Seq<>(4);
    public Sound createSound = Sounds.unitCreateBig;
    public float createSoundVolume = 1f;
    public Map<AssemblerUnitPlan, Integer> planAreaMap = new HashMap<>();
    public Map<AssemblerUnitPlan, Integer> tierRequired = new HashMap<>();
    public @Load("@-side1") TextureRegion sideRegion1;
    public @Load("@-side2") TextureRegion sideRegion2;
    protected @Nullable ConsumePayloadDynamic consPayload;
    protected @Nullable ConsumeItemDynamic consItem;
    public FlexAssembler(String name){
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
    public void addPlan(String label, UnitType output, float time, int customArea, int requiredTier, PayloadStack... requirements){
        Seq<PayloadStack> reqSeq = new Seq<>(requirements);
        AssemblerUnitPlan plan = new AssemblerUnitPlan(output, time, reqSeq);
        plans.add(plan);
        tierRequired.put(plan, requiredTier);
        planAreaMap.put(plan, customArea);
    }
    public Rect getRect(Rect rect, float x, float y, int rotation){
        rect.setCentered(x, y, areaSize * tilesize);
        float len = tilesize * (areaSize + size) / 2f;
        rect.x += Geometry.d4x(rotation) * len;
        rect.y += Geometry.d4y(rotation) * len;
        return rect;
    }
    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        x *= tilesize;
        y *= tilesize;
        x += offset;
        y += offset;
        Rect rect = getRect(Tmp.r1, x, y, rotation);
        Drawf.dashRect(valid ? Pal.accent : Pal.remove, rect);
    }
    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        Rect rect = getRect(Tmp.r1, tile.worldx() + offset, tile.worldy() + offset, rotation).grow(0.1f);
        return !indexer.getFlagged(team, BlockFlag.unitAssembler).contains(b ->
                b != tile.build &&
                        b.block instanceof FlexAssembler assembler &&
                        assembler.getRect(Tmp.r2, b.x, b.y, b.rotation).overlaps(rect)
        );
    }
    @Override
    public void setBars(){
        super.setBars();
        boolean planLiquids = false;
        for(AssemblerUnitPlan plan : plans){
            if(plan.liquidReq != null && plan.liquidReq.length > 0){
                for(LiquidStack stack : plan.liquidReq){
                    addLiquidBar(stack.liquid);
                }
                planLiquids = true;
            }
        }
        if(planLiquids){
            removeBar("liquid");
        }
        addBar("progress", (FlexAssemblerBuild e) ->
                new Bar(
                        "bar.progress",
                        Pal.ammo,
                        () -> e.progress
                )
        );
        addBar("units", (FlexAssemblerBuild e) ->
                new Bar(
                        () -> Core.bundle.format(
                                "bar.unitcap",
                                Fonts.getUnicodeStr(e.unit().name),
                                e.team.data().countType(e.unit()),
                                e.unit().useUnitCap ? Units.getStringCap(e.team) : "∞"
                        ),
                        () -> Pal.power,
                        () -> e.unit().useUnitCap ?
                                (float)e.team.data().countType(e.unit()) / Units.getCap(e.team) :
                                1f
                )
        );
    }
    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
        if(sideRegion1 != null && sideRegion2 != null){
            Draw.rect(
                    plan.rotation >= 2 ? sideRegion2 : sideRegion1,
                    plan.drawx(),
                    plan.drawy(),
                    plan.rotation * 90
            );
        }
        Draw.rect(topRegion, plan.drawx(), plan.drawy());
    }
    @Override
    public TextureRegion[] icons(){
        if(sideRegion1 != null && sideRegion2 != null){
            return new TextureRegion[]{region, sideRegion1, topRegion};
        }
        return new TextureRegion[]{region, topRegion};
    }
    @Override
    public void init(){
        updateClipRadius((areaSize + 1) * tilesize);
        consume(
                consPayload = new ConsumePayloadDynamic(
                        (FlexAssemblerBuild build) -> {
                            AssemblerUnitPlan plan = build.plan();
                            return plan == null || plan.requirements == null ?
                                    new Seq<PayloadStack>() :
                                    plan.requirements;
                        }
                )
        );
        consume(
                consItem = new ConsumeItemDynamic(
                        (FlexAssemblerBuild build) -> {
                            AssemblerUnitPlan plan = build.plan();
                            return plan == null || plan.itemReq == null ?
                                    ItemStack.empty :
                                    plan.itemReq;
                        }
                )
        );
        consume(
                new ConsumeLiquidsDynamic(
                        (FlexAssemblerBuild build) -> {
                            AssemblerUnitPlan plan = build.plan();
                            return plan == null || plan.liquidReq == null ?
                                    LiquidStack.empty :
                                    plan.liquidReq;
                        }
                )
        );
        super.init();
        initCapacities();
    }
    @Override
    public void afterPatch(){
        initCapacities();
        super.afterPatch();
    }
    public void initCapacities(){
        consumeBuilder.each(c ->
                c.multiplier = b -> state.rules.unitCost(b.team)
        );
        itemCapacity = 10;
        capacities = new int[Vars.content.items().size];
        for(AssemblerUnitPlan plan : plans){
            if(plan.itemReq != null){
                for(ItemStack stack : plan.itemReq){
                    capacities[stack.item.id] = Math.max(
                            capacities[stack.item.id],
                            stack.amount * 2
                    );
                    itemCapacity = Math.max(
                            itemCapacity,
                            stack.amount * 2
                    );
                }
            }
            if(plan.liquidReq != null){
                for(LiquidStack stack : plan.liquidReq){
                    liquidFilter[stack.liquid.id] = true;
                }
            }
        }
    }
    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.output);
        stats.add(Stat.output, table -> {
            table.row();
            Map<Integer, Seq<AssemblerUnitPlan>> byTier = new HashMap<>();
            for(AssemblerUnitPlan plan : plans){
                int required = tierRequired.getOrDefault(plan, 0);
                byTier.computeIfAbsent(
                        required,
                        k -> new Seq<>()
                ).add(plan);
            }
            int maxTier = byTier.keySet().stream()
                    .max(Integer::compareTo)
                    .orElse(0);
            for(int tier = 0; tier <= maxTier; tier++){
                Seq<AssemblerUnitPlan> group = byTier.get(tier);
                if(group == null || group.isEmpty()) continue;
                final int currentTier = tier;
                table.table(
                        Tex.pane,
                        t -> t.add(
                                Core.bundle.format(
                                        "flexassembler.tier.stat",
                                        currentTier
                                )
                        ).pad(5).left().growX()
                ).growX().pad(5).row();
                for(AssemblerUnitPlan plan : group){
                    table.table(Tex.pane, t -> {
                        if(plan.unit.isBanned()){
                            t.image(Icon.cancel)
                                    .color(Pal.remove)
                                    .size(40)
                                    .pad(10);
                            return;
                        }
                        if(plan.unit.unlockedNow()){
                            t.image(plan.unit.uiIcon)
                                    .scaling(Scaling.fit)
                                    .size(40)
                                    .pad(10)
                                    .left();
                            t.table(info -> {
                                info.left();
                                info.add(plan.unit.localizedName).left();
                                info.row();
                                info.add(
                                        Strings.autoFixed(
                                                plan.time / 60f,
                                                1
                                        ) + " " +
                                                Core.bundle.get("unit.seconds")
                                ).color(Color.lightGray).left();
                                int required = tierRequired.getOrDefault(plan, 0);
                                if(required > 0){
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
                                                planAreaMap.getOrDefault(
                                                        plan,
                                                        areaSize
                                                )
                                        )
                                ).color(Color.lightGray).left();
                            }).left();
                            t.table(req -> {
                                int length = 0;
                                if(plan.itemReq != null){
                                    for(ItemStack stack : plan.itemReq){
                                        if(length % 4 == 0){
                                            req.row();
                                        }
                                        req.add(
                                                StatValues.stack(stack)
                                        ).pad(5);
                                        length++;
                                    }
                                }
                                if(plan.requirements != null){
                                    for(PayloadStack stack : plan.requirements){
                                        if(length % 4 == 0){
                                            req.row();
                                        }
                                        req.add(
                                                StatValues.stack(stack)
                                        ).pad(5);
                                        length++;
                                    }
                                }
                                if(plan.liquidReq != null){
                                    for(LiquidStack stack : plan.liquidReq){
                                        req.row();
                                        req.add(
                                                StatValues.displayLiquid(
                                                        stack.liquid,
                                                        stack.amount * 60f,
                                                        true
                                                )
                                        ).pad(5).right();
                                    }
                                }
                            }).right();
                        }else{
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
    public static class AssemblerUnitPlan{
        public UnitType unit;
        @Nullable
        public Seq<PayloadStack> requirements;
        @Nullable
        public ItemStack[] itemReq;
        @Nullable
        public LiquidStack[] liquidReq;
        public float time;
        public AssemblerUnitPlan(
                UnitType unit,
                float time,
                Seq<PayloadStack> requirements
        ){
            this.unit = unit;
            this.time = time;
            this.requirements = requirements;
        }
        AssemblerUnitPlan(){}
    }
    public static class YeetData{
        public Vec2 target;
        public UnlockableContent item;
        public YeetData(
                Vec2 target,
                UnlockableContent item
        ){
            this.target = target;
            this.item = item;
        }
    }
    @Remote(called = Loc.server)
    public static void flexAssemblerUnitSpawned(Tile tile){
        if(tile == null || !(tile.build instanceof FlexAssemblerBuild build)){
            return;
        }
        build.spawned();
    }
    @Remote(called = Loc.server)
    public static void flexAssemblerDroneSpawned(
            Tile tile,
            int id
    ){
        if(tile == null || !(tile.build instanceof FlexAssemblerBuild build)){
            return;
        }
        build.droneSpawned(id);
    }
    @Override
    public Building createBuilding(){
        return new FlexAssemblerBuild();
    }
    public class FlexAssemblerBuild extends PayloadBlock.PayloadBlockBuild<Payload>{
        private static final int NO_PLAN = -1;
        protected IntSeq readUnits = new IntSeq();
        protected IntSeq whenSyncedUnits = new IntSeq();
        public @Nullable Vec2 commandPos;
        public Seq<Unit> units = new Seq<>();
        public Seq<FlexAssemblerModule.FlexAssemblerModuleBuild> modules = new Seq<>();
        public PayloadSeq blocks = new PayloadSeq();
        public float progress;
        public float warmup;
        public float droneWarmup;
        public float powerWarmup;
        public float sameTypeWarmup;
        public float invalidWarmup;
        public float droneProgress;
        public float totalDroneProgress;
        public int currentTier;
        public int lastTier = -2;
        public boolean wasOccupied;
        private int lockedIndex = NO_PLAN;
        private AssemblerUnitPlan lockedPlan;
        public int myAreaSize = FlexAssembler.this.areaSize;
        private void updateLockedPlan(){
            if(lockedIndex >= 0 && lockedIndex < plans.size){
                lockedPlan = plans.get(lockedIndex);
            }else{
                lockedPlan = null;
            }
        }
        private void syncArea(){
            AssemblerUnitPlan effective = plan();
            if(effective != null){
                myAreaSize = planAreaMap.getOrDefault(
                        effective,
                        FlexAssembler.this.areaSize
                );
            }else{
                myAreaSize = FlexAssembler.this.areaSize;
            }
        }
        @Override
        public void created(){
            super.created();
            updateLockedPlan();
            syncArea();
        }
        public Vec2 getUnitSpawn(){
            float len = tilesize * (myAreaSize + size) / 2f;
            return Tmp.v4.set(
                    x + Geometry.d4x(rotation) * len,
                    y + Geometry.d4y(rotation) * len
            );
        }
        public Rect getRect(Rect rect){
            float len = tilesize * (myAreaSize + size) / 2f;
            rect.setCentered(
                    x + Geometry.d4x(rotation) * len,
                    y + Geometry.d4y(rotation) * len,
                    myAreaSize * tilesize
            );
            return rect;
        }
        public boolean moduleFits(
                Block other,
                float ox,
                float oy,
                int rotation
        ){
            float dx =
                    ox +
                            Geometry.d4x(rotation) *
                                    (other.size / 2f + 0.5f) *
                                    tilesize;
            float dy =
                    oy +
                            Geometry.d4y(rotation) *
                                    (other.size / 2f + 0.5f) *
                                    tilesize;
            Vec2 spawn = getUnitSpawn();
            if(Tile.relativeTo(
                    ox,
                    oy,
                    spawn.x,
                    spawn.y
            ) != rotation){
                return false;
            }
            float dst = Math.max(
                    Math.abs(dx - spawn.x),
                    Math.abs(dy - spawn.y)
            );
            return Mathf.equal(
                    dst,
                    tilesize * myAreaSize / 2f -
                            tilesize / 2f
            );
        }
        public void updateModules(
                FlexAssemblerModule.FlexAssemblerModuleBuild build
        ){
            modules.addUnique(build);
            checkTier();
        }
        public void removeModule(
                FlexAssemblerModule.FlexAssemblerModuleBuild build
        ){
            modules.remove(build);
            checkTier();
        }
        public void checkTier(){
            modules.sort(b -> b.tier());
            int max = 0;
            for(FlexAssemblerModule.FlexAssemblerModuleBuild module : modules){
                if(module.tier() == max ||
                        module.tier() == max + 1){
                    max = module.tier();
                }else{
                    break;
                }
            }
            currentTier = max;
        }
        public UnitType unit(){
            AssemblerUnitPlan p = plan();
            return p == null ? UnitTypes.alpha : p.unit;
        }
        public AssemblerUnitPlan plan(){
            if(lockedPlan != null){
                int required = tierRequired.getOrDefault(
                        lockedPlan,
                        0
                );
                if(required <= currentTier){
                    return lockedPlan;
                }
            }
            if(plans.isEmpty()){
                return null;
            }
            AssemblerUnitPlan best = null;
            int bestTier = -1;
            for(AssemblerUnitPlan plan : plans){
                int required = tierRequired.getOrDefault(
                        plan,
                        0
                );
                if(required <= currentTier &&
                        required >= bestTier){
                    best = plan;
                    bestTier = required;
                }
            }
            return best;
        }
        @Override
        public boolean shouldConsume(){
            AssemblerUnitPlan plan = plan();
            if(plan == null){
                return false;
            }
            return enabled &&
                    !wasOccupied &&
                    Units.canCreate(team, plan.unit) &&
                    (consPayload == null || consPayload.efficiency(this) > 0) &&
                    (consItem == null || consItem.efficiency(this) > 0) &&
                    team.activateUnitFactories();
        }
        @Override
        public void drawSelect(){
            for(FlexAssemblerModule.FlexAssemblerModuleBuild module : modules){
                Drawf.selected(module, Pal.accent);
            }
            Drawf.dashRect(
                    Tmp.c1.set(Pal.accent)
                            .lerp(Pal.remove, invalidWarmup),
                    getRect(Tmp.r1)
            );
        }
        @Override
        public void display(Table table){
            super.display(table);
            if(team != player.team()){
                return;
            }
            table.row();
            table.table(t -> {
                t.left().defaults().left();
                Block prev = null;
                for(FlexAssemblerModule.FlexAssemblerModuleBuild module : modules){
                    if(prev == module.block){
                        continue;
                    }
                    t.image(module.block.uiIcon)
                            .size(iconMed)
                            .padRight(4);
                    prev = module.block;
                }
                AssemblerUnitPlan plan = plan();
                if(plan != null){
                    t.label(
                            () -> "[accent] -> []" +
                                    plan.unit.emoji() +
                                    " " +
                                    plan.unit.localizedName
                    );
                }
            }).pad(4).padLeft(0f).fillX().left();
        }
        @Override
        public void buildConfiguration(Table table){
            if(Vars.headless){
                return;
            }
            updateLockedPlan();
            AssemblerUnitPlan current = lockedPlan;
            boolean locked = current != null;
            Seq<AssemblerUnitPlan> available = new Seq<>();
            for(AssemblerUnitPlan plan : plans){
                if(tierRequired.getOrDefault(plan, 0) <= currentTier){
                    available.add(plan);
                }
            }
            if(available.isEmpty()){
                table.label(
                        () -> Core.bundle.get(
                                "flexassembler.no-plans"
                        )
                ).pad(10);
                if(locked){
                    table.row();
                    table.button(
                            Core.bundle.get(
                                    "flexassembler.deselect"
                            ),
                            () -> configure(NO_PLAN)
                    );
                }
                return;
            }
            if(locked){
                table.label(
                        () -> Core.bundle.format(
                                "flexassembler.producing",
                                current.unit.localizedName
                        )
                ).padBottom(4).row();
            }else{
                table.label(
                                () -> Core.bundle.get(
                                        "flexassembler.select-unit"
                                )
                        ).padBottom(4)
                        .color(Color.gray)
                        .row();
            }
            Table grid = new Table();
            int cols = 4;
            for(int i = 0; i < available.size; i++){
                if(i % cols == 0 && i != 0){
                    grid.row();
                }
                AssemblerUnitPlan plan = available.get(i);
                boolean chosen = locked && current == plan;
                int index = plans.indexOf(plan);
                Button button = new Button(Tex.button);
                button.table(inner -> {
                    inner.image(plan.unit.uiIcon)
                            .size(30f)
                            .padBottom(4f);
                    inner.row();
                    inner.add(plan.unit.localizedName)
                            .color(
                                    chosen ?
                                            Pal.accent :
                                            Color.lightGray
                            );
                }).pad(8);
                button.clicked(() -> {
                    lockedIndex = index;
                    updateLockedPlan();
                    syncArea();
                    configure(index);
                });
                grid.add(button)
                        .size(80f, 80f)
                        .pad(4f);
            }
            table.add(new ScrollPane(grid))
                    .grow()
                    .maxHeight(400f)
                    .row();
            if(locked){
                table.button(
                        Core.bundle.get(
                                "flexassembler.deselect"
                        ),
                        () -> configure(NO_PLAN)
                ).growX().pad(5);
            }
        }
        @Override
        public Object config(){
            return lockedIndex;
        }
        @Override
        public void configure(@Nullable Object value){
            if(value instanceof Integer){
                int index = (Integer)value;
                if(index == NO_PLAN ||
                        (index >= 0 && index < plans.size)){
                    lockedIndex = index;
                    updateLockedPlan();
                    syncArea();
                }
            }
            super.configure(value);
        }
        @Override
        public void updateTile(){
            if(!readUnits.isEmpty()){
                units.clear();
                readUnits.each(id -> {
                    Unit unit = Groups.unit.getByID(id);
                    if(unit != null){
                        units.add(unit);
                    }
                });
                readUnits.clear();
            }
            if(lastTier != currentTier){
                if(lastTier >= 0){
                    progress = 0f;
                }
                lastTier = lastTier == -2 ? -1 : currentTier;
                syncArea();
            }
            if(units.size < dronesCreated &&
                    whenSyncedUnits.size > 0){
                whenSyncedUnits.each(id -> {
                    Unit unit = Groups.unit.getByID(id);
                    if(unit != null){
                        units.addUnique(unit);
                    }
                });
                whenSyncedUnits.clear();
            }
            units.removeAll(u ->
                    !u.isAdded() ||
                            u.dead ||
                            !(u.controller() instanceof AssemblerAI)
            );
            if(!allowUpdate()){
                progress = 0f;
                units.each(Unit::kill);
                units.clear();
            }
            float powerStatus =
                    !enabled ?
                            0f :
                            power == null ?
                                    1f :
                                    power.status;
            powerWarmup = Mathf.lerpDelta(
                    powerWarmup,
                    powerStatus > 0.0001f ? 1f : 0f,
                    0.1f
            );
            droneWarmup = Mathf.lerpDelta(
                    droneWarmup,
                    units.size < dronesCreated ?
                            powerStatus :
                            0f,
                    0.1f
            );
            totalDroneProgress += droneWarmup * delta();
            if(units.size < dronesCreated &&
                    enabled &&
                    (droneProgress +=
                            delta() *
                                    state.rules.unitBuildSpeed(team) *
                                    powerStatus /
                                    droneConstructTime) >= 1f){
                if(!net.client()){
                    Unit drone = droneType.create(team);
                    if(drone.controller() instanceof AssemblerAI){
                        if(drone instanceof BuildingTetherc tether){
                            tether.building(this);
                        }
                        drone.set(x, y);
                        drone.rotation = 90f;
                        drone.add();
                        units.add(drone);
                        Call.flexAssemblerDroneSpawned(
                                tile,
                                drone.id
                        );
                    }else{
                        droneProgress = 0f;
                    }
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
            for(int i = 0; i < units.size; i++){
                Unit drone = units.get(i);
                if(!(drone.controller() instanceof AssemblerAI ai)){
                    continue;
                }
                ai.targetPos.trns(
                        i * 90f + 45f,
                        myAreaSize / 2f *
                                Mathf.sqrt2 *
                                tilesize
                ).add(spawn);
                ai.targetAngle =
                        i * 90f +
                                45f +
                                180f;
            }
            AssemblerUnitPlan plan = plan();
            if(plan == null){
                progress = 0f;
                wasOccupied = false;
                return;
            }
            wasOccupied = checkSolid(spawn, false);
            boolean visualOccupied = checkSolid(spawn, true);
            float eff =
                    dronesCreated <= 0 ?
                            1f :
                            units.count(u ->
                                    u.controller() instanceof AssemblerAI ai &&
                                            ai.inPosition()
                            ) /
                                    (float)dronesCreated;
            sameTypeWarmup = Mathf.lerpDelta(
                    sameTypeWarmup,
                    wasOccupied && !visualOccupied ? 0f : 1f,
                    0.1f
            );
            invalidWarmup = Mathf.lerpDelta(
                    invalidWarmup,
                    visualOccupied ? 1f : 0f,
                    0.1f
            );
            if(!wasOccupied &&
                    efficiency > 0 &&
                    Units.canCreate(team, plan.unit) &&
                    shouldConsume()){
                warmup = Mathf.lerpDelta(
                        warmup,
                        efficiency,
                        0.1f
                );
                if((progress +=
                        edelta() *
                                state.rules.unitBuildSpeed(team) *
                                eff /
                                plan.time) >= 1f){
                    if(!net.client()){
                        spawned();
                    }
                }
            }else{
                warmup = Mathf.lerpDelta(
                        warmup,
                        0f,
                        0.1f
                );
            }
        }
        public void droneSpawned(int id){
            Fx.spawn.at(x, y);
            droneProgress = 0f;
            if(net.client()){
                whenSyncedUnits.add(id);
            }
        }
        public void spawned(){
            AssemblerUnitPlan plan = plan();
            if(plan == null){
                return;
            }
            Vec2 spawn = getUnitSpawn();
            consume();
            Unit unit = plan.unit.create(team);
            if(unit.isCommandable() &&
                    commandPos != null){
                unit.command().commandPosition(commandPos);
            }
            unit.set(
                    spawn.x + Mathf.range(0.001f),
                    spawn.y + Mathf.range(0.001f)
            );
            unit.rotation = rotdeg();
            if(!net.client()){
                unit.add();
                Units.notifyUnitSpawn(unit);
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
            Events.fire(
                    new EventType.UnitCreateEvent(
                            unit,
                            this
                    )
            );
        }
        @Override
        public void draw(){
            Draw.rect(region, x, y);
            if(inRegion != null){
                for(int i = 0; i < 4; i++){
                    if(blends(i) && i != rotation){
                        Draw.rect(
                                inRegion,
                                x,
                                y,
                                (i * 90) - 180
                        );
                    }
                }
            }
            if(sideRegion1 != null &&
                    sideRegion2 != null){
                Draw.rect(
                        rotation >= 2 ?
                                sideRegion2 :
                                sideRegion1,
                        x,
                        y,
                        rotdeg()
                );
            }
            Draw.z(Layer.blockOver);
            payRotation = rotdeg();
            drawPayload();
            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);
            if(isPayload()){
                return;
            }
            if(droneWarmup > 0.001f){
                Draw.draw(
                        Layer.blockOver + 0.2f,
                        () -> Drawf.construct(
                                this,
                                droneType.fullIcon,
                                Pal.accent,
                                0f,
                                droneProgress,
                                droneWarmup,
                                totalDroneProgress,
                                14f
                        )
                );
            }
            Vec2 spawn = getUnitSpawn();
            AssemblerUnitPlan plan = plan();
            if(plan == null){
                return;
            }
            Draw.draw(Layer.blockBuilding, () -> {
                Draw.color(Pal.accent, warmup);
                Shaders.blockbuild.region =
                        plan.unit.fullIcon;
                Shaders.blockbuild.time =
                        Time.time;
                Shaders.blockbuild.alpha =
                        warmup;
                Shaders.blockbuild.progress =
                        Mathf.clamp(progress + 0.05f);
                Draw.rect(
                        plan.unit.fullIcon,
                        spawn.x,
                        spawn.y,
                        rotdeg() - 90f
                );
                Draw.flush();
                Draw.color();
                Shaders.blockbuild.alpha = 1f;
            });
            Draw.reset();
            Draw.z(Layer.buildBeam);
            Draw.mixcol(
                    Tmp.c1.set(Pal.accent)
                            .lerp(Pal.remove, invalidWarmup),
                    1f
            );
            Draw.alpha(
                    Math.min(
                            powerWarmup,
                            sameTypeWarmup
                    )
            );
            Draw.rect(
                    plan.unit.fullIcon,
                    spawn.x,
                    spawn.y,
                    rotdeg() - 90f
            );
            Draw.alpha(
                    Math.min(
                            1f - invalidWarmup,
                            warmup
                    )
            );
            for(Unit drone : units){
                if(!(drone.controller() instanceof AssemblerAI ai) ||
                        !ai.inPosition()){
                    continue;
                }
                float px =
                        drone.x +
                                Angles.trnsx(
                                        drone.rotation,
                                        drone.type.buildBeamOffset
                                );
                float py =
                        drone.y +
                                Angles.trnsy(
                                        drone.rotation,
                                        drone.type.buildBeamOffset
                                );
                Drawf.buildBeam(
                        px,
                        py,
                        spawn.x,
                        spawn.y,
                        plan.unit.hitSize / 2f
                );
            }
            Fill.square(
                    spawn.x,
                    spawn.y,
                    plan.unit.hitSize / 2f
            );
            Draw.reset();
            Draw.z(Layer.buildBeam);
            float fulls =
                    myAreaSize *
                            tilesize /
                            2f;
            Lines.stroke(2f, Pal.accent);
            Draw.alpha(powerWarmup);
            Drawf.dashRectBasic(
                    spawn.x - fulls,
                    spawn.y - fulls,
                    fulls * 2f,
                    fulls * 2f
            );
            Draw.reset();
            float outSize =
                    plan.unit.hitSize + 9f;
            if(invalidWarmup > 0){
                Lines.stroke(
                        2f,
                        Tmp.c3.set(Pal.accent)
                                .lerp(Pal.remove, invalidWarmup)
                                .a(invalidWarmup)
                );
                Drawf.dashSquareBasic(
                        spawn.x,
                        spawn.y,
                        outSize
                );
            }
            Draw.reset();
        }
        public boolean checkSolid(Vec2 v, boolean same){
            AssemblerUnitPlan plan = plan();
            if(plan == null){
                return true;
            }
            UnitType output = plan.unit;
            float hsize = output.hitSize * 1.4f;
            return (
                    !output.flying &&
                            collisions.overlapsTile(
                                    Tmp.r1.setCentered(
                                            v.x,
                                            v.y,
                                            output.hitSize
                                    ),
                                    EntityCollisions::solid
                            )
            ) ||
                    Units.anyEntities(
                            v.x - hsize / 2f,
                            v.y - hsize / 2f,
                            hsize,
                            hsize,
                            u ->
                                    (!same || u.type != output) &&
                                            !u.spawnedByCore &&
                                            (
                                                    (u.type.allowLegStep &&
                                                            output.allowLegStep) ||
                                                            (output.flying &&
                                                                    u.isFlying()) ||
                                                            (!output.flying &&
                                                                    u.isGrounded())
                                            )
                    );
        }
        public boolean ready(){
            return efficiency > 0 && !wasOccupied;
        }
        public void yeetPayload(Payload payload){
            Vec2 spawn = getUnitSpawn();
            blocks.add(payload.content(), 1);
            float rot = payload.angleTo(spawn);
            Fx.shootPayloadDriver.at(
                    payload.x(),
                    payload.y(),
                    rot
            );
            Fx.payloadDeposit.at(
                    payload.x(),
                    payload.y(),
                    rot,
                    new YeetData(
                            spawn.cpy(),
                            payload.content()
                    )
            );
            Sounds.shootPayload.at(
                    x,
                    y,
                    1f + Mathf.range(0.1f),
                    1f
            );
        }
        @Override
        public BlockStatus status(){
            if(!team.activateUnitFactories()){
                return BlockStatus.inactive;
            }
            return super.status();
        }
        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.progress){
                return progress;
            }
            return super.sense(sensor);
        }
        @Override
        public PayloadSeq getPayloads(){
            return blocks;
        }
        @Override
        public boolean acceptPayload(
                Building source,
                Payload payload
        ){
            AssemblerUnitPlan plan = plan();
            if(plan == null ||
                    plan.requirements == null){
                return false;
            }
            return (
                    this.payload == null ||
                            source instanceof FlexAssemblerModule.FlexAssemblerModuleBuild
            ) &&
                    plan.requirements.contains(
                            stack ->
                                    stack.item == payload.content() &&
                                            blocks.get(payload.content()) <
                                                    Mathf.round(
                                                            stack.amount *
                                                                    state.rules.unitCost(team)
                                                    ) -
                                                            (
                                                                    source instanceof FlexAssemblerModule.FlexAssemblerModuleBuild &&
                                                                            this.payload != null &&
                                                                            this.payload.contentEquals(payload) ?
                                                                            1 :
                                                                            0
                                                            )
                    );
        }
        @Override
        public int getMaximumAccepted(Item item){
            return Mathf.round(
                    capacities[item.id] *
                            state.rules.unitCost(team)
            );
        }
        @Override
        public boolean acceptItem(
                Building source,
                Item item
        ){
            AssemblerUnitPlan plan = plan();
            return plan != null &&
                    plan.itemReq != null &&
                    items.get(item) < getMaximumAccepted(item) &&
                    Structs.contains(
                            plan.itemReq,
                            stack -> stack.item == item
                    );
        }
        @Override
        public Vec2 getCommandPosition(){
            return commandPos;
        }
        @Override
        public void onCommand(Vec2 target){
            commandPos = target;
        }
        @Override
        public byte version(){
            return 2;
        }
        @Override
        public void write(Writes write){
            super.write(write);
            write.f(progress);
            write.b(units.size);
            for(Unit unit : units){
                write.i(unit.id);
            }
            blocks.write(write);
            TypeIO.writeVecNullable(
                    write,
                    commandPos
            );
            write.i(lockedIndex);
            write.i(myAreaSize);
        }
        @Override
        public void read(
                Reads read,
                byte revision
        ){
            super.read(read, revision);
            progress = read.f();
            int count = read.b();
            readUnits.clear();
            for(int i = 0; i < count; i++){
                readUnits.add(read.i());
            }
            whenSyncedUnits.clear();
            blocks.read(read);
            if(revision >= 1){
                commandPos = TypeIO.readVecNullable(read);
            }
            if(revision >= 2){
                lockedIndex = read.i();
                myAreaSize = read.i();
            }
            updateLockedPlan();
            syncArea();
        }
    }
    public static class FlexAssemblerModule extends PayloadBlock{
        public int tier = 1;
        public FlexAssemblerModule(String name){
            super(name);
            update = solid = true;
            rotate = true;
            rotateDraw = false;
            acceptsPayload = true;
            flags = EnumSet.of(BlockFlag.unitAssembler);
            group = BlockGroup.units;
            sync = true;
        }
        @Override
        public void setStats(){
            super.setStats();
            stats.add(
                    Stat.moduleTier,
                    tier
            );
        }
        @Override
        public boolean canPlaceOn(
                Tile tile,
                Team team,
                int rotation
        ){
            return getLink(
                    team,
                    tile.x,
                    tile.y,
                    rotation
            ) != null;
        }
        public FlexAssemblerBuild getLink(
                Team team,
                int x,
                int y,
                int rotation
        ){
            Building found = indexer.getFlagged(
                    team,
                    BlockFlag.unitAssembler
            ).find(
                    b ->
                            b instanceof FlexAssemblerBuild &&
                                    ((FlexAssemblerBuild)b).moduleFits(
                                            this,
                                            x * tilesize + offset,
                                            y * tilesize + offset,
                                            rotation
                                    )
            );
            return found instanceof FlexAssemblerBuild build ?
                    build :
                    null;
        }
        @Override
        public Building createBuilding(){
            return new FlexAssemblerModuleBuild();
        }
        public class FlexAssemblerModuleBuild extends PayloadBlock.PayloadBlockBuild<Payload>{
            public FlexAssemblerBuild link;
            public int lastChange = -2;
            public void findLink(){
                if(link != null){
                    link.removeModule(this);
                }
                link = getLink(
                        team,
                        tile.x,
                        tile.y,
                        rotation
                );
                if(link != null){
                    link.updateModules(this);
                }
            }
            public int tier(){
                return FlexAssemblerModule.this.tier;
            }
            @Override
            public boolean acceptPayload(
                    Building source,
                    Payload payload
            ){
                return link != null &&
                        this.payload == null &&
                        link.acceptPayload(this, payload);
            }
            @Override
            public void drawSelect(){
                if(link != null){
                    Drawf.selected(
                            link,
                            Pal.accent
                    );
                }
            }
            @Override
            public void onRemoved(){
                if(link != null){
                    link.removeModule(this);
                    link = null;
                }
                super.onRemoved();
            }
            @Override
            public void updateTile(){
                if(lastChange != world.tileChanges){
                    lastChange = world.tileChanges;
                    findLink();
                }
                if(
                        payload != null &&
                                moveInPayload() &&
                                link != null &&
                                link.moduleFits(
                                        block,
                                        x,
                                        y,
                                        rotation
                                ) &&
                                !link.wasOccupied &&
                                link.acceptPayload(
                                        this,
                                        payload
                                ) &&
                                efficiency > 0
                ){
                    link.yeetPayload(payload);
                    payload = null;
                }
            }
        }
    }
}