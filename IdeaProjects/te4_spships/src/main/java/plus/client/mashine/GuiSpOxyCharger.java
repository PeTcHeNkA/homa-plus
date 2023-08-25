package plus.client.mashine;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.SpMain;
import plus.common.mashine.oxy.SpOxyChargerContainer;


@SideOnly(Side.CLIENT)
public class GuiSpOxyCharger extends GuiContainer {
    private static final ResourceLocation SP_OXY_CHARGER_GUI =
            new ResourceLocation(SpMain.MOD_ID,"textures/gui/oxy_charger.png");


    private final IInventory oxyInv;//TODO


    public GuiSpOxyCharger(InventoryPlayer playerInv, IInventory oxyInv) {
        super(new SpOxyChargerContainer(playerInv, oxyInv));
        this.oxyInv = oxyInv;
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
        this.mc.getTextureManager().bindTexture(SP_OXY_CHARGER_GUI);

        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        drawVentil(i,j, oxyInv.getField(0)==1);
    }


    private int tick = 0;

    protected void drawVentil(int i, int j, boolean animate){
        final int shiftY = 22*tick;

        this.drawTexturedModalRect(i+70, j+44, 176, shiftY, 22, 22);

        if(animate){
            if((tick+=1)>3){
                tick = 0;
            }
        }
    }
}