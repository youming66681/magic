package magical.content;

import arc.struct.Seq;
import mindustry.type.PayloadStack;
import mindustry.type.UnitType;
import mindustry.world.blocks.units.UnitAssembler;
import mindustry.world.blocks.units.UnitAssembler.AssemblerUnitPlan;
import mindustry.world.blocks.payloads.*;

public class SelectableAssembler extends UnitAssembler {

    public @Load("@-side1") TextureRegion sideRegion1;
    public @Load("@-side2") TextureRegion sideRegion2;

    public int areaSize = 11;
    public UnitType droneType = UnitTypes.assemblyDrone;
    public int dronesCreated = 4;
    public float droneConstructTime = 60f * 4f;
    public int[] capacities = {};

    public Seq<AssemblerUnitPlan> plans = new Seq<>(4);

    public Sound createSound = Sounds.unitCreateBig;
    public float createSoundVolume = 1f;

    protected @Nullable ConsumePayloadDynamic consPayload;
    protected @Nullable ConsumeItemDynamic consItem;

    public UnitAssembler(String name) {
        SelectableAssembler(name);
    }
    @Override
    public Building newBuilding() {
        return new MultiUnitAssemblerBuild();
    }

    public class MultiUnitAssemblerBuild extends UnitAssemblerBuild {
        public int[] selected = new int[levelOptions.length];

        @Override
        public void updateTile() {
            super.updateTile();
        }

        // 获取当前等级（由父类维护的 level 变量）对应的计划
        public AssemblerUnitPlan currentPlan() {
            int level = this.level; // 父类中的当前等级（0 开始）
            if (level < 0 || level >= levelOptions.length) return null;
            Seq<AssemblerUnitPlan> options = levelOptions[level];
            int idx = selected[level];
            if (idx < 0 || idx >= options.size) return null;
            return options.get(idx);
        }

        // 覆盖父类中获取计划的方法
        @Override
        public AssemblerUnitPlan getPlan() {
            return currentPlan();
        }

        // 覆盖消耗、进度等需要计划的方法
        @Override
        public void consume() {
            AssemblerUnitPlan plan = currentPlan();
            if (plan == null) return;
            // 消耗 plan.requirements
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            for (int i = 0; i < selected.length; i++) {
                write.i(selected[i]);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            for (int i = 0; i < selected.length; i++) {
                selected[i] = read.i();
            }
        }

        // ===== 配置 UI =====
        @Override
        public void buildConfiguration(Table table) {
            // 为每个等级绘制一组选择按钮
            for (int levelIdx = 0; levelIdx < levelOptions.length; levelIdx++) {
                Seq<AssemblerUnitPlan> options = levelOptions[levelIdx];
                if (options == null || options.isEmpty()) continue;

                // 显示等级标签
                table.add(Core.bundle.get("level") + " " + (levelIdx + 1) + ":").padRight(8f);
                Table group = new Table();
                group.defaults().size(40f).pad(2f);

                for (int optIdx = 0; optIdx < options.size; optIdx++) {
                    AssemblerUnitPlan plan = options.get(optIdx);
                    int finalLevel = levelIdx;
                    int finalOpt = optIdx;
                    // 创建单位图标按钮
                    group.button(
                            t -> {
                                t.image(plan.unit.icon(Cicon.medium)).size(32f);
                            },
                            Styles.clearToggleTransi,
                            () -> {
                                // 点击时切换选择
                                selected[finalLevel] = finalOpt;
                                // 同步到客户端
                                configure(any -> {
                                    // 发送选择数据，需自定义逻辑
                                });
                            }
                    ).update(b -> {
                        // 高亮当前选中的按钮
                        b.setChecked(selected[finalLevel] == finalOpt);
                    }).pad(2f);
                }
                table.add(group).row();
            }
        }
    }
}