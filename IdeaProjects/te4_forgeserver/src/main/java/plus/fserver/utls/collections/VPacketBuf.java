package plus.fserver.utls.collections;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;


@Deprecated
public final class VPacketBuf implements Runnable{
    private final EntityPlayerMP player;
    private final EntityPlayerMP[] players;
    private final Packet<?>[] packets;
    private final Packet<?>[] packets2;
    private int cursor;
    private int cursor2;

    public VPacketBuf(EntityPlayerMP player, EntityPlayerMP[] players) {
        packets = new Packet[8];
        packets2 = new Packet[4];
        this.player = player;
        this.players = players;
    }

    public void add(Packet<?> packet){
        packets[cursor++] = packet;
    }


    public void addSelf(Packet<?> packet){
        packets2[cursor2++] = packet;
    }


    @Override
    public void run() {
        int i;
        for (i=0;i<cursor2;i++)
            player.connection.netManager.sendPacket(packets2[i]);
        for (EntityPlayerMP player:players){
            for (i=0;i<cursor;i++)
                player.connection.netManager.sendPacket(packets[i]);
        }
    }
}