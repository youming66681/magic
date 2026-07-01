//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package magical.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.IntSet;
import arc.struct.Seq;
import arc.util.ArcRuntimeException;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import java.util.Objects;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadSeq;
import mindustry.type.PayloadStack;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadBlock;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.consumers.ConsumeItemDynamic;
import mindustry.world.consumers.ConsumePowerDynamic;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class MultiCrafter extends PayloadBlock {
    public boolean hasHeat = false;
    public boolean hasPayloads = false;
    public float powerCapacity = 0.0F;
    public int payloadCapacity = 1;
    public float itemCapacityMultiplier = 1.0F;
    public float fluidCapacityMultiplier = 1.0F;
    public float powerCapacityMultiplier = 1.0F;
    public float payloadCapacityMultiplier = 2.0F;
    public Object recipes;
    @Nullable
    public Seq<Recipe> resolvedRecipes = null;
    public String menu = "transform";
    @Nullable
    public RecipeSwitchStyle switchStyle = null;
    public Effect craftEffect;
    public Effect updateEffect;
    public Effect changeRecipeEffect;
    public int[] fluidOutputDirections;
    public float updateEffectChance;
    public float warmupSpeed;
    public boolean ignoreLiquidFullness;
    public boolean dumpExtraFluid;
    public DrawBlock drawer;
    protected boolean isOutputItem;
    protected boolean isConsumeItem;
    protected boolean isOutputFluid;
    protected boolean isConsumeFluid;
    protected boolean isOutputPower;
    protected boolean isConsumePower;
    protected boolean isOutputHeat;
    protected boolean isConsumeHeat;
    protected boolean isOutputPayload;
    protected boolean isConsumePayload;
    public Color heatColor;
    public int defaultRecipeIndex;
    public float overheatScale;
    public float maxEfficiency;
    public float warmupRate;
    protected boolean showNameTooltip;
    @Nullable
    protected static Table hoveredInfo;

    public MultiCrafter(String name) {
        super(name);
        this.craftEffect = Fx.none;
        this.updateEffect = Fx.none;
        this.changeRecipeEffect = Fx.rotateBlock;
        this.fluidOutputDirections = new int[]{-1};
        this.updateEffectChance = 0.04F;
        this.warmupSpeed = 0.019F;
        this.ignoreLiquidFullness = false;
        this.dumpExtraFluid = true;
        this.drawer = new DrawDefault();
        this.isOutputItem = false;
        this.isConsumeItem = false;
        this.isOutputFluid = false;
        this.isConsumeFluid = false;
        this.isOutputPower = false;
        this.isConsumePower = false;
        this.isOutputHeat = false;
        this.isConsumeHeat = false;
        this.isOutputPayload = false;
        this.isConsumePayload = false;
        this.heatColor = new Color(1.0F, 0.22F, 0.22F, 0.8F);
        this.defaultRecipeIndex = 0;
        this.overheatScale = 1.0F;
        this.maxEfficiency = 1.0F;
        this.warmupRate = 0.15F;
        this.showNameTooltip = false;
        this.update = true;
        this.solid = true;
        this.sync = true;
        this.flags = EnumSet.of(new BlockFlag[]{BlockFlag.factory});
        this.ambientSound = Sounds.loopMachine;
        this.configurable = true;
        this.saveConfig = true;
        this.ambientSoundVolume = 0.03F;
        this.config(Integer.class, MultiCrafterBuild::setCurRecipeIndexFromRemote);
    }

    public void init() {
        this.hasItems = false;
        this.hasLiquids = false;
        this.hasPower = false;
        this.hasHeat = false;
        this.hasPayloads = false;
        this.outputsPower = false;
        this.outputsPayload = false;
        MultiCrafterParser parser = new MultiCrafterParser();
        if (this.resolvedRecipes == null && this.recipes != null) {
            this.resolvedRecipes = parser.parse(this, this.recipes);
        }

        if (this.resolvedRecipes != null && !this.resolvedRecipes.isEmpty()) {
            if (this.switchStyle == null) {
                this.switchStyle = RecipeSwitchStyle.get(this.menu);
            }

            this.decorateRecipes();
            this.setupBlockByRecipes();
            this.defaultRecipeIndex = Mathf.clamp(this.defaultRecipeIndex, 0, this.resolvedRecipes.size - 1);
            this.recipes = null;
            this.setupConsumers();
            super.init();
        } else {
            throw new ArcRuntimeException(MultiCrafterParser.genName(this) + " has no recipe! It's perhaps because all recipes didn't find items, fluids or payloads they need. Check your `last_log.txt` to obtain more information.");
        }
    }

    public void setStats() {
        super.setStats();
        this.stats.add(Stat.output, (t) -> {
            this.showNameTooltip = true;
            this.buildStats(t);
            this.showNameTooltip = false;
        });
    }

    public void buildStats(Table stat) {
        stat.row();

        for(Recipe recipe : this.resolvedRecipes) {
            Table t = new Table();
            t.background(Tex.whiteui);
            t.setColor(Pal.darkestGray);
            this.buildIOEntry(t, recipe, true);
            Table time = new Table();
            float[] duration = new float[]{0.0F};
            float visualCraftTime = recipe.craftTime;
            time.update(() -> {
                duration[0] += Time.delta;
                if (duration[0] > visualCraftTime) {
                    duration[0] = 0.0F;
                }

            });
            String craftTime = recipe.craftTime == 0.0F ? "0" : String.format("%.2f", recipe.craftTime / 60.0F);
            Cell<Bar> barCell = time.add(new Bar(() -> craftTime, () -> Pal.accent, () -> Interp.smooth.apply(duration[0] / visualCraftTime))).height(45.0F);
            barCell.width(Vars.mobile ? 220.0F : 250.0F);
            Cell<Table> timeCell = t.add(time).pad(12.0F);
            if (this.showNameTooltip) {
                String var10001 = Stat.productionTime.localized();
                timeCell.tooltip(var10001 + ": " + craftTime + " " + StatUnit.seconds.localized());
            }

            this.buildIOEntry(t, recipe, false);
            stat.add(t).pad(10.0F).grow();
            stat.row();
        }

        stat.row();
        stat.defaults().grow();
    }

    protected void buildIOEntry(Table table, Recipe recipe, boolean isInput) {
        Table t = new Table();
        if (isInput) {
            t.left();
        } else {
            t.right();
        }

        Table mat = new Table();
        IOEntry entry = isInput ? recipe.input : recipe.output;
        int i = 0;

        for(ItemStack stack : entry.items) {
            Cell<FluidImage> iconCell = mat.add(new FluidImage(stack.item.uiIcon, (float)stack.amount)).pad(2.0F);
            if (isInput) {
                iconCell.left();
            } else {
                iconCell.right();
            }

            if (this.showNameTooltip) {
                iconCell.tooltip(stack.item.localizedName);
            }

            if (i != 0 && i % 2 == 0) {
                mat.row();
            }

            ++i;
        }

        for(LiquidStack stack : entry.fluids) {
            Cell<FluidImage> iconCell = mat.add(new FluidImage(stack.liquid.uiIcon, stack.amount * 60.0F)).pad(2.0F);
            if (isInput) {
                iconCell.left();
            } else {
                iconCell.right();
            }

            if (this.showNameTooltip) {
                iconCell.tooltip(stack.liquid.localizedName);
            }

            if (i != 0 && i % 2 == 0) {
                mat.row();
            }

            ++i;
        }

        if (entry.power > 0.0F) {
            Cell<PowerImage> iconCell = mat.add(new PowerImage(entry.power * 60.0F)).pad(2.0F);
            if (isInput) {
                iconCell.left();
            } else {
                iconCell.right();
            }

            if (this.showNameTooltip) {
                float var10001 = entry.power;
                iconCell.tooltip(var10001 + " " + StatUnit.powerSecond.localized());
            }

            ++i;
            if (i != 0 && i % 2 == 0) {
                mat.row();
            }
        }

        if (entry.heat > 0.0F) {
            Cell<HeatImage> iconCell = mat.add(new HeatImage(entry.heat)).pad(2.0F);
            if (isInput) {
                iconCell.left();
            } else {
                iconCell.right();
            }

            if (this.showNameTooltip) {
                float var27 = entry.heat;
                iconCell.tooltip(var27 + " " + StatUnit.heatUnits.localized());
            }

            ++i;
            if (i != 0 && i % 2 == 0) {
                mat.row();
            }
        }

        for(PayloadStack stack : entry.payloads) {
            Cell<PayloadImage> iconCell = mat.add(new PayloadImage(stack.item.uiIcon, (float)stack.amount)).pad(2.0F);
            if (this.showNameTooltip) {
                iconCell.tooltip(stack.item.localizedName);
            }

            if (isInput) {
                iconCell.left();
            } else {
                iconCell.right();
            }

            if (i != 0 && i % 2 == 0) {
                mat.row();
            }

            ++i;
        }

        Cell<Table> matCell = t.add(mat);
        if (isInput) {
            matCell.left();
        } else {
            matCell.right();
        }

        Cell<Table> tCell = table.add(t).pad(12.0F).fill();
        tCell.width(Vars.mobile ? 90.0F : 120.0F);
    }

    public void setBars() {
        super.setBars();
        if (this.hasPower) {
            this.addBar("power", (b) -> new Bar(b.getCurRecipe().isOutputPower() ? Core.bundle.format("bar.poweroutput", new Object[]{Strings.fixed(b.getPowerProduction() * 60.0F * b.timeScale(), 1)}) : "bar.power", Pal.powerBar, () -> b.efficiency));
        }

        if (this.isConsumeHeat || this.isOutputHeat) {
            this.addBar("heat", (b) -> {
                String var10002 = b.getCurRecipe().isConsumeHeat() ? Core.bundle.format("bar.heatpercent", new Object[]{(int)(b.heat + 0.01F), (int)(b.efficiencyScale() * 100.0F + 0.01F)}) : "bar.heat";
                Color var10003 = Pal.lightOrange;
                Objects.requireNonNull(b);
                return new Bar(var10002, var10003, b::heatFrac);
            });
        }

        this.addBar("progress", (b) -> {
            Color var10003 = Pal.accent;
            Objects.requireNonNull(b);
            return new Bar("bar.loadprogress", var10003, b::progress);
        });
    }

    public boolean rotatedOutput(int x, int y) {
        return false;
    }

    public void load() {
        super.load();
        this.drawer.load(this);
    }

    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        this.drawer.drawPlan(this, plan, list);
    }

    public TextureRegion[] icons() {
        return this.drawer.finalIcons(this);
    }

    public void getRegionsToOutline(Seq<TextureRegion> out) {
        this.drawer.getRegionsToOutline(this, out);
    }

    public boolean outputsItems() {
        return this.isOutputItem;
    }

    public void drawOverlay(float x, float y, int rotation) {
        Recipe firstRecipe = (Recipe)this.resolvedRecipes.get(this.defaultRecipeIndex);
        LiquidStack[] fluids = firstRecipe.output.fluids;

        for(int i = 0; i < fluids.length; ++i) {
            int dir = this.fluidOutputDirections.length > i ? this.fluidOutputDirections[i] : -1;
            if (dir != -1) {
                Draw.rect(fluids[i].liquid.fullIcon, x + (float)Geometry.d4x(dir + rotation) * ((float)(this.size * 8) / 2.0F + 4.0F), y + (float)Geometry.d4y(dir + rotation) * ((float)(this.size * 8) / 2.0F + 4.0F), 8.0F, 8.0F);
            }
        }

    }

    protected void decorateRecipes() {
        this.resolvedRecipes.shrink();

        for(Recipe recipe : this.resolvedRecipes) {
            recipe.cacheUnique();
        }

    }

    protected void setupBlockByRecipes() {
        int maxItemAmount = 0;
        float maxFluidAmount = 0.0F;
        float maxPower = 0.0F;
        float maxHeat = 0.0F;
        int maxPayloadAmount = 0;

        for(Recipe recipe : this.resolvedRecipes) {
            this.hasItems |= recipe.hasItems();
            this.hasLiquids |= recipe.hasFluids();
            this.conductivePower = this.hasPower |= recipe.hasPower();
            this.hasHeat |= recipe.hasHeat();
            this.hasPayloads |= recipe.hasPayloads();
            maxItemAmount = Math.max(recipe.maxItemAmount(), maxItemAmount);
            maxFluidAmount = Math.max(recipe.maxFluidAmount(), maxFluidAmount);
            maxPower = Math.max(recipe.maxPower(), maxPower);
            maxHeat = Math.max(recipe.maxHeat(), maxHeat);
            maxPayloadAmount = Math.max(recipe.maxPayloadAmount(), maxPayloadAmount);
            this.isOutputItem |= recipe.isOutputItem();
            this.acceptsItems = this.isConsumeItem |= recipe.isConsumeItem();
            this.outputsLiquid = this.isOutputFluid |= recipe.isOutputFluid();
            this.isConsumeFluid |= recipe.isConsumeFluid();
            this.outputsPower = this.isOutputPower |= recipe.isOutputPower();
            this.consumesPower = this.isConsumePower |= recipe.isConsumePower();
            this.isOutputHeat |= recipe.isOutputHeat();
            this.isConsumeHeat |= recipe.isConsumeHeat();
            this.outputsPayload = this.isOutputPayload |= recipe.isOutputPayload();
            this.acceptsPayload = this.isConsumePayload |= recipe.isConsumePayload();
        }

        this.itemCapacity = Math.max((int)((float)maxItemAmount * this.itemCapacityMultiplier), this.itemCapacity);
        this.liquidCapacity = Math.max((float)((int)(maxFluidAmount * 60.0F * this.fluidCapacityMultiplier)), this.liquidCapacity);
        this.powerCapacity = Math.max(maxPower * 60.0F * this.powerCapacityMultiplier, this.powerCapacity);
        this.payloadCapacity = Math.max((int)((float)maxPayloadAmount * this.payloadCapacityMultiplier), this.payloadCapacity);
        if (this.isOutputHeat) {
            this.rotate = true;
            this.rotateDraw = false;
            this.canOverdrive = false;
            this.drawArrow = true;
        }

    }

    protected void setupConsumers() {
        if (this.isConsumeItem) {
            this.consume(new ConsumeItemDynamic((b) -> b.getCurRecipe().input.items));
        }

        if (this.isConsumeFluid) {
            this.consume(new ConsumeFluidDynamic((b) -> b.getCurRecipe().input.fluids));
        }

        if (this.isConsumePower) {
            this.consume(new ConsumePowerDynamic((b) -> ((MultiCrafterBuild)b).getCurRecipe().input.power));
        }

        if (this.isConsumePayload) {
            this.consume(new CustomConsumePayloadDynamic((b) -> b.getCurRecipe().input.payloads));
        }

    }

    public class MultiCrafterBuild extends PayloadBlock.PayloadBlockBuild<Payload> implements HeatBlock, HeatConsumer {
        public float[] sideHeat = new float[4];
        public float heat = 0.0F;
        public float craftingTime;
        public float totalProgress;
        public float warmup;
        public int curRecipeIndex;
        public int maxpayload;
        public PayloadSeq payloads;

        public MultiCrafterBuild() {
            super(MultiCrafter.this);
            this.curRecipeIndex = MultiCrafter.this.defaultRecipeIndex;
            this.payloads = new PayloadSeq();
        }

        public void setCurRecipeIndexFromRemote(int index) {
            int newIndex = Mathf.clamp(index, 0, MultiCrafter.this.resolvedRecipes.size - 1);
            if (newIndex != this.curRecipeIndex) {
                this.curRecipeIndex = newIndex;
                this.createEffect(MultiCrafter.this.changeRecipeEffect);
                this.craftingTime = 0.0F;
                if (!Vars.headless) {
                    this.rebuildHoveredInfo();
                }
            }

        }

        public Recipe getCurRecipe() {
            this.curRecipeIndex = Mathf.clamp(this.curRecipeIndex, 0, MultiCrafter.this.resolvedRecipes.size - 1);
            return (Recipe)MultiCrafter.this.resolvedRecipes.get(this.curRecipeIndex);
        }

        public boolean acceptItem(Building source, Item item) {
            return MultiCrafter.this.hasItems && this.getCurRecipe().input.itemsUnique.contains(item) && this.items.get(item) < this.getMaximumAccepted(item);
        }

        public boolean acceptLiquid(Building source, Liquid liquid) {
            return MultiCrafter.this.hasLiquids && this.getCurRecipe().input.fluidsUnique.contains(liquid) && this.liquids.get(liquid) < MultiCrafter.this.liquidCapacity;
        }

        public boolean acceptPayload(Building source, Payload payload) {
            return MultiCrafter.this.hasPayloads && this.payload == null && this.getCurRecipe().input.payloadsUnique.contains(payload.content()) && this.payloads.get(payload.content()) < MultiCrafter.this.payloadCapacity;
        }

        public PayloadSeq getPayloads() {
            return this.payloads;
        }

        public void yeetPayload(Payload payload) {
            this.payloads.add(payload.content(), 1);
        }

        public float edelta() {
            Recipe cur = this.getCurRecipe();
            return cur.input.power > 0.0F ? this.efficiency * Mathf.clamp(this.getCurPowerStore() / cur.input.power) * this.delta() : this.efficiency * this.delta();
        }

        public void updateTile() {
            Recipe cur = this.getCurRecipe();
            float craftTimeNeed = cur.craftTime;
            if (cur.isConsumeHeat()) {
                this.heat = this.calculateHeat(this.sideHeat);
            }

            if (cur.isOutputHeat()) {
                float heatOutput = cur.output.heat;
                this.heat = Mathf.approachDelta(this.heat, heatOutput * this.efficiency, MultiCrafter.this.warmupRate * this.edelta());
            }

            if (this.efficiency > 0.0F && (!MultiCrafter.this.hasPower || this.getCurPowerStore() >= cur.input.power) && craftTimeNeed > 0.0F) {
                this.craftingTime += this.edelta();
            }

            this.warmup = Mathf.approachDelta(this.warmup, this.warmupTarget(), MultiCrafter.this.warmupSpeed);
            if (MultiCrafter.this.hasPower) {
                float powerChange = (cur.output.power - cur.input.power) * this.delta();
                if (!Mathf.zero(powerChange)) {
                    this.setCurPowerStore(this.getCurPowerStore() + powerChange);
                }
            }

            if (cur.isOutputFluid()) {
                float increment = this.getProgressIncrease(1.0F);

                for(LiquidStack output : cur.output.fluids) {
                    Liquid fluid = output.liquid;
                    this.handleLiquid(this, fluid, Math.min(output.amount * increment, MultiCrafter.this.liquidCapacity - this.liquids.get(fluid)));
                }
            }

            if (this.wasVisible && Mathf.chanceDelta((double)MultiCrafter.this.updateEffectChance)) {
                MultiCrafter.this.updateEffect.at(this.x + Mathf.range((float)MultiCrafter.this.size * 4.0F), this.y + (float)Mathf.range(MultiCrafter.this.size * 4));
            } else {
                this.warmup = Mathf.approachDelta(this.warmup, 0.0F, MultiCrafter.this.warmupSpeed);
            }

            this.totalProgress += this.warmup * Time.delta;
            if (this.moveInPayload()) {
                this.yeetPayload(this.payload);
                this.payload = null;
            }

            if (cur.isOutputPayload() && this.craftingTime >= craftTimeNeed) {
                for(PayloadStack output : cur.output.payloads) {
                    Payload payloadOutput = null;
                    if (output.item instanceof Block) {
                        payloadOutput = new BuildPayload((Block)output.item, this.team);
                    } else if (output.item instanceof UnitType) {
                        payloadOutput = new UnitPayload(((UnitType)output.item).create(this.team));
                    }

                    if (payloadOutput != null) {
                        boolean qwq = this.dumpPayload(payloadOutput);
                        if (!qwq) {
                            ++this.maxpayload;
                        }
                    }
                }
            }

            if (craftTimeNeed <= 0.0F) {
                if (this.efficiency > 0.0F) {
                    this.craft();
                }
            } else if (this.craftingTime >= craftTimeNeed) {
                this.craft();
            }

            this.updateBars();
            this.dumpOutputs();
        }

        public void updateBars() {
            MultiCrafter.this.barMap.clear();
            MultiCrafter.this.setBars();
        }

        public boolean shouldConsume() {
            Recipe cur = this.getCurRecipe();
            if (MultiCrafter.this.hasItems) {
                for(ItemStack output : cur.output.items) {
                    if (this.items.get(output.item) + output.amount > MultiCrafter.this.itemCapacity) {
                        return false;
                    }
                }
            }

            if (MultiCrafter.this.hasLiquids && cur.isOutputFluid() && !MultiCrafter.this.ignoreLiquidFullness) {
                boolean allFull = true;

                for(LiquidStack output : cur.output.fluids) {
                    if (this.liquids.get(output.liquid) >= MultiCrafter.this.liquidCapacity - 0.001F) {
                        if (!MultiCrafter.this.dumpExtraFluid) {
                            return false;
                        }
                    } else {
                        allFull = false;
                    }
                }

                if (allFull) {
                    return false;
                }
            }

            return cur.isOutputPayload() && this.maxpayload > 9 ? false : this.enabled;
        }

        public void craft() {
            this.consume();
            Recipe cur = this.getCurRecipe();
            if (cur.isOutputItem()) {
                for(ItemStack output : cur.output.items) {
                    for(int i = 0; i < output.amount; ++i) {
                        this.offload(output.item);
                    }
                }
            }

            if (this.wasVisible) {
                this.createCraftEffect();
            }

            if (cur.craftTime > 0.0F) {
                this.craftingTime %= cur.craftTime;
            } else {
                this.craftingTime = 0.0F;
            }

        }

        public void createCraftEffect() {
            Recipe cur = this.getCurRecipe();
            Effect curFx = cur.craftEffect;
            Effect fx = curFx != Fx.none ? curFx : MultiCrafter.this.craftEffect;
            this.createEffect(fx);
        }

        public void dumpOutputs() {
            Recipe cur = this.getCurRecipe();
            if (this.timer(MultiCrafter.this.timerDump, (float)MultiCrafter.this.dumpTime / this.timeScale) && cur.isOutputItem()) {
                for(ItemStack output : cur.output.items) {
                    this.dump(output.item);
                }
            }

            if (cur.isOutputFluid()) {
                LiquidStack[] fluids = cur.output.fluids;

                for(int i = 0; i < fluids.length; ++i) {
                    int dir = MultiCrafter.this.fluidOutputDirections.length > i ? MultiCrafter.this.fluidOutputDirections[i] : -1;
                    this.dumpLiquid(fluids[i].liquid, 2.0F, dir);
                }
            }

            if (cur.isOutputPayload() && this.maxpayload > 0) {
                for(int i = 0; i < this.maxpayload + 1; ++i) {
                    for(PayloadStack output : cur.output.payloads) {
                        Payload payloadOutput = null;
                        if (output.item instanceof Block) {
                            payloadOutput = new BuildPayload((Block)output.item, this.team);
                        } else if (output.item instanceof UnitType) {
                            payloadOutput = new UnitPayload(((UnitType)output.item).create(this.team));
                        }

                        if (payloadOutput != null) {
                            boolean qwq = this.dumpPayload(payloadOutput);
                            if (qwq) {
                                --this.maxpayload;
                            }
                        }
                    }
                }
            }

        }

        public float heat() {
            return this.heat;
        }

        public float heatFrac() {
            Recipe cur = this.getCurRecipe();
            if (MultiCrafter.this.isOutputHeat && cur.isOutputHeat()) {
                return this.heat / cur.output.heat;
            } else {
                return MultiCrafter.this.isConsumeHeat && cur.isConsumeHeat() ? this.heat / cur.input.heat : 0.0F;
            }
        }

        public float[] sideHeat() {
            return this.sideHeat;
        }

        public float heatRequirement() {
            Recipe cur = this.getCurRecipe();
            return MultiCrafter.this.isConsumeHeat && cur.isConsumeHeat() ? cur.input.heat : 0.0F;
        }

        public float calculateHeat(float[] sideHeat) {
            for(Point2 edge : this.block.getEdges()) {
                Building build = this.nearby(edge.x, edge.y);
                if (build != null && build.team == this.team && build instanceof HeatBlock heater) {
                    if (!(heater instanceof MultiCrafterBuild)) {
                        return this.calculateHeat(sideHeat, (IntSet)null);
                    }

                    MultiCrafterBuild multi = (MultiCrafterBuild)heater;
                    if (multi.getCurRecipe().isOutputHeat()) {
                        return this.calculateHeat(sideHeat, (IntSet)null);
                    }
                }
            }

            return 0.0F;
        }

        public float getPowerProduction() {
            Recipe cur = this.getCurRecipe();
            return MultiCrafter.this.isOutputPower && cur.isOutputPower() ? cur.output.power * this.efficiency : 0.0F;
        }

        public void buildConfiguration(Table table) {
            MultiCrafter.this.switchStyle.build(MultiCrafter.this, this, table);
        }

        public float getCurPowerStore() {
            return this.power == null ? 0.0F : this.power.status * MultiCrafter.this.powerCapacity;
        }

        public void setCurPowerStore(float powerStore) {
            if (this.power != null) {
                this.power.status = Mathf.clamp(powerStore / MultiCrafter.this.powerCapacity);
            }
        }

        public void draw() {
            MultiCrafter.this.drawer.draw(this);
        }

        public void drawLight() {
            super.drawLight();
            MultiCrafter.this.drawer.drawLight(this);
        }

        public Object config() {
            return this.curRecipeIndex;
        }

        public boolean shouldAmbientSound() {
            return this.efficiency > 0.0F;
        }

        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress) {
                return (double)this.progress();
            } else {
                return sensor == LAccess.heat ? (double)this.warmup() : super.sense(sensor);
            }
        }

        public void write(Writes write) {
            super.write(write);
            write.f(this.craftingTime);
            write.f(this.warmup);
            write.i(this.curRecipeIndex);
            write.f(this.heat);
            write.i(this.maxpayload);
            if (this.getCurRecipe().isConsumePayload()) {
                this.payloads.write(write);
            }

        }

        public void read(Reads read, byte revision) {
            super.read(read, revision);
            this.craftingTime = read.f();
            this.warmup = read.f();
            this.curRecipeIndex = Mathf.clamp(read.i(), 0, MultiCrafter.this.resolvedRecipes.size - 1);
            this.heat = read.f();
            this.maxpayload = read.i();
            if (this.getCurRecipe().isConsumePayload()) {
                this.payloads.read(read);
            }

        }

        public float warmupTarget() {
            Recipe cur = this.getCurRecipe();
            return MultiCrafter.this.isConsumeHeat && cur.isConsumeHeat() ? Mathf.clamp(this.heat / cur.input.heat) : 1.0F;
        }

        public void updateEfficiencyMultiplier() {
            Recipe cur = this.getCurRecipe();
            if (MultiCrafter.this.isConsumeHeat && cur.isConsumeHeat()) {
                this.efficiency *= this.efficiencyScale();
                this.potentialEfficiency *= this.efficiencyScale();
            }

        }

        public float efficiencyScale() {
            Recipe cur = this.getCurRecipe();
            if (MultiCrafter.this.isConsumeHeat && cur.isConsumeHeat()) {
                float heatRequirement = cur.input.heat;
                float over = Math.max(this.heat - heatRequirement, 0.0F);
                return Math.min(Mathf.clamp(this.heat / heatRequirement) + over / heatRequirement * MultiCrafter.this.overheatScale, MultiCrafter.this.maxEfficiency);
            } else {
                return 1.0F;
            }
        }

        public float warmup() {
            return this.warmup;
        }

        public float progress() {
            Recipe cur = this.getCurRecipe();
            return Mathf.clamp(cur.craftTime > 0.0F ? this.craftingTime / cur.craftTime : 1.0F);
        }

        public float totalProgress() {
            return this.totalProgress;
        }

        public void display(Table table) {
            super.display(table);
            MultiCrafter.hoveredInfo = table;
        }

        public void rebuildHoveredInfo() {
            try {
                Table info = MultiCrafter.hoveredInfo;
                if (info != null) {
                    info.clear();
                    this.display(info);
                }
            } catch (Exception var2) {
            }

        }

        public void createEffect(Effect effect) {
            if (effect != Fx.none) {
                if (effect == Fx.placeBlock) {
                    effect.at(this.x, this.y, (float)this.block.size);
                } else if (effect == Fx.coreBuildBlock) {
                    effect.at(this.x, this.y, 0.0F, this.block);
                } else if (effect == Fx.upgradeCore) {
                    effect.at(this.x, this.y, 0.0F, this.block);
                } else if (effect == Fx.upgradeCoreBloom) {
                    effect.at(this.x, this.y, (float)this.block.size);
                } else if (effect == Fx.rotateBlock) {
                    effect.at(this.x, this.y, (float)this.block.size);
                } else {
                    effect.at(this.x, this.y, 0.0F, this);
                }

            }
        }
    }
}
