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
public class LGuiMainMenu extends LGuiScreen{
    private static final LGuiMainMenu inst = new LGuiMainMenu();
    private ResourceLocation addImg;

    private LGuiMainMenu() {
        super("options_bg", 1280, 729);
    }


    private void editBg(Object obj){
        LResLocation newImg = new LResLocation(obj);
        if(newImg.equals(addImg) || newImg.equals(img))return;
        if(addImg != null) img = addImg;
        addImg = newImg;
        alpha = 0;
        lastDraw = -1;
    }


    public static LGuiMainMenu open(Object otherImg){
        inst.editBg(otherImg);
        return inst;
    }

    public static LGuiMainMenu get(){
        return inst;
    }


    public void drawImageSafe(){
        if(img!=null) {
            GlStateManager.pushMatrix();
            Minecraft.getMinecraft().getTextureManager().bindTexture(img);
            drawImg();
            GlStateManager.popMatrix();
        }
        drawAddImg();
    }


    private long lastDraw = -1;
    private float alpha = 0;


    private void drawAddImg(){
        if(addImg==null)return;
        long cur = System.currentTimeMillis();
        if(lastDraw==-1){
            alpha = 0;
            lastDraw = cur;
            return;
        } else {
            alpha += (0.0033f * (cur-lastDraw));
            lastDraw = cur;
        }
        if(alpha>=1){
            alpha = 0;
            img = addImg;
            addImg = null;
            super.drawImageSafe();
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();

        Minecraft.getMinecraft().getTextureManager().bindTexture(addImg);
        drawImg(alpha);

        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.popMatrix();
    }


    private void drawImg(float alpha) {
        float f = 1/maxX;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(5, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(0, height,0).tex(shiftX1*f,            (maxY-shiftY2+sy)*f ).color(1,1,1, alpha).endVertex();
        bufferbuilder.pos(width,height,0).tex((maxX - shiftX2) * f, (maxY-shiftY2+sy)*f ).color(1,1,1, alpha).endVertex();
        bufferbuilder.pos(width,0,  0).tex((maxX - shiftX2) * f, (shiftY1+sy)*f      ).color(1,1,1, alpha).endVertex();
        bufferbuilder.pos(0, 0,  0).tex(shiftX1*f,             (shiftY1+sy)*f     ).color(1,1,1, alpha).endVertex();
        bufferbuilder.pos(0, height,0).tex(shiftX1*f,            (maxY-shiftY2+sy)*f ).color(1,1,1, alpha).endVertex();
        tessellator.draw();
    }
}