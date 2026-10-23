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

    private final IInventory inv;
    public GuiSpReact(InventoryPlayer playerInv, IInventory spReactInv) {
        super(SP_REACT_GUI, new SpReactContainer(playerInv, spReactInv));
        this.inv = spReactInv;
    }


    @Override
    protected void onDraw(int dx, int dy) {
        double genSpeed = inv.getField(0);
        int curenrg = inv.getField(1);
        String enrgStr;
        if(genSpeed>99999){
            if(genSpeed>999999)
                enrgStr = ((int)genSpeed/100)+"E/T";
            else
                enrgStr = (((int) genSpeed/10)/10d)+"E/T";
        } else
            enrgStr = (genSpeed/100)+"E/T";

        drawString(enrgStr, dx+64, dy+11, COLOR_GRAY);
        drawString("E: "+curenrg, dx+71, dy+20, COLOR_GRAY);
    }
}