package plus.invlib.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Objects;


public class RString extends RenderableBasic {
    public static final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

    protected String string = "";
    protected int xLimit;
    protected int curXPos;
    protected int curYPos;
    protected float curScale = 1;
    protected boolean center = false;

    public RString(){}
    public RString(String s){
        flushStr(s);
    }


    protected void flushStr(String s){
        if(Objects.equals(string, s))return;
        if(s==null || s.equals("")){
            string = "";
            cantDraw = true;
        } else {
            string = parseColors(s);
            recalcForCurString();
        }
    }


    protected void recalcForCurString(){
        cantDraw = false;
        if(center){
            float width = getDefWidth();
            float range = xLimit - xPos - width*scale;

            if(range>=0){
                curXPos = (int) (xPos + range/2);
                curYPos = yPos;
                curScale = scale;
            } else {
                curScale = (xLimit - xPos)/width;
                curYPos = (int) (
                        ((yPos*scale)/curScale) +
                        ((fontRenderer.FONT_HEIGHT - fontRenderer.FONT_HEIGHT*curScale)/2)
                );
                curXPos = (int) ((xPos*scale)/curScale);
            }
        } else {
            curScale = scale;
            curYPos = (int) ((yPos*curScale));
            curXPos = (int) ((xPos*curScale));
        }
        needResize = isNeedScale(curScale);
    }


    public static String parseColors(String src){
        return src.replace("&","\u00a7");
    }


    public RString update(String s){
        flushStr(s);
        return this;
    }


    public RString update(String s, int x, int y, int z, float scale){
        flushStr(s);
        return (RString) update(x, y, z, scale);
    }


    public RString update(int x, int xLimit, int y, int z, float scale, boolean center){
        if(validScale(scale)){
            this.xLimit = (int) (xLimit/scale);
            this.center = center;
        }
        curXPos = x;
        curYPos = y;
        return (RString) update(x, y, z, scale);
    }


    @Override
    public void draw() {
        if(cantDraw)return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, zIndex);
        if(needResize){
            GlStateManager.scale(curScale,curScale,curScale);
        }
        onDraw();
        GlStateManager.popMatrix();
    }


    @Override
    protected void onDraw() {
        fontRenderer.drawStringWithShadow(string, curXPos, curYPos, 0xFFFFFF);
    }


    public int getDefWidth(){
        return fontRenderer.getStringWidth(string);
    }
}