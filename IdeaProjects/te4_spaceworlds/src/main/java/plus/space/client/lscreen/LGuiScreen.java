package plus.space.client.lscreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class LGuiScreen{
    public static int DEF_WIDTH = 1024;
    public static int DEF_HEIGHT = 576;
    private static LGuiScreen last;
    final float maxX, maxY, sy;
    private final Sticky stickyW;
    private final Sticky stickyH;
    ResourceLocation img;
    private String title;
    float shiftX1, shiftY1, shiftX2, shiftY2;
    int width, height;

    public static LGuiScreen getLast(){
        if(last==null)return newScreen();
        return last;
    }


    public static LGuiScreen newScreen(){
        if(1==1)return new LGuiScreen("sel_server", 1280, 729);
        return last = new LGuiScreen();
    }


    private LGuiScreen(){
        img = new LResLocation(1);
        title = LResLocation.getRandomTitle();
        maxX = DEF_WIDTH;
        maxY = DEF_HEIGHT;
        sy = 0;
        stickyW = stickyH = Sticky.CENTER;
    }


    public LGuiScreen(String img){
        this(img, DEF_WIDTH, DEF_HEIGHT);
    }


    public LGuiScreen(String img, int maxX, int maxY){
        this(img, maxX, maxY, 0, Sticky.CENTER, Sticky.CENTER);
    }


    public LGuiScreen(String img, int maxX, int maxY, int shiftY, Sticky w, Sticky h){
        this.img = new LResLocation(img);
        title = null;
        this.maxY = maxY - shiftY;
        this.maxX = maxX;
        this.sy = shiftY;
        this.stickyH = h;
        this.stickyW = w;
    }


    public void initGui() {
        float wc = maxX/width;
        float wh = maxY/height;
        float scale;

        if(stickyH == Sticky.CENTER){
            if(stickyW == Sticky.CENTER){
                scale = Math.min(wh, wc);
            } else  {
                scale = wh;
            }
        } else if (stickyW == Sticky.CENTER) {
            scale = wc;
        } else {
            scale = Math.min(wh, wc);
        }

        float base = ((maxX - width * scale) / scale);
        switch (stickyW){
            case CENTER:
                shiftX1 = shiftX2 = (base / 2f);
                break;
            case TOP   :
                shiftX1 = base * 2;
                shiftX2 = 0;
                break;
            case BUTTON:
                shiftX1 = 0;
                shiftX2 = base * 2;
                break;
        }
        base = ((maxY - height * scale) / scale);
        switch (stickyH){
            case CENTER:
                shiftY1 = shiftY2 = (base / 2f);
                break;
            case TOP   :
                shiftY1 = base * 2;
                shiftY2 = 0;
                break;
            case BUTTON:
                shiftY1 = 0;
                shiftY2 = base * 2;
                break;
        }
    }


    public void setSize(int w, int h) {
        this.width = w;
        this.height = h;
        initGui();
    }


    public void drawImageSafe(){
        if(img==null)return;
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(img);
        drawImg();
        GlStateManager.popMatrix();
    }


    public void drawImg() {
        float f = 1/maxX;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(0, height,0).tex(shiftX1*f,            (maxY-shiftY2+sy)*f ).endVertex();
        bufferbuilder.pos(width,height,0).tex((maxX - shiftX2) * f, (maxY-shiftY2+sy)*f ).endVertex();
        bufferbuilder.pos(width,0,  0).tex((maxX - shiftX2) * f, (shiftY1+sy)*f      ).endVertex();
        bufferbuilder.pos(0, 0,  0).tex(shiftX1*f,             (shiftY1+sy)*f     ).endVertex();
        tessellator.draw();
    }


    public enum Sticky{
        TOP, BUTTON, CENTER
    }
}