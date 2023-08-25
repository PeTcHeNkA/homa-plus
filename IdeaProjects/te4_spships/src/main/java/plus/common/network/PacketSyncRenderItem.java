package plus.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import static plus.proxy.ServerProxy.network;


public final class PacketSyncRenderItem implements IMessage {
    public static class AMap<T> extends IntObjectHashMap<IntObjectHashMap<T>>{
        public void add(int int1, int int2, T obj) {
            get(int1).put(int2, obj);
        }
        public T get(int int1, int int2) {
            if (containsKey(int1)) {
                return get(int1).get(int2);
            } else return null;
        }
        public IntObjectHashMap<T> get(int int1) {
            if (containsKey(int1)) {
                return super.get(int1);
            } else {
                IntObjectHashMap<T> m = new IntObjectHashMap<>();
                put(int1, m);
                return m;
            }
        }
    }


    @SideOnly(Side.CLIENT)
    public static final AMap<ItemStack> clientCache = new AMap<ItemStack>(){
        public ItemStack get(int int1, int int2) {
            if (containsKey(int1)) {
                return get(int1).get(int2);
            } else return ItemStack.EMPTY;
        }
    };
    public static final AMap<PacketSyncRenderItem> serverCache = new AMap<>();


    public static final class ClientHandler implements IMessageHandler<PacketSyncRenderItem, PacketSyncRenderItem> {
        @Override
        public PacketSyncRenderItem onMessage(PacketSyncRenderItem sp, MessageContext ctx) {
            clientCache.add(sp.id, sp.slot, sp.item);
            return null;
        }
    }

    public ItemStack item;
    public int id;
    public int slot;

    public PacketSyncRenderItem(){}
    public PacketSyncRenderItem(ItemStack item, int id, int slot){
        this.item = item;
        this.id = id;
        this.slot = slot;
    }

    @Override
    public String toString(){
        return String.format("Entity: [%s], Slot: [%s], Item: [%s] ",id,slot,item);
    }


    @Override
    public final void fromBytes(ByteBuf data) {
        id = data.readInt();
        slot = data.readInt();
        item = ByteBufUtils.readItemStack(data);
    }


    @Override
    public final void toBytes(ByteBuf data) {
        data.writeInt(id);
        data.writeInt(slot);
        ByteBufUtils.writeItemStack(data,item);
    }


    public static void updatePlayerCache(EntityPlayerMP player, int slot, ItemStack item){
        PacketSyncRenderItem packet = new PacketSyncRenderItem(item, player.getEntityId(), slot);
        serverCache.add(player.getEntityId(), slot, packet);
        sendToAllTracking1SlotOfCache(player, slot);
    }


    @SideOnly(Side.CLIENT)
    public static void updateSelfCache(int slot, ItemStack item){
        clientCache.add(Minecraft.getMinecraft().player.getEntityId(), slot, item);
    }


    public static void sendPlayerCacheOf(EntityPlayerMP player, int targetId){
        IntObjectHashMap<PacketSyncRenderItem> slots = serverCache.get(targetId);
        for(PacketSyncRenderItem slot: slots.values()){
            network.sendTo(slot,player);
        }
    }


    public static void sendToAllTrackingCacheOf(Entity player){
        IntObjectHashMap<PacketSyncRenderItem> slots = serverCache.get(player.getEntityId());
        for(PacketSyncRenderItem slot: slots.values()){
            network.sendToAllTracking(slot,player);
        }
    }


    public static void sendToAllTracking1SlotOfCache(Entity player, int slot){
        if(serverCache.get(player.getEntityId(), slot)==null)return;
        network.sendToAllTracking(
                serverCache.get(player.getEntityId(), slot),player
        );
    }
}