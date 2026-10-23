package plus.space.client.lscreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.io.IOException;
import static plus.space.client.gui.GuiStarMap.renderItemModel;


@SideOnly(Side.CLIENT)
public class LGuiLoadingScreen extends GuiScreen {
    private final LGuiScreen lGuiScreen = LGuiScreen.getLast();


    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        lGuiScreen.setSize(width, height);
    }


    @Override
    public void setGuiSize(int w, int h) {
        super.setGuiSize(w, h);
        lGuiScreen.setSize(w, h);
    }


    @Override
    public void drawBackground(int tint) {
        lGuiScreen.drawImageSafe();
    }


    public void drawImg(){
        lGuiScreen.drawImageSafe();
    }

    public void drawImgFull(){
        lGuiScreen.drawImageSafe();
        drawObj(new ItemStack(Blocks.STONE));
    }


    public void drawImgWithTitle(String title){
        lGuiScreen.drawImageSafe();
        //TODO
    }


    private void drawObj(ItemStack item){
        drawObj(width/2f, height/2f, 5f, item);
    }

    private static float angleY = 12.5f, angleX = 180 - 12.5f;
    //private static long lastDraw = System.currentTimeMillis();

    private void drawObj(double x, double y, float scale, ItemStack item){
        scale*=30;
        GlStateManager.pushMatrix();

        long cur = System.currentTimeMillis();
        angleX -= 0.15f;
        //lastDraw = cur;

        GlStateManager.translate(x,y,0);
        GlStateManager.rotate(180, 0,0, 1);
        GlStateManager.rotate(angleX, 0, 1, 0);
        GlStateManager.rotate(angleY, 1,0, 0);
        GlStateManager.scale(scale, scale, scale);
        RenderHelper.enableStandardItemLighting();
        renderItemModel(item);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }


    @Override
    public void initGui() {}


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        //
    }
}