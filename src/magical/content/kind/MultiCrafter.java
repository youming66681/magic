package magical.content;

import arc.struct.Seq;
import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.Core;

import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.gen.Unit;
import mindustry.ui.Styles;
import mindustry.world.meta.StatValue;
import mindustry.Vars;

public class MultiCrafter extends Block {

    public Seq<Recipe> recipes = new Seq<>();

    public MultiCrafter(String name){
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        configurable = true;
    }
    public class MultiCrafterBuild extends Building{

        public int selected = 0;
        public float progress = 0f;

        @Override
        public void updateTile(){

            if(recipes.size == 0) return;

            Recipe r = recipes.get(selected);

            for(ItemStack in : r.input){
                if(items.get(in.item) < in.amount){
                    return;
                }
            }

            progress += edelta();

            if(progress >= r.craftTime){
                progress = 0f;

                for(ItemStack in : r.input){
                    items.remove(in.item, in.amount);
                }

                for(ItemStack out : r.output){
                    offload(out.item);
                }
            }
        }

        @Override
        public void buildConfiguration(Table table){
            table.clear();

            for(int i = 0; i < recipes.size; i++){
                int id = i;
                Recipe r = recipes.get(i);

                table.button(
                        new TextureRegionDrawable(r.output[0].item.uiIcon),
                        Icon.none,
                        () -> configure(id)
                ).size(48f);
            }
        }

        @Override
        public void configured(Unit player, Object value){
            if(value instanceof Integer){
                selected = (Integer)value;
            }
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(selected);
            write.f(progress);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            selected = read.i();
            progress = read.f();
        }
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.productionTime);
        stats.add(Stat.productionTime, table -> {
            for(Recipe r : recipes){
                table.row();
                table.add(Core.bundle.get("recipe." + r.key + ".desc")).left();
                table.add(" : " + (r.craftTime / 60f) + "s");
            }
        });
    }
}