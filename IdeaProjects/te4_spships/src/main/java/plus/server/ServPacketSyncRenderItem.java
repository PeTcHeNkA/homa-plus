package plus.server;

import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import plus.common.network.PacketSyncRenderItem;
import static plus.proxy.ServerProxy.network;


//TODO CLEAR CACHE ON OFFLINE PLAYERS
public class ServPacketSyncRenderItem {
    public static final PacketSyncRenderItem.AMap<PacketSyncRenderItem> serverCache = new PacketSyncRenderItem.AMap<>();

    public static void sendToAllTracking1SlotOfCache(Entity player, int slot){
        if(serverCache.get(player.getEntityId(), slot)==null)return;
        network.sendToAllTracking(
                serverCache.get(player.getEntityId(), slot),player
        );
    }


    public static void sendToAllTrackingCacheOf(Entity player){
        IntObjectHashMap<PacketSyncRenderItem> slots = serverCache.get(player.getEntityId());
        for(PacketSyncRenderItem slot: slots.values()){
            network.sendToAllTracking(slot,player);
        }
    }


    public static void sendPlayerCacheOf(EntityPlayerMP player, int targetId){
        IntObjectHashMap<PacketSyncRenderItem> slots = serverCache.get(targetId);
        for(PacketSyncRenderItem slot: slots.values()){
            network.sendTo(slot,player);
        }
    }


    public static void updatePlayerCache(EntityPlayerMP player, int slot, ItemStack item){
        PacketSyncRenderItem packet = new PacketSyncRenderItem(item, player.getEntityId(), slot);
        serverCache.add(player.getEntityId(), slot, packet);
        sendToAllTracking1SlotOfCache(player, slot);
    }
}
