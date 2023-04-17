package plus.client.mashine;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.SpMain;
import plus.client.invlib.SimpleGuiContainer;
import plus.common.mashine.react.SpReactContainer;


@SideOnly(Side.CLIENT)
public class GuiSpReact extends SimpleGuiContainer {
    private static final ResourceLocation SP_REACT_GUI =
            new ResourceLocation(SpMain.MOD_ID,"textures/gui/aether_react.png");

    public GuiSpReact(InventoryPlayer playerInv, IInventory spReactInv) {
        super(SP_REACT_GUI, new SpReactContainer(playerInv, spReactInv));
    }
}