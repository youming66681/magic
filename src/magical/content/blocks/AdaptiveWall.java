package magical.content;

import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.ui.Bar;
import mindustry.ui.Table;
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
            for(AdaptiveWallBuild build : other.linked){
                if(!linked.contains(build)){
                    linked.add(build);
                    totalHealth += build.health;
                    totalMax += build.block.health;
                }
            }

            for(AdaptiveWallBuild build : linked){
                build.linked = linked;
                build.totalHealth = totalHealth;
                build.totalMax = totalMax;
            }
        }

        private void sync(){
            float each = totalHealth / linked.size;

            for(AdaptiveWallBuild build : linked){
                build.health = each;
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

            linked.remove(this);

            for(AdaptiveWallBuild build : linked){
                build.linked = new Seq<>();
                build.linked.add(build);
                build.totalHealth = build.health;
                build.totalMax = build.block.health;
                build.needUpdate = true;
            }
        }

        @Override
        public void displayBars(Table table){
            super.displayBars(table);

            // 原生命值位置显示总生命值
            table.add(new Bar(
                    () -> "health: " + (int)totalHealth,
                    () -> team.color,
                    () -> totalHealth / totalMax
            )).growX();

            table.row();

            // 单独显示连接数量
            table.add(new Bar(
                    () -> "bind: " + linked.size,
                    () -> team.color,
                    () -> 1f
            )).growX();
        }
    }
}
