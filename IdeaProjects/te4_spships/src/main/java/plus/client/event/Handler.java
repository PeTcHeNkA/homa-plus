package plus.client.event;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.client.camera.Camera;

import static plus.client.CTaskShoulder.INST;


@SideOnly(Side.CLIENT)
public class Handler {
    public static void initEventHandlers(){
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent e){
        INST.tick();
    }
}