package magical.content;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

import magical.content.MLUi;

public class MultiCrafter extends PayloadBlock{

    public Seq<CraftPlan> craftPlans = new Seq<>();


    public DrawBlock drawer = new DrawDefault();

    public boolean useBlockDrawer = false;

    public boolean hasDoubleOutput = false;

    public boolean autoAddBar = true;

    public Color progressColor = Pal.accent;

    public boolean showProgressBar = true;

    public boolean useLiquidTable = true;

    public int maxList = 4;

    public float maxPayloadSize = 4f;

    public int payloadCapacity = 20;

    public float selectScroll;

    public MultiCrafter(String name){
        super(name);

        update = true;
        solid = true;
        hasItems = true;
        sync = true;
        flags = EnumSet.of(BlockFlag.factory);
        drawArrow = false;

        configurable = true;
        saveConfig = true;

        config(int[].class, (MultiCrafterBuild tile, int[] in) -> tile.applyConfig(in));

        config(Integer.class, (MultiCrafterBuild tile, Integer in) ->
            tile.applyConfig(new int[]{tile.rotation, in}));
    }

    @Override
    public void init(){
        for(CraftPlan plan : craftPlans){
            plan.owner = this;
            plan.init();
            if(plan.outputLiquids.length > 0){
                hasLiquids = true;
                outputsLiquid = true;
            }
            if(plan.outputItems.length > 0){
                hasItems = true;
            }
            if(plan.consPower != null){
                hasPower = true;
                consumesPower = true;
            }
            if(plan.consPayload != null){
                acceptsPayload = true;
            }
            if(plan.powerProduction > 0){
                hasPower = true;
                outputsPower = true;
            }
        }

        if(hasPower && consumesPower) consumePowerDynamic(b ->
            b instanceof MultiCrafterBuild tile ? tile.formulaPower() : 0f);

        super.init();
        hasConsumers = craftPlans.any();
    }

    @Override
    public void setBars(){
        addBar("health", entity -> new Bar("stat.health", Pal.health, entity::healthf).blink(Color.white));

        if(consPower != null){
            boolean buffered = consPower.buffered;
            float capacity = consPower.capacity;
            addBar("power", entity -> new Bar(
                () -> buffered ? Core.bundle.format("bar.poweramount", Float.isNaN(entity.power.status * capacity) ? "<ERROR>" : UI.formatAmount((int)(entity.power.status * capacity))) :
                    Core.bundle.get("bar.power"),
                () -> Pal.powerBar,
                () -> Mathf.zero(consPower.requestedPower(entity)) && entity.power.graph.getPowerProduced() + entity.power.graph.getBatteryStored() > 0f ? 1f : entity.power.status)
            );
        }

        if(craftPlans.contains(c -> c.heatRequirement > 0f)){
            addBar("heat", (MultiCrafterBuild entity) -> new Bar(
                "bar.heat",
                Pal.lightOrange,
                () -> {
                    float req = entity.heatRequirement();
                    return req <= 0f ? 1f : Mathf.clamp(entity.heat / req, 0f, 1f);
                }));
        }

        if(unitCapModifier != 0){
            stats.add(Stat.maxUnits, (unitCapModifier < 0 ? "-" : "+") + Math.abs(unitCapModifier));
        }
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.output, table -> {
            table.row();
            for(CraftPlan plan : craftPlans){
                table.table(Styles.grayPanel, info -> {
                    info.left().defaults().left();
                    Stats s = new Stats();
                    s.timePeriod = plan.craftTime;
                    if(plan.hasConsumers) for(Consume c : plan.consumers) c.display(s);

                    if((hasItems && itemCapacity > 0) || plan.outputItems.length > 0)
                        s.add(Stat.productionTime, plan.craftTime / 60f, StatUnit.seconds);

                    if(plan.heatRequirement > 0f){
                        s.add(Stat.input, plan.heatRequirement, StatUnit.heatUnits);
                        s.add(Stat.maxEfficiency, (int)(plan.maxHeatEfficiency * 100f), StatUnit.percent);
                    }

                    if(plan.heatOutput > 0f){
                        s.add(Stat.output, plan.heatOutput, StatUnit.heatUnits);
                    }

                    if(plan.outputItems.length > 0)
                        s.add(Stat.output, StatValues.items(plan.craftTime, plan.outputItems));

                    if(plan.outputLiquids.length > 0)
                        s.add(Stat.output, StatValues.liquids(1f, plan.outputLiquids));

                    info.table(t -> MLUi.statTurnTable(s, t)).pad(8).left();
                }).growX().left().pad(10);
                table.row();
            }
        });
    }

    @Override
    public void load(){
        super.load();
        if(useBlockDrawer){
            drawer.load(this);
        }else{
            for(CraftPlan p : craftPlans) p.drawer.load(this);
        }
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        if(useBlockDrawer){
            drawer.drawPlan(this, plan, list);
            return;
        }
        DrawBlock first = craftPlans.any() ? craftPlans.get(0).drawer : null;
        if(first == null){
            super.drawPlanRegion(plan, list);
        }else{
            first.drawPlan(this, plan, list);
        }
    }

    @Override
    protected TextureRegion[] icons(){
        if(useBlockDrawer) return drawer.icons(this);
        DrawBlock fallback = craftPlans.any() ? craftPlans.get(0).drawer : null;
        return fallback == null ? super.icons() : fallback.icons(this);
    }

    public int clampCraftPlanIndex(int index){
        return craftPlans.isEmpty() ? -1 : Mathf.clamp(index, 0, craftPlans.size - 1);
    }

    public class MultiCrafterBuild extends PayloadBlockBuild<Payload> implements HeatBlock, HeatConsumer{

        public CraftPlan craftPlan = craftPlans.any() ? craftPlans.get(0) : null;

        public float progress;

        public float totalProgress;

        public float warmup;

        public float heat;
        public float[] sideHeat = new float[4];
        public PayloadSeq payloads = new PayloadSeq();

        public int[] configs = {0, 0};
        public int lastRotation = -1;
        public float selectScroll;

        public int craftPlanIndex(){
            if(craftPlan == null) return -1;
            int idx = craftPlans.indexOf(craftPlan);
            return idx < 0 ? -1 : idx;
        }

        public void setCraftPlanIndex(int index){
            int resolved = clampCraftPlanIndex(index);
            if(resolved < 0){
                craftPlan = null;
                configs[1] = -1;
                return;
            }
            craftPlan = craftPlans.get(resolved);
            configs[1] = resolved;
        }

        public void applyConfig(int[] in){
            if(in == null || in.length != 2) return;
            rotation = Mathf.mod(in[0], 4);
            configs[0] = rotation;
            setCraftPlanIndex(in[1]);
        }

        @Override
        public void draw(){
            (useBlockDrawer || craftPlan == null ? drawer : craftPlan.drawer).draw(this);
        }

        @Override
        public void drawStatus(){
            if(block.enableDrawStatus && craftPlan != null && craftPlan.hasConsumers){
                float multiplier = block.size > 1 ? 1 : 0.64f;
                float brcX = x + (float)(block.size * 8) / 2f - 8f * multiplier / 2f;
                float brcY = y - (float)(block.size * 8) / 2f + 8f * multiplier / 2f;
                Draw.z(71f);
                Draw.color(Pal.gray);
                Fill.square(brcX, brcY, 2.5f * multiplier, 45);
                Draw.color(status().color);
                Fill.square(brcX, brcY, 1.5f * multiplier, 45);
                Draw.color();
            }
        }

        public float warmupTarget(){
            return 1f;
        }

        public float formulaPower(){
            if(craftPlan == null || craftPlan.consPower == null) return 0f;
            return craftPlan.consPower.usage;
        }

        public float heatEfficiency(){
            heat = calculateHeat(sideHeat);
            if(craftPlan == null || craftPlan.heatRequirement <= 0f) return 1f;
            return Mathf.clamp(heat / craftPlan.heatRequirement, 0f, craftPlan.maxHeatEfficiency);
        }

        @Override
        public float efficiencyScale(){
            return heatEfficiency();
        }

        @Override
        public float[] sideHeat(){ return sideHeat; }

        @Override
        public float heatRequirement(){
            return craftPlan == null ? 0f : craftPlan.heatRequirement;
        }

        @Override
        public float heat(){
            if(craftPlan == null || craftPlan.heatOutput <= 0f) return 0f;
            return craftPlan.heatOutput * warmup;
        }

        @Override
        public float heatFrac(){
            if(craftPlan == null || craftPlan.heatOutput <= 0f) return 0f;
            return Mathf.clamp(heat() / craftPlan.heatOutput, 0f, 1f);
        }

        @Override
        public PayloadSeq getPayloads(){ return payloads; }

        @Override
        public float getPowerProduction(){
            if(craftPlan == null || !enabled) return 0f;
            return craftPlan.powerProduction * efficiency;
        }

        @Override
        public void updateTile(){
            if(lastRotation != rotation){
                Fx.placeBlock.at(x, y, size);
                lastRotation = rotation;
            }

            if(craftPlan == null) return;

            if(efficiency > 0f){
                float inc = getProgressIncrease(craftPlan.craftTime, craftPlan);
                if(inc > 0f){
                    progress += inc;
                    warmup = Mathf.approachDelta(warmup, warmupTarget(), craftPlan.warmupSpeed);

                    if(craftPlan.outputLiquids.length > 0){
                        float liquidInc = getProgressIncrease(1f);
                        for(LiquidStack output : craftPlan.outputLiquids){
                            handleLiquid(this, output.liquid,
                                Math.min(output.amount * liquidInc, liquidCapacity - liquids.get(output.liquid)));
                        }
                    }

                    if(wasVisible && Mathf.chanceDelta(craftPlan.updateEffectChance)){
                        craftPlan.updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
                    }
                }else{
                    warmup = Mathf.approachDelta(warmup, 0f, craftPlan.warmupSpeed);
                }
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, craftPlan.warmupSpeed);
            }

            totalProgress += warmup * Time.delta;

            if(progress >= 1f){
                craft(craftPlan);
            }

            dumpOutputs(craftPlan);
        }

        @Override
        public float totalProgress(){ return totalProgress; }

        @Override
        public float progress(){ return progress; }

        @Override
        public float warmup(){ return warmup; }

        public float getProgressIncrease(float baseTime, CraftPlan plan){
            if(plan.ignoreLiquidFullness){
                return super.getProgressIncrease(baseTime);
            }

            float scaling = 1f, max = 1f;
            if(plan.outputLiquids.length > 0){
                max = 0f;
                for(LiquidStack output : plan.outputLiquids){
                    float value = (liquidCapacity - liquids.get(output.liquid)) / (output.amount * edelta());
                    scaling = Math.min(scaling, value);
                    max = Math.max(max, value);
                }
            }

            return super.getProgressIncrease(baseTime) * (plan.dumpExtraLiquid ? Math.min(max, 1f) : scaling);
        }

        public void craft(CraftPlan plan){
            consume();

            for(ItemStack output : plan.outputItems){
                for(int i = 0; i < output.amount; i++){
                    offload(output.item);
                }
            }

            if(wasVisible){
                plan.craftEffect.at(x, y);
            }
            progress %= 1f;
        }

        public void dumpOutputs(CraftPlan plan){
            if(plan.outputItems.length > 0 && timer(timerDump, dumpTime / timeScale)){
                for(ItemStack output : plan.outputItems) dump(output.item);
            }

            if(plan.outputLiquids.length > 0){
                for(int i = 0; i < plan.outputLiquids.length; i++){
                    int dir = plan.liquidOutputDirections.length > i ? plan.liquidOutputDirections[i] : -1;
                    dumpLiquid(plan.outputLiquids[i].liquid, 2f, dir);
                }
            }
        }

        @Override
        public boolean shouldConsume(){
            if(craftPlan == null) return false;
            for(ItemStack output : craftPlan.outputItems){
                if(items.get(output.item) + output.amount > itemCapacity) return false;
            }
            if(craftPlan.outputLiquids.length > 0 && !craftPlan.ignoreLiquidFullness){
                boolean allFull = true;
                for(LiquidStack output : craftPlan.outputLiquids){
                    if(liquids.get(output.liquid) >= liquidCapacity - 0.001f){
                        if(!craftPlan.dumpExtraLiquid) return false;
                    }else{
                        allFull = false;
                    }
                }
                if(allFull) return false;
            }
            return enabled;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            if(craftPlan == null) return false;
            return craftPlan.getConsumeItem(item) && items.get(item) < itemCapacity;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            if(craftPlan == null) return false;
            return block.hasLiquids && craftPlan.getConsumeLiquid(liquid);
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            if(craftPlan == null || payload == null) return false;
            if(!block.acceptsPayload || !payload.fits(maxPayloadSize)) return false;

            UnlockableContent content = payload.content();
            if(content == null) return false;

            int need = craftPlan.payloadNeed(content, this);
            return need > 0 && payloads.get(content) < need && payloads.total() < payloadCapacity;
        }

        @Override
        public void handlePayload(Building source, Payload payload){
            if(craftPlan == null || payload == null) return;
            UnlockableContent content = payload.content();
            if(content == null) return;
            int need = craftPlan.payloadNeed(content, this);
            if(need <= 0 || payloads.get(content) >= need || payloads.total() >= payloadCapacity) return;
            payloads.add(content);
        }

        @Override
        public void consume(){
            if(craftPlan == null) return;
            for(Consume cons : craftPlan.consumers) cons.trigger(this);
        }

        @Override
        public void displayConsumption(Table table){
            MLUi.buildConsumption(this, table);
        }

        @Override
        public void drawSelect(){
            super.drawSelect();
            if(craftPlan == null || !useLiquidTable) return;

            if(craftPlan.outputLiquids.length > 0){
                for(int i = 0; i < craftPlan.outputLiquids.length; i++){
                    int dir = craftPlan.liquidOutputDirections.length > i ? craftPlan.liquidOutputDirections[i] : -1;
                    if(dir != -1){
                        Draw.rect(
                            craftPlan.outputLiquids[i].liquid.fullIcon,
                            x + Geometry.d4x(dir + rotation) * (size * tilesize / 2f + 4),
                            y + Geometry.d4y(dir + rotation) * (size * tilesize / 2f + 4),
                            8f, 8f);
                    }
                }
            }
        }

        @Override
        public void displayBars(Table table){
            MLUi.buildRecipeBars(MultiCrafter.this, this, table);
        }

        @Override
        public boolean shouldAmbientSound(){
            return efficiency > 0;
        }

        public transient boolean shouldConsumePower;

        @Override
        public void updateConsumption(){
            if(craftPlan == null) return;

            if(!craftPlan.hasConsumers || cheating()){
                potentialEfficiency = enabled && productionValid() ? 1f : 0f;
                efficiency = optionalEfficiency = shouldConsume() ? potentialEfficiency : 0f;
                shouldConsumePower = true;
                updateEfficiencyMultiplier();
                return;
            }

            if(!enabled){
                potentialEfficiency = efficiency = optionalEfficiency = 0f;
                shouldConsumePower = true;
                return;
            }

            boolean valid = shouldConsume() && productionValid();
            float minEfficiency = 1f;
            efficiency = optionalEfficiency = 1f;
            shouldConsumePower = true;

            for(Consume cons : craftPlan.nonOptionalConsumers){
                float result = cons.efficiency(this);
                if(cons != consPower && result <= 0.0000001f){
                    shouldConsumePower = false;
                }
                minEfficiency = Math.min(minEfficiency, result);
            }

            for(Consume cons : craftPlan.optionalConsumers){
                optionalEfficiency = Math.min(optionalEfficiency, cons.efficiency(this));
            }

            efficiency = minEfficiency;
            optionalEfficiency = Math.min(optionalEfficiency, minEfficiency);
            potentialEfficiency = efficiency;
            if(!valid) efficiency = optionalEfficiency = 0f;

            updateEfficiencyMultiplier();

            if(valid && efficiency > 0){
                for(Consume cons : craftPlan.updateConsumers) cons.update(this);
            }
        }

        @Override
        public void buildConfiguration(Table table){
            MLUi.buildRecipeConfig(MultiCrafter.this, this, table);
        }

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.progress) return progress;
            return super.sense(sensor);
        }

        @Override
        public int[] config(){
            return new int[]{rotation, craftPlanIndex()};
        }

        @Override
        public void configure(Object value){
            super.configure(value);
            deselect();
        }

        @Override
        public byte version(){ 
            return 2; 
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(progress);
            write.f(warmup);
            write.i(lastRotation);
            write.i(craftPlanIndex());
            payloads.write(write);
            write.f(selectScroll);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            progress = read.f();
            warmup = read.f();
            lastRotation = read.i();
            setCraftPlanIndex(read.i());
            configs[0] = rotation;
            selectScroll = read.f();
            if(revision >= 1){
                payloads.read(read);
            }else{
                payloads.clear();
            }
        }
    }

    public static class CraftPlan{
        public Consume[] consumers = {}, optionalConsumers = {}, nonOptionalConsumers = {}, updateConsumers = {};

        public ConsumePower consPower = null;

        public ConsumePayloads consPayload = null;

        public float craftTime = 60f;

        public boolean hasConsumers = false;

        public ItemStack[] outputItems = {};

        public LiquidStack[] outputLiquids = {};

        public int[] liquidOutputDirections = {-1};

        public boolean ignoreLiquidFullness = false;

        public boolean dumpExtraLiquid = true;

        public float warmupSpeed = 0.02f;

        public float updateEffectChance = 0.05f;

        public float powerProduction = 0f;

        public float heatRequirement = 0f;
        public float heatOutput = 0f;
        public float warmupRate = 0.15f;

        public float maxHeatEfficiency = 1f;

        public Effect updateEffect = Fx.none;
        public Effect craftEffect = Fx.none;

        public DrawBlock drawer = new DrawDefault();

        public ObjectMap<Item, Boolean> itemFilter = new ObjectMap<>();
        public ObjectMap<Liquid, Boolean> liquidFilter = new ObjectMap<>();
        public Seq<PayloadStack> payloadRequirements = new Seq<>();

        protected MultiCrafter owner = null;
        protected Seq<Consume> consumeBuilder = new Seq<>();
        protected OrderedMap<String, Func<Building, Bar>> barMap = new OrderedMap<>();

        public void init(){
            consumers = consumeBuilder.toArray(Consume.class);
            optionalConsumers = consumeBuilder.select(c -> c.optional && !c.ignore()).toArray(Consume.class);
            nonOptionalConsumers = consumeBuilder.select(c -> !c.optional && !c.ignore()).toArray(Consume.class);
            updateConsumers = consumeBuilder.select(c -> c.update && !c.ignore()).toArray(Consume.class);
            hasConsumers = consumers.length > 0;

            if(owner.autoAddBar){
                if(!liquidFilter.isEmpty()){
                    for(Liquid l : liquidFilter.keys().toSeq()) addLiquidBar(l);
                }
                for(LiquidStack l : outputLiquids) addLiquidBar(l.liquid);
            }
            
            if(owner.showProgressBar){
                addProgressBar();
            }
        }

        public void setApply(UnlockableContent content){
            if(content instanceof Item item) itemFilter.put(item, true);
            if(content instanceof Liquid liquid) liquidFilter.put(liquid, true);
        }

        public Iterable<Func<Building, Bar>> listBars(){ return barMap.values(); }

        public boolean hasBars(){ return !barMap.isEmpty(); }

        public void addBar(String name, Func<Building, Bar> sup){ barMap.put(name, sup); }

        public void addLiquidBar(Liquid liquid){
            addBar("liquid-" + liquid.name, build -> !liquid.unlockedNow() ? null : new Bar(
                () -> liquid.localizedName,
                liquid::barColor,
                () -> build.liquids.get(liquid) / owner.liquidCapacity));
        }

        public void addProgressBar(){
            addBar("progress", build -> {
                MultiCrafter.MultiCrafterBuild b = (MultiCrafter.MultiCrafterBuild) build;
                return new Bar(
                    () -> Core.bundle.get("bar.progress"),
                    () -> owner.progressColor,
                    () -> b.progress
                );
            });
        }

        public MultiCrafter owner(){ return owner; }

        @SuppressWarnings("unchecked")
        public <T extends Consume> T findConsumer(Boolf<Consume> filter){
            return consumers.length == 0
                ? (T)consumeBuilder.find(filter)
                : (T)Structs.find(consumers, filter);
        }

        public boolean hasConsumer(Consume cons){ return consumeBuilder.contains(cons); }

        public void removeConsumer(Consume cons){
            if(consumers.length > 0) return;
            consumeBuilder.remove(cons);
        }

        public void removeConsumers(Boolf<Consume> b){
            consumeBuilder.removeAll(b);
            if(!consumeBuilder.contains(c -> c instanceof ConsumePower)) consPower = null;
            if(!consumeBuilder.contains(c -> c instanceof ConsumePayloads)){
                consPayload = null;
                payloadRequirements.clear();
            }
        }

        public boolean getConsumeItem(Item item){
            return itemFilter.containsKey(item) && itemFilter.get(item);
        }

        public boolean getConsumeLiquid(Liquid liquid){
            return liquidFilter.containsKey(liquid) && liquidFilter.get(liquid);
        }

        public int payloadNeed(UnlockableContent content, Building build){
            if(content == null || consPayload == null || payloadRequirements.isEmpty()) return 0;
            float mult = consPayload.multiplier.get(build);
            int need = 0;
            for(PayloadStack stack : payloadRequirements){
                if(stack.item == content) need += Math.round(stack.amount * mult);
            }
            return need;
        }

        public void consumeLiquid(Liquid liquid, float amount){
            setApply(liquid);
            consume(new ConsumeLiquid(liquid, amount));
        }

        public void consumeLiquids(LiquidStack... stacks){
            for(LiquidStack s : stacks) setApply(s.liquid);
            consume(new ConsumeLiquids(stacks));
        }

        public void consumePower(float powerPerTick){
            consume(new ConsumePower(powerPerTick, 0.0f, false));
        }

        public void consumePayload(UnlockableContent content){ consumePayload(content, 1); }

        public void consumePayload(UnlockableContent content, int amount){
            consumePayloads(PayloadStack.with(content, amount));
        }

        public void consumePayloads(PayloadStack... payloads){
            consumePayloads(new Seq<>(payloads));
        }

        public void consumePayloads(Seq<PayloadStack> payloads){
            payloadRequirements.clear();
            payloadRequirements.addAll(payloads);
            Seq<PayloadStack> req = new Seq<>();
            req.addAll(payloadRequirements);
            consume(new ConsumePayloads(req));
        }

        public void consumeItem(Item item){
            consumeItem(item, 1);
        }

        public void consumeItem(Item item, int amount){
            setApply(item);
            consume(new ConsumeItems(new ItemStack[]{new ItemStack(item, amount)}));
        }

        public void consumeItems(ItemStack... items){
            for(ItemStack s : items) setApply(s.item);
            consume(new ConsumeItems(items));
        }

        public <T extends Consume> void consume(T consume){
            if(consume instanceof ConsumePower cp){
                consumeBuilder.removeAll(b -> b instanceof ConsumePower);
                consPower = cp;
            }
            if(consume instanceof ConsumePayloads cp){
                consumeBuilder.removeAll(b -> b instanceof ConsumePayloads);
                consPayload = cp;
                payloadRequirements.clear();
                payloadRequirements.addAll(cp.payloads);
            }
            consumeBuilder.add(consume);
        }
    }
}
