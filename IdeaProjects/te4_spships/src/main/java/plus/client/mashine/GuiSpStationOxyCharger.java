package plus.client.mashine;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.SpMain;
import plus.common.mashine.oxy.SpStationOxyChargerContainer;


@SideOnly(Side.CLIENT)
public class GuiSpStationOxyCharger extends GuiSpOxyCharger {
    private static final ResourceLocation SP_OXY_STATION_CHARGER_GUI =
            new ResourceLocation(SpMain.MOD_ID,"textures/gui/sp_oxy_station.png");

    public GuiSpStationOxyCharger(InventoryPlayer playerInv, IInventory oxyInv) {
        super(new SpStationOxyChargerContainer(playerInv, oxyInv), oxyInv);

    }


    @Override
    protected ResourceLocation getBg() {
        return SP_OXY_STATION_CHARGER_GUI;
    }


    @Override
    protected void onDraw(int i, int j, boolean animVentil) {
        super.onDraw(i+9, j+6, animVentil || true);

        drawLiquid(i, j);
        drawAir(i, j);
    }


    private static int calcPercent(int maxReal, int maxPixel, int value){
        if(value <= 0)return 0;
        if(value >= maxReal)return maxPixel;
        return (int) ((float) value / maxReal * maxPixel);
    }


    //176, 88, 10x30, 6x30
    protected void drawLiquid(int i, int j){
        int shift = 30 - calcPercent(SpStationOxyChargerContainer.MAX_LIQUID, 30, getField(1));

        drawTexturedModalRect(i+12, j+28+shift,
                176, 88, 10, 30-shift);
    }


    protected void drawAir(int i, int j){
        int shift = 30 - calcPercent(SpStationOxyChargerContainer.MAX_AIR, 30, getField(2));

        drawTexturedModalRect(i+22, j+28+shift,
                176+10, 88, 6, 30-shift);
    }
}