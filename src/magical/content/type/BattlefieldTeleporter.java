package magical.content;
import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.PayloadStack;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Tile;
import mindustry.world.blocks.units.UnitAssembler;
import mindustry.world.blocks.units.UnitAssembler.AssemblerUnitPlan;
import mindustry.world.meta.Stat;
public class BattlefieldTeleporter extends UnitAssembler{
    public int[] teleportRanges = {30, 60, 100, 160, 240, 350};
    public float teleportDelay = 30f;
    public float teleportCooldown = 60f;
    public Seq<String> planNames = new Seq<>();
    public Seq<Integer> planAreas = new Seq<>();
    public Seq<Integer> planTiers = new Seq<>();
    public BattlefieldTeleporter(String name){
        super(name);
        configurable = true;
        saveConfig = true;
        sync = true;
        config(Integer.class, (BattlefieldTeleporterBuild build, Integer value) -> {
            if(value == null) return;
            build.setSelectedPlan(value);
        });
        config(Point2.class, (BattlefieldTeleporterBuild build, Point2 value) -> {
            if(value == null) return;
            build.setTarget(value.x, value.y);
        });
    }
    public void addPlan(String name, UnitType output, float time, int area, int requiredTier, PayloadStack... requirements){
        AssemblerUnitPlan plan = new AssemblerUnitPlan(output, time, new Seq<>(requirements));
        plans.add(plan);
        planNames.add(name);
        planAreas.add(area);
        planTiers.add(requiredTier);
    }
    public String getPlanName(int index){
        if(index < 0 || index >= planNames.size) return "";
        String key = planNames.get(index);
        return Core.bundle.has(key) ? Core.bundle.get(key) : key;
    }
    public int getPlanArea(int index){
        if(index < 0 || index >= planAreas.size) return 0;
        return planAreas.get(index);
    }
    public int getPlanTier(int index){
        if(index < 0 || index >= planTiers.size) return 0;
        return planTiers.get(index);
    }
    public int getTeleportRange(int tier){
        if(teleportRanges == null || teleportRanges.length == 0) return 0;
        return teleportRanges[Mathf.clamp(tier, 0, teleportRanges.length - 1)];
    }
    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.range, Core.bundle.format("battlefield-teleporter.range", getTeleportRange(0)));
    }
    public class BattlefieldTeleporterBuild extends UnitAssemblerBuild{
        private static final int NO_PLAN = -1;
        private int selectedPlan = NO_PLAN;
        private int targetTileX = -1;
        private int targetTileY = -1;
        private float teleportProgress = 0f;
        private float cooldown = 0f;
        private boolean teleporting = false;
        private AssemblerUnitPlan selected(){
            if(selectedPlan < 0 || selectedPlan >= plans.size) return null;
            return plans.get(selectedPlan);
        }
        private int range(){
            return BattlefieldTeleporter.this.getTeleportRange(currentTier);
        }
        private float targetX(){
            return targetTileX * Vars.tilesize + Vars.tilesize / 2f;
        }
        private float targetY(){
            return targetTileY * Vars.tilesize + Vars.tilesize / 2f;
        }
        private boolean targetSet(){
            return targetTileX >= 0 && targetTileY >= 0;
        }
        private boolean targetInRange(){
            if(!targetSet()) return false;
            return Mathf.within(x, y, targetX(), targetY(), range() * Vars.tilesize);
        }
        private boolean targetValid(){
            if(!targetSet()) return false;
            Tile tile = Vars.world.tile(targetTileX, targetTileY);
            if(tile == null) return false;
            if(!targetInRange()) return false;
            if(tile.solid()) return false;
            if(tile.build != null && tile.build.team != team) return false;
            return true;
        }
        private void clearTarget(){
            targetTileX = -1;
            targetTileY = -1;
        }
        public void setSelectedPlan(int index){
            if(index < 0 || index >= plans.size){
                return;
            }
            if(getPlanTier(index) > currentTier){
                return;
            }
            selectedPlan = index;
            progress = 0f;
            blocks.clear();
        }
        @Override
        public void created(){
            super.created();
            if(selectedPlan == NO_PLAN && plans.size > 0){
                for(int i = 0; i < plans.size; i++){
                    if(getPlanTier(i) <= currentTier){
                        selectedPlan = i;
                        break;
                    }
                }
            }
        }
        @Override
        public Object config(){
            return selectedPlan;
        }
        @Override
        public void configure(@Nullable Object value){
            if(value instanceof Integer){
                setSelectedPlan((Integer)value);
                return;
            }
            if(value instanceof Point2){
                Point2 point = (Point2)value;
                setTarget(point.x, point.y);
            }
        }
        private void setTarget(int tx, int ty){
            Tile tile = Vars.world.tile(tx, ty);
            if(tile == null){
                return;
            }
            float txWorld = tx * Vars.tilesize + Vars.tilesize / 2f;
            float tyWorld = ty * Vars.tilesize + Vars.tilesize / 2f;
            if(!Mathf.within(x, y, txWorld, tyWorld, range() * Vars.tilesize)){
                return;
            }
            targetTileX = tx;
            targetTileY = ty;
        }
        @Override
        public void buildConfiguration(Table table){
            if(Vars.headless) return;
            AssemblerUnitPlan current = selected();
            table.label(() ->
                    current == null
                            ? Core.bundle.get("battlefield-teleporter.no-unit")
                            : Core.bundle.format("battlefield-teleporter.unit", current.unit.localizedName)
            ).growX().left().row();
            table.label(() ->
                    Core.bundle.format("battlefield-teleporter.range", range())
            ).growX().left().row();
            table.label(() ->
                    targetSet()
                            ? Core.bundle.format("battlefield-teleporter.target", targetTileX, targetTileY)
                            : Core.bundle.get("battlefield-teleporter.no-target")
            ).growX().left().row();
            table.row();
            table.button(Icon.units, Styles.cleari, this::showUnitSelector).size(50f);
            table.button(Icon.refresh, Styles.cleari, this::clearTarget).size(50f);
        }
        private void showUnitSelector(){
            if(Vars.headless) return;
            BaseDialog dialog = new BaseDialog(
                    Core.bundle.get("battlefield-teleporter.select-unit")
            );
            Table table = new Table();
            table.defaults().size(90f);
            for(int i = 0; i < plans.size; i++){
                final int index = i;
                AssemblerUnitPlan plan = plans.get(i);
                boolean unlocked = getPlanTier(index) <= currentTier;
                table.button(b -> {
                    b.image(plan.unit.uiIcon).size(40f);
                    b.row();
                    b.add(getPlanName(index)).growX();
                }, unlocked ? Styles.cleart : Styles.cleari, () -> {
                    if(unlocked){
                        configure(index);
                        dialog.hide();
                    }
                });
                if(i % 4 == 3){
                    table.row();
                }
            }
            dialog.cont.add(table).grow();
            dialog.addCloseButton();
            dialog.show();
        }
        @Override
        public void updateTile(){
            if(cooldown > 0f){
                cooldown = Math.max(0f, cooldown - edelta());
            }
            if(teleporting){
                teleportProgress += edelta();
                if(teleportProgress >= teleportDelay){
                    finishTeleport();
                }
                return;
            }
            super.updateTile();
        }
        @Override
        public void spawned(){
            AssemblerUnitPlan plan = selected();
            if(plan == null) return;
            if(getPlanTier(selectedPlan) > currentTier) return;
            if(!targetValid()) return;
            if(cooldown > 0f) return;
            if(Vars.net.client()) return;
            teleporting = true;
            teleportProgress = 0f;
            Fx.unitAssemble.at(targetX(), targetY(), 0f, plan.unit);
        }
        private void finishTeleport(){
            if(Vars.net.client()){
                teleporting = false;
                teleportProgress = 0f;
                return;
            }
            AssemblerUnitPlan plan = selected();
            if(plan == null || !targetValid()){
                teleporting = false;
                teleportProgress = 0f;
                return;
            }
            Unit unit = plan.unit.create(team);
            unit.set(targetX(), targetY());
            unit.rotation = rotdeg();
            unit.add();
            createSound.at(
                    targetX(),
                    targetY(),
                    1f,
                    createSoundVolume
            );
            Fx.spawn.at(targetX(), targetY());
            cooldown = teleportCooldown;
            teleporting = false;
            teleportProgress = 0f;
            progress = 0f;
            blocks.clear();
        }
        @Override
        public void draw(){
            super.draw();
            if(targetSet()){
                Draw.z(Layer.overlayUI);
                Draw.color(targetValid() ? Pal.accent : Pal.remove);
                Lines.stroke(2f);
                Lines.circle(
                        targetX(),
                        targetY(),
                        6f + Mathf.absin(Time.time, 4f, 2f)
                );
                Lines.stroke(1f);
                Lines.circle(
                        targetX(),
                        targetY(),
                        10f + Mathf.absin(Time.time, 3f, 2f)
                );
                Draw.reset();
            }
        }
        @Override
        public void drawSelect(){
            super.drawSelect();
            Draw.z(Layer.overlayUI);
            Draw.color(Pal.accent, 0.7f);
            Lines.stroke(2f);
            Lines.circle(
                    x,
                    y,
                    range() * Vars.tilesize
            );
            if(targetSet()){
                Draw.color(targetValid() ? Pal.accent : Pal.remove);
                Lines.stroke(3f);
                Lines.line(
                        x,
                        y,
                        targetX(),
                        targetY()
                );
            }
            Draw.reset();
        }
        @Override
        public void write(Writes write){
            super.write(write);
            write.i(selectedPlan);
            write.i(targetTileX);
            write.i(targetTileY);
            write.f(cooldown);
        }
        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            selectedPlan = read.i();
            targetTileX = read.i();
            targetTileY = read.i();
            cooldown = read.f();
            teleporting = false;
            teleportProgress = 0f;
        }
    }
}
