package magical.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.LiquidTurret;
import mindustry.Vars;
import mindustry.world.Tile;
import mindustry.entities.Fires;
import arc.util.Nullable;
import arc.math.geom.Geometry;
import mindustry.type.Liquid;
import mindustry.content.Liquids;

public class fluvialErosion extends LiquidBulletType{

    public float lightStroke = 40f;
    public float width = 3.7f, oscScl = 1.2f, oscMag = 0.02f;
    public int divisions = 25;

    public boolean drawFlare = true;
    public Color flareColor = Color.valueOf("e189f5");
    public float flareWidth = 3f, flareInnerScl = 0.5f, flareLength = 40f, flareInnerLenScl = 0.5f, flareLayer = Layer.bullet - 0.0001f, flareRotSpeed = 1.2f;
    public boolean rotateFlare = false;
    public Interp lengthInterp = Interp.slope;
    /** Lengths, widths, ellipse panning, and offsets, all as fractions of the base width and length. Stored as an 'interleaved' array of values: LWPO1 LWPO2 LWPO3... */
    public float[] lengthWidthPans = {
            1.12f, 1.3f, 0.32f,
            1f, 1f, 0.3f,
            0.8f, 0.9f, 0.2f,
            0.5f, 0.8f, 0.15f,
            0.25f, 0.7f, 0.1f,
    };

    public Color[] colors = {Color.valueOf("eb7abe").a(0.55f), Color.valueOf("e189f5").a(0.7f), Color.valueOf("907ef7").a(0.8f), Color.valueOf("91a4ff"), Color.white.cpy()};
    public fluvialErosion(float damage){
        super(Liquids.water);
        this.damage = damage;
    }

    public fluvialErosion(){
        this(null);
    }
    public float length = 144f;
        public fluvialErosion(@Nullable Liquid liquid){
        super();

        if(liquid != null) {
            this.liquid = liquid;
            this.status = liquid.effect;
        }

        optimalLifeFract = 0.5f;
        hitEffect = Fx.hitFlameBeam;
        length = 144f;
        hitSize = 4;
        drawSize = 420f;
        lifetime = 16f;
        hitColor = colors[1].cpy().a(1f);
        lightColor = hitColor;
        lightOpacity = 0.7f;
        laserAbsorb = false;
        ammoMultiplier = 1f;
        pierceArmor = true;
    }

    @Override
    public void draw(Bullet b){
        super.draw(b);

        float mult = b.fin(lengthInterp);
        float realLength = Damage.findLength(b, length * mult, laserAbsorb, pierceCap);

        float sin = Mathf.sin(Time.time, oscScl, oscMag);

        for(int i = 0; i < colors.length; i++){
            Draw.color(colors[i].write(Tmp.c1).mul(0.9f).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)));
            Drawf.flame(b.x, b.y, divisions, b.rotation(),
                    realLength * lengthWidthPans[i * 3] * (1f - sin),
                    width * lengthWidthPans[i * 3 + 1] * mult * (1f + sin),
                    lengthWidthPans[i * 3 + 2]
            );
        }

        if(drawFlare){
            Draw.color(flareColor);
            Draw.z(flareLayer);

            float angle = Time.time * flareRotSpeed + (rotateFlare ? b.rotation() : 0f);

            for(int i = 0; i < 4; i++){
                Drawf.tri(b.x, b.y, flareWidth, flareLength * (mult + sin), i*90 + 45 + angle);
            }

            Draw.color();
            for(int i = 0; i < 4; i++){
                Drawf.tri(b.x, b.y, flareWidth * flareInnerScl, flareLength * flareInnerLenScl * (mult + sin), i*90 + 45 + angle);
            }
        }

        Tmp.v1.trns(b.rotation(), realLength * 1.1f);
        Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, lightStroke, lightColor, lightOpacity);
        Draw.reset();
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        for(float i = 0; i < 144f; i += 8f){
            float x = b.x + Angles.trnsx(b.rotation(), i);
            float y = b.y + Angles.trnsy(b.rotation(), i);

            Tile tile = Vars.world.tileWorld(x, y);
            if(tile != null){
                Fires.extinguish(tile, 100f);
            }
        }
    }

        public void hit(Bullet b, float hitx, float hity, boolean createFrags){
        hitEffect.at(hitx, hity, liquid.color);
        Puddles.deposit(Vars.world.tileWorld(hitx, hity), liquid, puddleSize);

        if(liquid.temperature <= 0.5f && liquid.flammability < 0.3f){
            float intensity = 400f * puddleSize/6f;
            Fires.extinguish(Vars.world.tileWorld(hitx, hity), intensity);
            for(Point2 p : Geometry.d4){
                Fires.extinguish(Vars.world.tileWorld(hitx + p.x * Vars.tilesize, hity + p.y * Vars.tilesize), intensity);
            }
        }
    }
}