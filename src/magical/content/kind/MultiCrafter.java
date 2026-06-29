package magical.world;

import mindustry.type.*;

public class CraftRecipe{

    public ItemStack[] consumeItems = ItemStack.empty;
    public LiquidStack[] consumeLiquids = LiquidStack.empty;

    public ItemStack[] outputItems = ItemStack.empty;
    public LiquidStack outputLiquid;

    public float craftTime = 60f;
    public float power = 0f;

    public Effect craftEffect = Fx.none;

    public CraftRecipe(){}
}

public class MultiCrafter extends GenericCrafter {

    public Seq<CraftRecipe> recipes = new Seq<>();

    public MultiCrafter(String name) {
        super(name);

        configurable = true;

        config(Integer.class, (MultiCrafterBuild tile, Integer value) -> {
            tile.recipe = value;
        });

        configClear((MultiCrafterBuild tile) -> {
            tile.recipe = 0;
        });
    }

    public class MultiCrafterBuild extends GenericCrafterBuild {

        public int recipe;

        @Override
        public void write(Writes write) {
            super.write(write);

            write.i(recipe);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);

            recipe = read.i();
        }

        public CraftRecipe current() {

            if (recipe < 0 || recipe >= recipes.size)
                recipe = 0;

            return recipes.get(recipe);
        }

        public boolean hasItems() {

            CraftRecipe r = current();

            for (ItemStack stack : r.consumeItems) {

                if (items.get(stack.item) < stack.amount)
                    return false;
            }

            return true;
        }

        public void consumeItems() {

            CraftRecipe r = current();

            for (ItemStack stack : r.consumeItems) {

                items.remove(stack.item, stack.amount);
            }
        }

        public void produce() {

            CraftRecipe r = current();

            for (ItemStack stack : r.outputItems) {

                for (int i = 0; i < stack.amount; i++) {

                    offload(stack.item);
                }
            }
        }

        @Override
        public void updateTile() {

            CraftRecipe r = current();

            if (hasItems()) {

                progress += edelta() / r.craftTime;

                if (progress >= 1) {

                    consumeItems();

                    produce();

                    r.craftEffect.at(x, y);

                    progress = 0;
                }

            } else {

                progress = 0;
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            table.clear();

            for (int i = 0; i < recipes.size; i++) {

                int id = i;

                table.button("" + (i + 1), Styles.cleart, () -> {

                    configure(id);

                }).size(50);

                if (i % 3 == 2)
                    table.row();
            }
        }

    }
}