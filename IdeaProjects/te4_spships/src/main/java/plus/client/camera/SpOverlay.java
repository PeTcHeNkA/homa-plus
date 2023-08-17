package plus.client.camera;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import plus.SpMain;
import plus.client.camera.animation.Animation;
import plus.client.camera.animation.Point1f;
import plus.common.player.PlayerSpship;
import plus.common.spship.SpShip;
import plus.tson.utl.Tuple;
import static plus.client.SpGui.mc;


public class SpOverlay extends Gui {
    public static final ResourceLocation SP_BG =
            new ResourceLocation(SpMain.MOD_ID,"textures/gui/sp_overlay.png");

    private final Animation<Point1f<Object>, Object> anim  = new Animation<>(270f, new Point1f<>(1f));
    private final Animation<Point1f<Object>, Object> anim2 = new Animation<>(80f,new Point1f<>(1f));
    private final Animation<Point1f<Object>, Object> engAnim1 = new Animation<>(270f, new Point1f<>(1f));


    float cur = 1;
    float prev = 1;

    private boolean updated;
    private boolean flag = true;



    public void update(){
        PlayerSpship sp0 = (PlayerSpship) mc.player;
        SpShip sp = sp0.getSp();
        cur = sp0.getSpShield() /sp.maxShield;
        prev = cur;
        updated = true;
    }


    void render(){
        if(!updated)update();

        Tuple<Boolean, Point1f<Object>> res = anim.getCurPoint();
        if(res.A){
            flag = true;
        } else {
            if(flag){
                flag = false;
                anim2.stop();
                anim2.preStart(new Point1f<>(prev));
                anim2.setTarget(new Point1f<>(cur));
            }
            prev = anim2.getCurPoint().B.value;
        }

        GlStateManager.pushMatrix();
        mc.getTextureManager().bindTexture(SP_BG);
        drawNormal(res.B.value, prev);
        GlStateManager.popMatrix();
    }


    public void onPostDamage(float newShield0){
        PlayerSpship sp0 = (PlayerSpship) mc.player;
        SpShip sp = sp0.getSp();
        float newShield = newShield0 /sp.maxShield;
        damage(cur-newShield);
    }


    void damage(float amount){
        prev = anim.getCurPoint().B.value;
        cur = cur-amount;
        if(cur<0)cur = 0;
        anim.stop();
        anim.preStart(new Point1f<>(prev));
        anim.setTarget(new Point1f<>(cur));
    }


    void drawShielded(float hp, float prev){
        drawPRSHP(mc.displayWidth/4-128, 50, hp,  prev, 7, 21, 14);
    }


    void drawNormal(float hp, float prev){
        drawPRSHP(mc.displayWidth/4-128, mc.displayHeight/2-45, hp, prev, 0, 28, 7);
    }


    void drawPRSHP(int x, int y, float hp, float prev, int l1, int l2, int l3){
        drawPRS(x, y, hp, prev, l1, l2, l3, 256, 7);
    }


    void drawRPSEN(int x, int y, float hp, float prev){
        drawPRS(x, y, hp, prev, 45,40, 35, 222, 5);
    }


    void drawPRS(int x, int y, float hp, float prev, int l1, int l2, int l3, int max, int height){
        int bound = (int) (max*(1-hp));
        int prevBd = (int) (max*(1-prev));
        if(bound>prevBd) {
            this.drawTexturedModalRect(x + max - bound, y, max - bound, l2,
                    bound - prevBd, height
            );
        }
        this.drawTexturedModalRect(x+max-prevBd, y, max-prevBd, l1, prevBd, height);
        this.drawTexturedModalRect(x, y, 0, l3, max - bound, height);
    }
}
