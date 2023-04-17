package plus.client.mashine;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.SpMain;
import plus.common.mashine.press.SpPressContainer;


@SideOnly(Side.CLIENT)
public class GuiSpPress extends GuiContainer {
    private static final ResourceLocation SP_PRESS_GUI =
            new ResourceLocation(SpMain.MOD_ID,"textures/gui/ibm_compress.png");
    private final IInventory spPressInv;

    public GuiSpPress(InventoryPlayer playerInv, IInventory spPressInv) {
        super(new SpPressContainer(playerInv, spPressInv));
        this.spPressInv = spPressInv;
    }


    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.mc.getTextureManager().bindTexture(SP_PRESS_GUI);

        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        drawProgress(i,j, spPressInv.getField(0)/100f);
    }


    protected void drawProgress(int i, int j, float f){
        this.drawTexturedModalRect(i+80, j+38, 176, 0, (int) (24*f), 18);
    }
}