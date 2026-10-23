package plus.gen;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber
public class ClientListener {
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load e){
        if(e.getWorld().isRemote && e.getWorld().provider.getDimension()==0) {
            //Te4WorldGen.buffer.clear();
            if (Te4WorldGen.updateOnLoadWorld) {
                Te4WorldGen.init();
            }
        }
    }
}