package plus.client.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.SpMain;
import plus.client.camera.animation.Animation;
import plus.client.camera.animation.Point1f;
import plus.common.mashine.inv.IEnergyFolder;
import plus.common.player.PlayerSpship;
import plus.common.spship.SpShip;
import plus.tson.utl.Tuple;
import static plus.client.SpGui.mc;


@SideOnly(Side.CLIENT)
public class SpOverlay extends Gui {
    public static final ResourceLocation SP_BG =
            new ResourceLocation(SpMain.MOD_ID,"textures/gui/sp_overlay.png");

    private final Animation<Point1f<Object>, Object> anim  = new Animation<>(270f, new Point1f<>(1f));
    private final Animation<Point1f<Object>, Object> anim2 = new Animation<>(80f,new Point1f<>(1f));

    private final Animation<Point1f<Object>, Object> animE = new Animation<>(270f, new Point1f<>(1f));
    private final Animation<Point1f<Object>, Object> animE2 = new Animation<>(80, new Point1f<>(1f));

    float cur = 1;
    float prev = 1;
    float curE = 1;
    float prevE = 1;

    private boolean updated;
    private boolean flag = true;
    private boolean flagE = true;

    //todo убрать лишние вызовы system.curTime()
    public void update(){
        PlayerSpship sp0 = (PlayerSpship) mc.player;
        SpShip sp = sp0.getSp();

        cur = sp0.getSpShield() /sp.maxShield;
        prev = cur;
        IEnergyFolder pwSys = sp.getPowerSystem();
        curE = (float) pwSys.getCurEnergy() / pwSys.getMaxEnergy();
        prevE = curE;

        updated = true;
    }

    private ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());

    void render(){
        if(!updated)update();
        scaled = new ScaledResolution(Minecraft.getMinecraft());

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
        res = animE.getCurPoint();
        if(res.A){
            flagE = true;
        } else {
            if(flagE) {
                flagE = false;
                animE2.stop();
                animE2.preStart(new Point1f<>(prevE));
                animE2.setTarget(new Point1f<>(curE));
            }
            prevE = animE2.getCurPoint().B.value;
        }
        drawEnergy(res.B.value, prevE);

        GlStateManager.popMatrix();
    }


    public void updateEnrg(SpShip sp){
        IEnergyFolder pwSys = sp.getPowerSystem();
        prevE = animE.getCurPoint().B.value;
        curE = (float) pwSys.getCurEnergy() / pwSys.getMaxEnergy();
        if(curE<0)curE = 0;
        animE.stop();
        animE.preStart(new Point1f<>(prevE));
        animE.setTarget(new Point1f<>(curE));
    }


    public void onPostDamage(float newShield0){
        PlayerSpship sp0 = (PlayerSpship) mc.player;
        SpShip sp = sp0.getSp();
        float newShield = newShield0 /sp.maxShield;
        damage(cur-newShield);
        updateEnrg(sp);
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
        drawPRSHP(scaled.getScaledWidth()/4-128, 50, hp,  prev, 7, 21, 14);
    }


    void drawNormal(float hp, float prev){
        drawPRSHP((scaled.getScaledWidth()/2)-128,
                (scaled.getScaledHeight() - (scaled.getScaledHeight()/5))+26,
                hp, prev, 0, 28, 7
        );
    }


    void drawEnergy(float eng, float prev){
        drawPRSHPE((scaled.getScaledWidth()/2)-111,
                (scaled.getScaledHeight() - (scaled.getScaledHeight()/5))+35,
                eng, prev, 45, 40, 35
        );
    }

    void drawPRSHPE(int x, int y, float hp, float prev, int l1, int l2, int l3){
        drawPRS(x, y, hp, prev, l1, l2, l3, 222, 5);
    }


    void drawPRSHP(int x, int y, float hp, float prev, int l1, int l2, int l3){
        drawPRS(x, y, hp, prev, l1, l2, l3, 256, 7);
    }


    void drawRPSEN(int x, int y, float hp, float prev){
        drawPRS(x, y, hp, prev, 45,40, 35, 222, 5);
    }


    void drawPRS(int x, int y, float hp, float prev, int l1, int l2, int l3, int max, int height){
        double bound  = (max*(1-hp));
        double prevBd = (max*(1-prev));
        if(bound>prevBd) {
            this.drawTexturedModalRect0(x + max - bound, y, max - bound, l2,
                    bound - prevBd, height
            );
        }
        this.drawTexturedModalRect0(x+max-prevBd, y, max-prevBd, l1, prevBd, height);
        this.drawTexturedModalRect0(x, y, 0, l3, max - bound, height);
    }

    private void drawTexturedModalRect1(double x, double y, double textureX, double textureY, double width, double height) {
        super.drawTexturedModalRect((int) x, (int) y, (int) textureX, (int) textureY, (int) width, (int) height);
    }


    private void drawTexturedModalRect0(double x, double y, double textureX, double textureY, double width, double height) {
        //float f1 = 0.00390625F; //1/256
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, this.zLevel).tex(textureX * 0.00390625, (textureY + height) * 0.00390625).endVertex();
        bufferbuilder.pos(x + width, y + height, this.zLevel).tex((textureX + width) * 0.00390625, (textureY + height) * 0.00390625).endVertex();
        bufferbuilder.pos(x + width, y, this.zLevel).tex((textureX + width) * 0.00390625, textureY * 0.00390625).endVertex();
        bufferbuilder.pos(x, y, this.zLevel).tex(textureX * 0.00390625, textureY * 0.00390625).endVertex();
        tessellator.draw();
    }
}
