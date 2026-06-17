package magical.content;

import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.ui.Bar;
import arc.scene.ui.layout.Table;
import mindustry.world.blocks.defense.Wall;

public class AdaptiveWall extends Wall {

    public String gname = "default";

    public AdaptiveWall(String name){
        super(name);

        update = true;
        solid = true;
        destructible = true;
    }

    public class AdaptiveWallBuild extends WallBuild {

        public Seq<AdaptiveWallBuild> linked = new Seq<>();
        public float totalHealth;
        public float totalMax;
        public boolean needUpdate = false;

        @Override
        public void created(){
            super.created();

            linked.add(this);
            totalHealth = health;
            totalMax = block.health;
        }

        @Override
        public void onProximityAdded(){
            super.onProximityAdded();

            for(AdaptiveWallBuild build : linked){
                build.needUpdate = true;
            }
        }

        @Override
        public void updateTile(){
            super.updateTile();

            if(needUpdate){
                for(int i = 0; i < 4; i++){
                    Building other = nearby(i);

                    if(other instanceof AdaptiveWallBuild wall){
                        if(wall.block instanceof AdaptiveWall aw &&
                                aw.gname.equals(gname) &&
                                wall.linked != linked){

                            merge(wall);
                        }
                    }
                }

                needUpdate = false;
            }

            sync();
        }

        private void merge(AdaptiveWallBuild other){

            float mergedMax = totalMax + other.totalMax;
            float mergedHealth = totalHealth + other.totalHealth;

            for(AdaptiveWallBuild build : other.linked){
                if(!linked.contains(build)){
                    linked.add(build);
                }
            }

            totalHealth = mergedHealth;
            totalMax = mergedMax;

            sync();
        }

        private void sync(){
            float ratio = totalHealth / totalMax;

            for(AdaptiveWallBuild build : linked){
                build.health = build.block.health * ratio;

                build.totalHealth = totalHealth;
                build.totalMax = totalMax;
                build.linked = linked;
            }
        }

        @Override
        public void damage(float damage){
            totalHealth -= damage;

            if(totalHealth <= 0){
                for(AdaptiveWallBuild build : linked){
                    build.kill();
                }
                return;
            }

            sync();
        }

        @Override
        public void heal(){
            totalHealth = totalMax;
            sync();
        }

        @Override
        public void heal(float amount){
            totalHealth = Math.min(totalHealth + amount, totalMax);
            sync();
        }

        @Override
        public void remove(){
            super.remove();

            float ratio = totalHealth / totalMax;

            linked.remove(this);

            for(AdaptiveWallBuild build : linked){
                build.linked = new Seq<>();
                build.linked.add(build);

                build.totalMax = build.block.health;
                build.totalHealth = build.totalMax * ratio;

                build.health = build.totalHealth;
                build.needUpdate = true;
            }
        }

        @Override
        public void displayBars(Table table){

            table.add(new Bar(
                    () -> Core.bundle.format("bar.total-health", (int)totalHealth, (int)totalMax),
                    () -> Pal.health,
                    () -> totalHealth / totalMax
            )).growX();
            table.row();
            table.label(() -> Core.bundle.format("bar.bind-count", linked.size)
            ).growX();
        }
    }
}
