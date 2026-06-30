package magical.content;

import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.draw.DrawBlock;

public class MultipleCrafter extends Block {

    public FormulaStack formulas;

    public boolean dumpExtraLiquid;
    public boolean ignoreLiquidFullness;

    public DrawBlock drawer;

    public MultipleCrafter(String name) {
        super(name);
    }

    @Override
    public void setStats() {
    }

    public void addLiquidBar(Liquid liq) {
    }

    public void setBars() {
    }

    @Override
    public void load() {
    }

    @Override
    public void init() {
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
    }

    @Override
    public TextureRegion[] icons() {
        return null;
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    public void getRegionsToOutline(Seq<TextureRegion> out) {
    }

    // =========================
    // Building class
    // =========================
    public class MultipleCrafterBuilding extends Building {

        public float progress;
        public float totalProgress;
        public float warmup;

        public int formulaIndex;
        public Formula formula;

        public Seq<ItemStack> outputItems = new Seq<>();
        public Seq<LiquidStack> outputLiquids = new Seq<>();

        public ConsumePower consPower;

        @Override
        public Object config() {
            return null;
        }

        @Override
        public void draw() {
        }

        @Override
        public void drawSelect() {
        }

        @Override
        public void drawLight() {
        }

        @Override
        public void drawStatus() {
        }

        @Override
        public boolean shouldConsume() {
            return true;
        }

        public void updateConsumption() {
        }

        public void displayConsumption(Table table) {
        }

        @Override
        public void updateTile() {
        }

        public float getProgressIncrease(float baseTime) {
            return 0f;
        }

        public float getPowerProduction() {
            return 0f;
        }

        public float warmup() {
            return warmup;
        }

        public float totalProgress() {
            return totalProgress;
        }

        private void craft() {
        }

        public void dumpOutputs() {
        }

        public void getContent(Table ta, Seq<Consume> consume) {
        }

        private Table add(Table t1, String s2) {
            return t1;
        }

        public void buildConfiguration(Table table) {
        }

        public float progress() {
            return progress;
        }

        public void setIndex(int index) {
            this.formulaIndex = index;
        }

        @Override
        public void write(Writes write) {
        }

        @Override
        public void read(Reads read, byte revision) {
        }
    }
}