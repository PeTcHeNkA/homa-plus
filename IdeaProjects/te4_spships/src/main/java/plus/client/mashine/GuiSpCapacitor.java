package plus.client.mashine;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.SpMain;
import plus.client.invlib.SimpleGuiContainer;
import plus.common.mashine.capicator.SpCapacitorContainer;


@SideOnly(Side.CLIENT)
public class GuiSpCapacitor extends SimpleGuiContainer {
    private static final ResourceLocation SP_CAP_GUI =
            new ResourceLocation(SpMain.MOD_ID,"textures/gui/ibm_cond.png");

    public GuiSpCapacitor(InventoryPlayer playerInv, IInventory spCapInv){
        super(SP_CAP_GUI,new SpCapacitorContainer(playerInv, spCapInv));
    }
}