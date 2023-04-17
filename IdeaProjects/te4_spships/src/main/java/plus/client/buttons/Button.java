package plus.client.buttons;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import plus.client.SpGui;


public class Button extends Gui {
    private final ItemStack ico;

    public Button(ItemStack ico) {
        this.ico = ico;
    }


    public void draw(int x, int y, boolean isActive) {
        draw(x, y, isActive, ico);
    }


    public void draw(int x, int y, boolean isActive, ItemStack itemIco) {
        SpGui.mc.getTextureManager().bindTexture(SpGui.RECIPE_BOOK);
        GlStateManager.disableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (isActive) {
            this.drawTexturedModalRect(x - 32, y, 153 + 35, 2, 35, 26);
        } else {
            this.drawTexturedModalRect(x - 30, y, 153, 2, 30, 26);
        }
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        if (itemIco != null) {
            SpGui.mc.getRenderItem().renderItemAndEffectIntoGUI(itemIco, x - 21, y + 5);
        }
        GlStateManager.enableLighting();
        RenderHelper.disableStandardItemLighting();
    }
}
