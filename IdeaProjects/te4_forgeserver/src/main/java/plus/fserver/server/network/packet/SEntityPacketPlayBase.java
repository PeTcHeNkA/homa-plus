package plus.fserver.server.network.packet;

import net.minecraft.entity.player.EntityPlayerMP;
import java.util.function.Consumer;


public abstract class SEntityPacketPlayBase extends SNodePacketPlayBase implements Consumer<EntityPlayerMP> {
    @Override
    public void accept(EntityPlayerMP player) {
        player.connection.netManager.addToQueueN(this);
    }
}