package plus.fserver.server.network.packet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import plus.fserver.server.network.IPacketNodeQueue;
import java.util.function.Consumer;


public abstract class SEntityPacketPlayBase extends SNodePacketPlayBase implements Consumer<EntityPlayerMP> {
    @Override
    public void accept(EntityPlayerMP player) {
        player.connection.netManager.addToQueueN(this);
    }
}