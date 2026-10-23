package plus.inventory;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import plus.main;

public class CapabilityEventHandler {

    public static void register(){
        MinecraftForge.EVENT_BUS.register(new CapabilityEventHandler());
    }

    public static final ResourceLocation INVENTORY_CAP = new ResourceLocation(main.MOD_ID, "inventory");

    //ОЧЕНЬ ВАЖНО! Добавляет капу игроку при его первом создании

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent event) {
//        if(e)
//        if (event instanceof EntityPlayer){
            event.addCapability(INVENTORY_CAP, new CAPCustomInventoryProvider());
//        }
    }
}