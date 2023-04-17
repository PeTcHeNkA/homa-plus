package plus.client.event;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.SpMain;

import static plus.client.CTaskShoulder.INST;


@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = SpMain.MOD_ID)
public class Handler {
    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e){
        INST.tick();
    }
}