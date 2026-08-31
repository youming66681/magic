package magical.content;
import arc.Core;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.units.*;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.meta.*;
import mindustry.entities.Units;
import java.util.*;
import static mindustry.Vars.*;
public class FlexAssembler extends UnitAssembler{
    public final Map<AssemblerUnitPlan,Integer> planAreaMap=new HashMap<>();
    public final Map<AssemblerUnitPlan,Integer> tierRequired=new HashMap<>();
    public FlexAssembler(String name){
        super(name);
        configurable=true;
    }
    public void addPlan(String label,UnitType output,float time,int customArea,int requiredTier,PayloadStack... requirements){
        AssemblerUnitPlan plan=new AssemblerUnitPlan(output,time,new Seq<>(requirements));
        plans.add(plan);
        planAreaMap.put(plan,customArea);
        tierRequired.put(plan,requiredTier);
    }
    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.output);
        stats.add(Stat.output,table->{
            table.row();
            Map<Integer,Seq<AssemblerUnitPlan>> byTier=new HashMap<>();
            for(AssemblerUnitPlan plan:plans){
                int tier=tierRequired.getOrDefault(plan,0);
                byTier.computeIfAbsent(tier,k->new Seq<>()).add(plan);
            }
            int maxTier=byTier.keySet().stream().max(Integer::compareTo).orElse(0);
            for(int tier=0;tier<=maxTier;tier++){
                Seq<AssemblerUnitPlan> group=byTier.get(tier);
                if(group==null||group.isEmpty())continue;
                final int displayTier=tier;
                table.table(Tex.pane,t->{
                    t.add(Core.bundle.format("flexassembler.tier.stat",displayTier))
                            .pad(5).left().growX();
                }).growX().pad(5).row();
                for(AssemblerUnitPlan plan:group){
                    table.table(Tex.pane,t->{
                        if(plan.unit.isBanned()){
                            t.image(Icon.cancel).color(Pal.remove).size(40).pad(10);
                            return;
                        }
                        if(plan.unit.unlockedNow()){
                            t.image(plan.unit.uiIcon)
                                    .scaling(Scaling.fit)
                                    .size(40)
                                    .pad(10)
                                    .left();
                            t.table(info->{
                                info.left();
                                info.add(plan.unit.localizedName).left();
                                info.row();
                                info.add(
                                        Strings.autoFixed(plan.time/60f,1)+" "+
                                                Core.bundle.get("unit.seconds")
                                ).color(Color.lightGray).left();
                                int required=tierRequired.getOrDefault(plan,0);
                                if(required>0){
                                    info.row();
                                    info.add(
                                            Core.bundle.format(
                                                    "flexassembler.tier.stat",
                                                    required
                                            )
                                    ).color(Color.lightGray).left();
                                }
                                info.row();
                                info.add(
                                        Core.bundle.format(
                                                "flexassembler.area.stat",
                                                planAreaMap.getOrDefault(plan,areaSize)
                                        )
                                ).color(Color.lightGray).left();
                            }).left();
                            t.table(req->{
                                for(int i=0;i<plan.requirements.size;i++){
                                    if(i%4==0)req.row();
                                    req.add(
                                            StatValues.stack(plan.requirements.get(i))
                                    ).pad(5);
                                }
                            }).right();
                        }else{
                            t.image(Icon.lock)
                                    .color(Pal.darkerGray)
                                    .size(40)
                                    .pad(10);
                        }
                    }).growX().pad(5).row();
                }
            }
        });
    }
    public class FlexAssemblerBuild extends UnitAssemblerBuild{
        private static final int NO_PLAN=-1;
        private int selectedPlan=NO_PLAN;
        private AssemblerUnitPlan selected(){
            if(selectedPlan<0||selectedPlan>=plans.size)return null;
            return plans.get(selectedPlan);
        }
        private boolean selectedValid(){
            AssemblerUnitPlan plan=selected();
            return plan!=null&&
                    tierRequired.getOrDefault(plan,0)<=currentTier;
        }
        private int effectiveArea(){
            AssemblerUnitPlan plan=selected();
            if(plan==null)return FlexAssembler.this.areaSize;
            return planAreaMap.getOrDefault(
                    plan,
                    FlexAssembler.this.areaSize
            );
        }
        private void updateArea(){
            if(selected()!=null){
                selectedArea=effectiveArea();
            }else{
                selectedArea=FlexAssembler.this.areaSize;
            }
        }
        private int selectedArea;
        @Override
        public void created(){
            super.created();
            if(selectedPlan<NO_PLAN||selectedPlan>=plans.size){
                selectedPlan=NO_PLAN;
            }
            updateArea();
        }
        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            updateArea();
        }
        @Override
        public AssemblerUnitPlan plan(){
            if(selectedPlan!=NO_PLAN){
                return selectedValid()?selected():null;
            }
            return super.plan();
        }
        @Override
        public Vec2 getUnitSpawn(){
            int area=effectiveArea();
            float len=tilesize*(area+block.size)/2f;
            return Tmp.v4.set(
                    x+Geometry.d4x(rotation)*len,
                    y+Geometry.d4y(rotation)*len
            );
        }
        @Override
        public boolean moduleFits(Block other,float ox,float oy,int rotation){
            int area=effectiveArea();
            float dx=ox+
                    Geometry.d4x(rotation)*
                            (other.size/2f+0.5f)*
                            tilesize;
            float dy=oy+
                    Geometry.d4y(rotation)*
                            (other.size/2f+0.5f)*
                            tilesize;
            Vec2 spawn=getUnitSpawn();
            if(
                    Tile.relativeTo(
                            ox,
                            oy,
                            spawn.x,
                            spawn.y
                    )!=rotation
            )return false;
            float dst=Math.max(
                    Math.abs(dx-spawn.x),
                    Math.abs(dy-spawn.y)
            );
            return Mathf.equal(
                    dst,
                    tilesize*area/2f-tilesize/2f
            );
        }
        @Override
        public void buildConfiguration(Table table){
            if(Vars.headless)return;
            AssemblerUnitPlan current=selected();
            boolean hasSelected=selectedValid();
            if(current!=null&&!hasSelected){
                current=null;
            }
            if(hasSelected){
                table.label(
                        ()->Core.bundle.format(
                                "flexassembler.producing",
                                current.unit.localizedName
                        )
                ).padBottom(4).row();
                table.label(
                        ()->Core.bundle.format(
                                "flexassembler.area.stat",
                                effectiveArea()
                        )
                ).color(Color.lightGray).padBottom(4).row();
            }else{
                table.label(
                        ()->Core.bundle.get(
                                "flexassembler.select-unit"
                        )
                ).padBottom(4).row();
            }
            Seq<AssemblerUnitPlan> available=new Seq<>();
            for(AssemblerUnitPlan plan:plans){
                if(
                        tierRequired.getOrDefault(plan,0)<=currentTier
                ){
                    available.add(plan);
                }
            }
            if(available.isEmpty()){
                table.label(
                        ()->Core.bundle.get(
                                "flexassembler.no-plans"
                        )
                ).pad(10);
                return;
            }
            Table grid=new Table();
            int cols=4;
            for(int i=0;i<available.size;i++){
                if(i%cols==0&&i!=0)grid.row();
                AssemblerUnitPlan plan=available.get(i);
                int index=plans.indexOf(plan);
                boolean chosen=selectedPlan==index;
                Button button=new Button(Tex.button);
                button.table(inner->{
                    inner.image(plan.unit.uiIcon)
                            .size(30f)
                            .padBottom(4f);
                    inner.row();
                    inner.add(plan.unit.localizedName)
                            .color(
                                    chosen?
                                            Pal.accent:
                                            Color.lightGray
                            );
                }).pad(8);
                button.clicked(()->{
                    configure(index);
                });
                grid.add(button)
                        .size(80f,80f)
                        .pad(4f);
            }
            table.add(
                    new ScrollPane(grid)
            ).grow().maxHeight(400f).row();
            if(hasSelected){
                table.button(
                        Core.bundle.get(
                                "flexassembler.deselect"
                        ),
                        ()->configure(NO_PLAN)
                ).padTop(10f).row();
            }
        }
        @Override
        public Object config(){
            return selectedPlan;
        }
        @Override
        public void configure(@Nullable Object value){
            if(!(value instanceof Integer))return;
            int index=(Integer)value;
            if(index==NO_PLAN){
                selectedPlan=NO_PLAN;
                selectedArea=FlexAssembler.this.areaSize;
                updateArea();
                return;
            }
            if(index<0||index>=plans.size)return;
            AssemblerUnitPlan plan=plans.get(index);
            if(plan==null)return;
            if(
                    tierRequired.getOrDefault(plan,0)>
                            currentTier
            )return;
            selectedPlan=index;
            selectedArea=planAreaMap.getOrDefault(
                    plan,
                    FlexAssembler.this.areaSize
            );
            updateArea();
        }
        @Override
        public boolean shouldConsume(){
            if(selectedPlan!=NO_PLAN){
                AssemblerUnitPlan plan=selected();
                if(plan==null)return false;
                if(
                        tierRequired.getOrDefault(plan,0)>
                                currentTier
                )return false;
                return enabled&&
                        !wasOccupied&&
                        Units.canCreate(team,plan.unit)&&
                        efficiency>0&&
                        team.activateUnitFactories();
            }
            return super.shouldConsume();
        }
        @Override
        public void updateTile(){
            updateArea();
            int previous=areaSize;
            areaSize=effectiveArea();
            super.updateTile();
            areaSize=previous;
        }
        @Override
        public void draw(){
            int previous=areaSize;
            areaSize=effectiveArea();
            super.draw();
            areaSize=previous;
        }
        @Override
        public void drawSelect(){
            int previous=areaSize;
            areaSize=effectiveArea();
            super.drawSelect();
            areaSize=previous;
        }
        @Override
        public void spawned(){
            AssemblerUnitPlan plan;
            if(selectedPlan!=NO_PLAN){
                plan=selected();
                if(plan==null||!selectedValid())return;
            }else{
                plan=super.plan();
                if(plan==null)return;
            }
            Vec2 spawn=getUnitSpawn();
            consume();
            Unit unit=plan.unit.create(team);
            if(
                    unit.isCommandable()&&
                            commandPos!=null
            ){
                unit.command()
                        .commandPosition(commandPos);
            }
            unit.set(
                    spawn.x+Mathf.range(0.001f),
                    spawn.y+Mathf.range(0.001f)
            );
            unit.rotation=rotdeg();
            var targetBuild=unit.buildOn();
            var payload=new UnitPayload(unit);
            if(
                    targetBuild!=null&&
                            targetBuild.team==team&&
                            targetBuild.acceptPayload(
                                    targetBuild,
                                    payload
                            )
            ){
                targetBuild.handlePayload(
                        targetBuild,
                        payload
                );
            }else if(!net.client()){
                unit.add();
                Units.notifyUnitSpawn(unit);
            }
            createSound.at(
                    spawn.x,
                    spawn.y,
                    1f+Mathf.range(0.06f),
                    createSoundVolume
            );
            progress=0f;
            Fx.unitAssemble.at(
                    spawn.x,
                    spawn.y,
                    rotdeg()-90f,
                    plan.unit
            );
            blocks.clear();
        }
        @Override
        public void write(Writes write){
            super.write(write);
            write.i(selectedPlan);
        }
        @Override
        public void read(Reads read,byte revision){
            super.read(read,revision);
            selectedPlan=read.i();
            if(
                    selectedPlan<NO_PLAN||
                            selectedPlan>=plans.size
            ){
                selectedPlan=NO_PLAN;
            }
            updateArea();
        }
    }
}