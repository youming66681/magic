package magical.content;

import arc.Core;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.meta.*;

import static mindustry.Vars.tilesize;

public final class MLUi{

    private MLUi(){}

    public static void statToTable(Stats stat, Table table){
        var cats = stat.toMap().keys().toSeq();
        for(int i = 0; i < cats.size; i++){
            var ss = stat.toMap().get(cats.get(i)).keys().toSeq();
            for(int j = 0; j < ss.size; j++){
                var vs = stat.toMap().get(cats.get(i)).get(ss.get(j));
                for(int k = 0; k < vs.size; k++){
                    vs.get(k).display(table);
                }
            }
        }
    }

    public static void statTurnTable(Stats stat, Table table){
        for(StatCat cat : stat.toMap().keys()){
            var map = stat.toMap().get(cat);
            if(map.size == 0) continue;

            if(stat.useCategories){
                table.add("@category." + cat.name).color(LPPal.accent.cpy()).fillX();
                table.row();
            }

            for(Stat s : map.keys()){
                table.table(inset -> {
                    inset.left();
                    inset.add("[lightgray]" + s.localized() + ":[] ").left().top();
                    Seq<StatValue> arr = map.get(s);
                    for(StatValue value : arr){
                        value.display(inset);
                        inset.add().size(10f);
                    }
                }).fillX().padLeft(10);
                table.row();
            }
        }
    }

    public static void buildRecipeConfig(MultiCrafter owner, MultiCrafter.MultiCrafterBuild build, Table table){
        Table rot = new Table();
        rot.left().defaults().size(55);

        Table cont = new Table().top();
        cont.left().defaults().left().growX();

        Runnable rebuild = () -> {
            rot.clearChildren();
            if(owner.hasDoubleOutput){
                for(int i = 0; i < 4; i++){
                    var button = new ImageButton();
                    int ii = i;
                    button.table(img -> img.image(Icon.right).color(Color.white).size(40).pad(10f));
                    button.changed(() -> build.configure(new int[]{ii, build.craftPlanIndex()}));
                    button.update(() -> button.setChecked(build.rotation == ii));
                    button.setStyle(Styles.clearNoneTogglei);
                    rot.add(button).tooltip(String.valueOf(i * 90));
                }
            }

            cont.clearChildren();

            int columns = 4;

            for(int i = 0; i < owner.craftPlans.size; i++){

                MultiCrafter.CraftPlan plan = owner.craftPlans.get(i);

                ImageButton button = new ImageButton(Styles.clearNoneTogglei);

                TextureRegion icon = Icon.cancel.getRegion();

                if(plan.outputItems.length > 0){
                    icon = plan.outputItems[0].item.uiIcon;
                }else if(plan.outputLiquids.length > 0){
                    icon = plan.outputLiquids[0].liquid.uiIcon;
                }

                button.image(icon).size(40);

                int index = i;

                button.changed(() ->
                        build.configure(new int[]{build.rotation, index})
                );

                button.update(() -> {
                    button.setChecked(build.craftPlan == plan);
                });

                button.addListener(new Tooltip(t -> {
                    t.background(Styles.black6);
                    t.add(plan.localizedName).pad(6);
                }));

                cont.add(button).size(60).pad(3);

                if((i + 1) % columns == 0){
                    cont.row();
                }
            }

    public static void buildRecipeBars(MultiCrafter owner, MultiCrafter.MultiCrafterBuild build, Table table){
        table.clear();
        for(Func<Building, Bar> bar : owner.listBars()){
            Bar result = bar.get(build);
            if(result != null){
                table.add(result).growX();
                table.row();
            }
        }
        if(build.craftPlan == null || !build.craftPlan.hasBars()) return;
        for(Func<Building, Bar> bar : build.craftPlan.listBars()){
            Bar result = bar.get(build);
            if(result == null) continue;
            table.add(result).growX();
            table.row();
        }
    }

    public static void buildConsumption(MultiCrafter.MultiCrafterBuild build, Table table){
        if(build.craftPlan == null) return;
        table.left();
        MultiCrafter.CraftPlan[] last = {build.craftPlan};
        table.table(t -> {
            table.update(() -> {
                if(last[0] != build.craftPlan){
                    rebuildConsumption(build, t);
                    last[0] = build.craftPlan;
                }
            });
            rebuildConsumption(build, t);
        });
    }

    private static void rebuildConsumption(MultiCrafter.MultiCrafterBuild build, Table table){
        table.clear();
        for(var cons : build.craftPlan.consumers){
            if(!cons.optional || !cons.booster){
                cons.build(build, table);
            }
        }
    }
}
