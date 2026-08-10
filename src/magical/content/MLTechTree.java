package magical.content;

import static mindustry.content.TechTree.*;
import mindustry.type.ItemStack;
import mindustry.content.Planets;
import arc.struct.Seq;
import mindustry.game.Objectives;
import mindustry.content.SectorPresets;
import mindustry.game.Objectives.SectorComplete;
import mindustry.game.Objectives.OnSector;

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
            /*幻钢传送带*/node(MLBlocks.phantomSteelConveyor, Seq.with(new OnSector(MLSectorPresets.LandingZone)), () -> {
                /*幻钢液体桥*/node(MLBlocks.PhantomSteelLiquidBridge, () -> {
                        });
                /*幻钛钢传送带*/node(MLBlocks.phantomTitaniumSteelConveyor, () -> {
                        });
                      /*幻钢装卸器*/node(MLBlocks.phantomSteelUnloader, () -> {
                              });
                /*幻钢连接器*/node(MLBlocks.phantomSteeljunction, () -> {
                });
                /*幻钢带桥*/node(MLBlocks.phantomSteelBridge, () -> {

                });
            });
            /*幻钢节点*/node(MLBlocks.phantomSteelPowerNode, Seq.with(new OnSector(MLSectorPresets.LandingZone)), () -> {
                /*燃能发电机*/node(MLBlocks.fuelPoweredGenerator, () -> {
                        });
                /*幻钛钢节点*/node(MLBlocks.phantomTitaniumSteelPowerNode, () -> {

                        });
                    });
            /*幻钢钻*/node(MLBlocks.phantomSteelDrill, () -> {
                    });
            /*电戈*/node(MLBlocks.electroge, Seq.with(new OnSector(MLSectorPresets.LandingZone)), () -> {
                /*裂光*/node(MLBlocks.Birefringence, Seq.with(new SectorComplete(MLSectorPresets.LandingZone)), () -> {
                        });
            });
       /*激沅*/node(MLBlocks.excitedYuan, Seq.with(new OnSector(MLSectorPresets.LandingZone)), () -> {
            /*流冲*/node(MLBlocks.fluvialErosion, Seq.with(new SectorComplete(MLSectorPresets.LandingZone)), ()-> {
                });
            });
            /*基础制造厂*/node(MLBlocks.BasicManufacturingPlant, Seq.with(new OnSector(MLSectorPresets.LandingZone)), () -> {
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
                /*静水*/node(MLUnitTypes.StillWater, Seq.with(new Objectives.Research(MLBlocks.BasicManufacturingPlant), new SectorComplete(MLSectorPresets.DeepSecludedJungle)), () -> {
                    /*涟漪*/node(MLUnitTypes.ripple, Seq.with(new Objectives.Research(MLBlocks.curvatureEvolutionPod)), () -> {
                        /*湍流*/node(MLUnitTypes.Turbulence, Seq.with(new Objectives.Research(MLBlocks.quantumFactory)), () -> {

                        });
                    });
                });
            /*曲率进化舱*/node(MLBlocks.curvatureEvolutionPod, () -> {
                /*量子制造厂*/node(MLBlocks.quantumFactory, Seq.with(new SectorComplete(MLSectorPresets.LandingZone)), () -> {

                });
                    });
            });
            /*幻钢压缩机*/node(MLBlocks.phantomSteelCompressor, Seq.with(new OnSector(MLSectorPresets.LandingZone)), () -> {
                /*幻钢电压机*/node(MLBlocks.phantomSteelVoltageMachine, Seq.with(new SectorComplete(MLSectorPresets.LandingZone)), () -> {
                    /*幻钛钢熔炼机*/node(MLBlocks.phantomTitaniumSteelCompressor, () -> {
                    });
                        /*玄晶混制机*/node(MLBlocks.xuanCrystalManufacturingMachine, () -> {
                             });
                       });
                    });
            /*幻钢墙*/node(MLBlocks.phantomSteelWall, Seq.with(new OnSector(MLSectorPresets.LandingZone)), () -> {
                /*大幻钢墙*/node(MLBlocks.largePhantomSteelWall, () -> {
                    /*幻钛钢墙*/node(MLBlocks.phantomTitaniumSteelWall, () -> {
                        /*大幻钛钢墙*/node(MLBlocks.largePhantomTitaniumSteelWall, () -> {
                        });
                            /*联合墙*/node(MLBlocks.adaptiveWall, Seq.with(new SectorComplete(MLSectorPresets.DeepSecludedJungle)), () -> {
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

                    /*构造器*/node(MLBlocks.Constructor, () -> {

                                /*解构器*/node(MLBlocks.Deconstructor, () -> {

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
                    nodeProduce(MLItems.mysticSteel, () -> {
                        nodeProduce(MLItems.wingedMetal, () -> {
                        });
                           nodeProduce(MLItems.fluorescentFeatherStone, () -> {

                           });
                               nodeProduce(MLItems.phantomLuminousAlloy, () -> {

                                        });
                                });
                    /*荧羽反应堆*/node(MLBlocks.LumifeatherReactor, () -> {

                            });
                    /*玄钢传送带*/node(MLBlocks.XuansteelConveyor, () -> {
                        /*荧羽传送带*/node(MLBlocks.GlowingFeatherConveyor, () -> {
                                });
                        /*荧羽桥*/node(MLBlocks.GlowFeatherBridge, () -> {
                        });
                    });
                    /*荧羽钻头*/node(MLBlocks.FluorescentFeatherDrill, () -> {
                            });
                    /*弹雨*/node(MLBlocks.BulletsRain, () -> {
                        /*曙光*/node(MLBlocks.DawN, () -> {
                                });
                        /*罗灵*/node(MLBlocks.LuoLing, () -> {
                    /*星辰*/node(MLBlocks.Stars, () -> {
                                /*沧龙*/node(MLBlocks.Mosasaurus, () -> {

                                });
                            });
                        });
                        /*寻天*/node(MLBlocks.SeekingSky, () -> {
                        });
                    });
                    /*幻钢焚烧机*/node(MLBlocks.PhantomSteelIncinerator, () -> {
                        /*大型相织布编织器*/node(MLBlocks.LargePhaseWeaver, () -> {
                                });
                        /*大型巨浪合金冶炼厂*/node(MLBlocks.LargeSurgeSmelter, () -> {
                                });
                        /*玄钢混制机*/node(MLBlocks.XuansteelMixer, () -> {
                            /*翼精金属合成器*/node(MLBlocks.WingEssenceMetalSynthesizer, () -> {
                            });
                            /*荧羽石反应器*/node(MLBlocks.LuminFeatherStoneReactor, () -> {
                            });
                            /*幻荧合金组合器*/node(MLBlocks.PhantomGlowAlloyCombiner, () -> {
                            });
                        });
                    });
                    /*玄钢墙*/node(MLBlocks.MysticSteelWall, () -> {
                        /*幻荧墙*/node(MLBlocks.PhantomGlowWall, () -> {
                            /*大幻荧墙*/node(MLBlocks.LargePhantomGlowWall, () -> {

                            });
                        });
                        /*大玄钢墙*/node(MLBlocks.LargeMysticSteelWall, () -> {

                        });
                    });
                    /*太虚构装核心*/node(MLBlocks.PretendingCore, () -> {
                        /*通用装配升级厂*/node(MLBlocks.GeneralAssemblyUpgrade, () -> {

                        });
                    });
                });
            });
            /*降落区*/node(MLSectorPresets.LandingZone, Seq.with(new SectorComplete(SectorPresets.planetaryTerminal)), () -> {
                /*深幽丛林*/node(MLSectorPresets.DeepSecludedJungle, Seq.with(new SectorComplete(MLSectorPresets.LandingZone)), () -> {

                });
            });
                 });
        root.children.add(Planets.serpulo.techTree);
        MLPlanets.cecilia.techTree = root;
    }
}
