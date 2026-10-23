package plus.client.mashine;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
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

    protected final IInventory oxyInv;//TODO

    public GuiSpOxyCharger(Container container, IInventory inv) {
        super(container);
        this.oxyInv = inv;
    }


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
        this.mc.getTextureManager().bindTexture(getBg());

        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        onDraw(i, j, getField(0)>0);
    }


    protected int getField(int id){
        return oxyInv.getField(id);
    }


    protected ResourceLocation getBg(){
        return SP_OXY_CHARGER_GUI;
    }


    protected int tick = 0;

    protected void onDraw(int i, int j, boolean animVentil){
        final int shiftY = 22*tick;

        this.drawTexturedModalRect(i+70, j+44, 176, shiftY, 22, 22);

        if(animVentil){
            if((tick+=1)>3){
                tick = 0;
            }
        }
    }
}