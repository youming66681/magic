package magical.content;

import static mindustry.content.TechTree.*;
import mindustry.type.ItemStack;
import mindustry.content.Planets;
import arc.struct.Seq;
import mindustry.game.Objectives;
import mindustry.content.SectorPresets;
import mindustry.game.Objectives.SectorComplete;

import magical.content.MLItems;
import magical.content.MLBlocks;
import magical.content.MLUnitTypes;
import magical.content.MLPlanets;

public class MLTechTree {
    public static void load() {
        MLPlanets.cecilia.techTree = Planets.serpulo.techTree;

        TechNode root = nodeRoot("cecilia", MLBlocks.baseCore, () -> {
           //基础科技
            nodeProduce(MLItems.phantomSteel, () -> {
                nodeProduce(MLLiquids.PhantomSteelSolution, () -> {
                        });
                nodeProduce(MLItems.phantomTitaniumSteel, () -> {
                    nodeProduce(MLItems.logicChip, () -> {
                    });
                });
                    nodeProduce(MLItems.mysticCrystal, () -> {
                    });
            });
            /*幻钢传送带*/node(MLBlocks.phantomSteelConveyor, () -> {
                /*幻钛钢传送带*/node(MLBlocks.phantomTitaniumSteelConveyor, () -> {
                        });
                      /*幻钢装卸器*/node(MLBlocks.phantomSteelUnloader, () -> {
                              });
                /*幻钢连接器*/node(MLBlocks.phantomSteeljunction, () -> {
                });
                /*幻钢带桥*/node(MLBlocks.phantomSteelBridge, () -> {

                });
            });
            /*幻钢节点*/node(MLBlocks.phantomSteelPowerNode, () -> {
                /*燃能发电机*/node(MLBlocks.fuelPoweredGenerator, () -> {
                        });
                /*幻钛钢节点*/node(MLBlocks.phantomTitaniumSteelPowerNode, () -> {

                        });
                    });
            /*幻钢钻*/node(MLBlocks.phantomSteelDrill, () -> {
                    });
            /*电戈*/node(MLBlocks.electroge, () -> {
                /*裂光*/node(MLBlocks.Birefringence, Seq.with(new SectorComplete(MLSectorPresets.LandingZone)), () -> {
                        });
            });
       /*激沅*/node(MLBlocks.excitedYuan, () -> {
            /*流冲*/node(MLBlocks.fluvialErosion, Seq.with(new SectorComplete(MLSectorPresets.LandingZone)), ()-> {
                });
            });
            /*基础制造厂*/node(MLBlocks.BasicManufacturingPlant, () -> {
                /*微雨*/node(MLUnitTypes.drizzle, Seq.with(new Objectives.Research(MLBlocks.BasicManufacturingPlant), new SectorComplete(MLSectorPresets.LandingZone)), () -> {
                    /*细雨*/node(MLUnitTypes.Drizzle, Seq.with(new Objectives.Research(MLBlocks.curvatureEvolutionPod), new SectorComplete(MLSectorPresets.LandingZone)), () -> {
                        /*透雨*/node(MLUnitTypes.drizzlingRain, Seq.with(new Objectives.Research(MLBlocks.quantumFactory), new SectorComplete(MLSectorPresets.LandingZone)), () -> {

                        });
                    });
                        });
                /*微风*/node(MLUnitTypes.Breeze, Seq.with(new Objectives.Research(MLBlocks.BasicManufacturingPlant)), () -> {
                    /*斜风*/node(MLUnitTypes.SlantingWind, Seq.with(new Objectives.Research(MLBlocks.curvatureEvolutionPod)), () -> {
                        /*狂风*/node(MLUnitTypes.Gale, Seq.with(new Objectives.Research(MLBlocks.quantumFactory)), () -> {

                        });
                    });
                        });
                /*静水*/node(MLUnitTypes.StillWater, Seq.with(new Objectives.Research(MLBlocks.BasicManufacturingPlant)), () -> {
                    /*涟漪*/node(MLUnitTypes.ripple, Seq.with(new Objectives.Research(MLBlocks.curvatureEvolutionPod)), () -> {
                        /*湍流*/node(MLUnitTypes.Turbulence, Seq.with(new Objectives.Research(MLBlocks.quantumFactory)), () -> {

                        });
                    });
                });
            /*曲率进化舱*/node(MLBlocks.curvatureEvolutionPod, () -> {
                /*量子制造厂*/node(MLBlocks.quantumFactory, () -> {

                });
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
            /*幻钢墙*/node(MLBlocks.phantomSteelWall, () -> {
                /*大幻钢墙*/node(MLBlocks.largePhantomSteelWall, () -> {
                    /*幻钛钢墙*/node(MLBlocks.phantomTitaniumSteelWall, () -> {
                        /*大幻钛钢墙*/node(MLBlocks.largePhantomTitaniumSteelWall, () -> {
                        });
                            /*联合墙*/node(MLBlocks.adaptiveWall, () -> {
                                /*大联合墙*/node(MLBlocks.largeAdaptiveWall, () -> {
                            });
                        });
                    });
                });
            });
            //进阶科技
            /*基站核心*/node(MLBlocks.baseStationCore, () -> {
                nodeProduce(MLItems.wingedStone, () -> {
                  nodeProduce(MLItems.acrylic, () -> {
                    nodeProduce(MLItems.arrayChip, () -> {
                    });
                });
            });
                /*大塑钢*/node(MLBlocks.LargePlastaniumCompressor, () -> {
                        });
                /*翼石冲压机*/node(MLBlocks.WingStonePunchingMachine, () -> {
                    /*钢化玻璃强化器*/node(MLBlocks.metaglassBooster, () -> {

                    });
                });
                /*星港造舰中心*/node(MLBlocks.starHarborShipbuildingCenter, () -> {

                    /*星舰材料构造器*/node(MLBlocks.StarshipMaterialConstructor, () -> {

                                /*星舰材料解构器*/node(MLBlocks.StarshipMaterialDeconstructor, () -> {

                                });
                            });

                    /*星芒*/node(MLUnitTypes.Starlight, () -> {
                            });
                        /*箐霄*/node(MLUnitTypes.Qingxiao, () -> {
                        });
                   });
                /*幻晶*/node(MLBlocks.PhantomCrystal, () -> {
                    /*光降*/node(MLBlocks.LightDescends, () -> {
                        /*破军*/node(MLBlocks.BreakingArmy, () -> {

                        });
                    });
                        /*星云*/node(MLBlocks.Nebula, () -> {
                        });
                    });
            /*翼石墙*/node(MLBlocks.wingWall, () -> {
                /*大翼石墙*/node(MLBlocks.LargeWingWall, () -> {
                   });
                });
                /*电磁裂变炉*/node(MLBlocks.ElectromagneticFissionReactor, () -> {

                });
                /*兵戈*/node(MLUnitTypes.war, Seq.with(new Objectives.Research(MLBlocks.BasicManufacturingPlant)), () -> {
                    /*烽火*/node(MLUnitTypes.BeaconFire, Seq.with(new Objectives.Research(MLBlocks.curvatureEvolutionPod)), () -> {
                        /*战乱*/node(MLUnitTypes.War, Seq.with(new Objectives.Research(MLBlocks.quantumFactory)), () -> {

                        });
                    });
                });
                /*流火*/node(MLUnitTypes.BlazingFire, Seq.with(new Objectives.Research(MLBlocks.BasicManufacturingPlant)), () -> {
                    /*荧辉*/node(MLUnitTypes.glow, Seq.with(new Objectives.Research(MLBlocks.curvatureEvolutionPod)), () -> {
                        /*激耀*/node(MLUnitTypes.blazing, Seq.with(new Objectives.Research(MLBlocks.quantumFactory)), () -> {

                        });
                    });
                });
                /*驱暗*/node(MLUnitTypes.ExpelDarkness, Seq.with(new Objectives.Research(MLBlocks.BasicManufacturingPlant)), () -> {
                    /*逐光*/node(MLUnitTypes.ChasingLight, Seq.with(new Objectives.Research(MLBlocks.curvatureEvolutionPod)), () -> {
                        /*启明*/node(MLUnitTypes.Dawn, Seq.with(new Objectives.Research(MLBlocks.quantumFactory)), () -> {

                        });
                    });
                });
                /*终端核心*/node(MLBlocks.TerminalCore, () -> {
                    /*幻钢焚烧机*/node(MLBlocks.PhantomSteelIncinerator, () -> {
                        /*玄钢混制机*/node(MLBlocks.XuansteelMixer, () -> {
                            /*翼精金属合成器*/node(MLBlocks.WingEssenceMetalSynthesizer, () -> {
                            });
                            /*荧羽石反应器*/node(MLBlocks.LuminFeatherStoneReactor, () -> {
                            });
                        });
                    });
                });
            });
            /*降落区*/node(MLSectorPresets.LandingZone, Seq.with(new SectorComplete(SectorPresets.planetaryTerminal)), () -> {
            });
                 });

    }
}
