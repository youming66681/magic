package magical.content;

import static mindustry.content.TechTree.*;
import mindustry.type.ItemStack;
import mindustry.content.Planets;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLPlanets;

public class MLTechTree {
    public static void load() {
        MLPlanets.cecilia.techTree = Planets.serpulo.techTree;

        TechNode root = nodeRoot("cecilia", MLBlocks.baseCore, () -> {
           //基础科技
            /*幻钢传送带*/node(MLBlocks.phantomSteelConveyor, () -> {
                      /*幻钢装卸器*/node(MLBlocks.phantomSteelUnloader, () -> {

                              });
                /*幻钢连接器*/node(MLBlocks.phantomSteeljunction, () -> {

                });
                /*幻钢带桥*/node(MLBlocks.phantomSteelBridge, () -> {

                });
            });
            /*幻钢钻*/node(MLBlocks.phantomSteelDrill, () -> {

                    });
            /*流冲*/node(MLBlocks.fluvialErosion, () -> {

            });
            /*电戈*/node(MLBlocks.electroge, () -> {
                /*裂光*/node(MLBlocks.Birefringence, () -> {

                });
            });
            /*幻钢压缩机*/node(MLBlocks.phantomSteelCompressor, () -> {
                /*幻钢电压机*/node(MLBlocks.phantomSteelVoltageMachine, () -> {
                    /*幻钛钢熔炼机*/node(MLBlocks.phantomTitaniumSteelCompressor, () -> {
                    });
                        /*玄晶混制机*/node(MLBlocks.xuanCrystalManufacturingMachine, () -> {
                             });
                       });
                    });
                 });
    }
}
