package plus.server;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class ServerEvents {
    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget().world.isRemote) return;
        if (!(event.getTarget() instanceof EntityPlayerMP)) return;
        ServPacketSyncRenderItem.sendPlayerCacheOf(
                (EntityPlayerMP) event.getEntityPlayer(), event.getTarget().getEntityId()
        );
    }
}