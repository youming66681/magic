package magical.content;

import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.gen.UnitType;
import mindustry.ui.Styles;
import mindustry.world.blocks.units.UnitAssembler;
import mindustry.world.blocks.units.UnitAssembler.UnitAssemblerBuild;


public class SelectableAssembler extends UnitAssembler {


    public UnitType[][] selectableUnits;


    public SelectableAssembler(String name){
        super(name);
    }


    public class SelectableAssemblerBuild extends UnitAssemblerBuild{


        public int selected = 0;


        @Override
        public UnitType unitType(){


            int tier = this.tier;


            if(selectableUnits == null)
                return super.unitType();


            if(tier >= selectableUnits.length)
                tier = selectableUnits.length - 1;


            UnitType[] list = selectableUnits[tier];


            if(list.length == 0)
                return null;


            return list[selected % list.length];
        }



        @Override
        public void buildConfiguration(Table table){


            table.clear();


            UnitType[] list =
                    selectableUnits[tier];


            for(int i=0;i<list.length;i++){


                int id=i;


                table.button(
                        list[i].icon(Cicon.medium),
                        Styles.clearNonei,
                        ()->{


                            selected=id;


                            deselect();


                            configure(id);
                        }

                ).size(50);


            }


            table.row();


            table.table(t->{


                t.add(
                        unitType().localizedName
                );

            });


        }



        @Override
        public Object config(){

            return selected;

        }


        @Override
        public void write(
                arc.util.io.Writes write
        ){

            super.write(write);

            write.i(selected);

        }



        @Override
        public void read(
                arc.util.io.Reads read,
                byte revision
        ){

            super.read(read,revision);

            selected=read.i();

        }

    }



    @Override
    public UnitAssemblerBuild buildType(){

        return new SelectableAssemblerBuild();

    }

}