package plus.client.invlib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;


public class RItemFrame extends RenderableBasic {
    public static final RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();

    protected ItemStack item;


    public RItemFrame(){}
    public RItemFrame(ItemStack item){
        this.item = item;
        cantDraw = false;
    }


    public RItemFrame update(ItemStack item, int x, int y, int z, float size) {
        if(item==null){
            cantDraw = true;
        } else {
            this.item = item;
            return (RItemFrame) update(x, y, z, size);
        }
        return this;
    }


    public RItemFrame update(ItemStack item){
        if(item==null){
            cantDraw = true;
        } else {
            this.item = item;
        }
        return this;
    }


    @Override
    public void draw() {
        if(cantDraw)return;
        GlStateManager.pushMatrix();


        GlStateManager.translate(0, 0, zIndex);
        if(needResize){
            GlStateManager.scale(scale,scale,scale);
            //GlStateManager.translate(xPos/scale, yPos/scale, zIndex);
        }
        onDraw();
        GlStateManager.popMatrix();
    }


    @Override
    protected void onDraw() {
        RenderHelper.enableGUIStandardItemLighting();
        itemRender.renderItemAndEffectIntoGUI(item, (int) (xPos/scale), (int) (yPos/scale));
        RenderHelper.disableStandardItemLighting();
    }
}