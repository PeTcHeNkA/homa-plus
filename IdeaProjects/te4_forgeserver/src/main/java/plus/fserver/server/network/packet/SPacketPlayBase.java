package plus.fserver.server.network.packet;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;


public abstract class SPacketPlayBase implements Packet<INetHandlerPlayClient> {
    @Override
    public EnumConnectionState getConnectionState() {
        return EnumConnectionState.PLAY;
    }
}