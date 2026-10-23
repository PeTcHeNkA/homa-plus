package plus.invlib.render;

import net.minecraft.client.renderer.GlStateManager;


public abstract class RenderableBasic implements Renderable{
    float scale = 1;
    int zIndex = 0;
    int xPos = 0;
    int yPos = 0;
    boolean cantDraw = true;
    boolean needResize = false;


    public static boolean validScale(float scale){
        return !(scale < 0.0000001);
    }


    public static boolean isNeedScale(float scale){
        return scale - 1 > 0.0000001 || scale - 1 < 0.0000001;
    }


    public Renderable update(int x, int y, int z, float scale){
        if(validScale(scale)){
            if(isNeedScale(scale)){
                needResize = true;
                this.scale = scale;
            }
        } else {
            cantDraw = true;
            return this;
        }
        xPos = (int) ((float)x/scale);
        yPos = (int) ((float)y/scale);
        zIndex = (int) ((float)z/scale);

        cantDraw = false;
        return this;
    }


    @Override
    public void draw() {
        if(cantDraw)return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, zIndex);
        if(needResize){
            GlStateManager.scale(scale,scale,scale);
        }
        onDraw();
        GlStateManager.popMatrix();
    }


    protected abstract void onDraw();
}