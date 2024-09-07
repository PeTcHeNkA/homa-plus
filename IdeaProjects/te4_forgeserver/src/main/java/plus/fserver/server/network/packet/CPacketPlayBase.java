package plus.fserver.server.network.packet;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayServer;


public abstract class CPacketPlayBase implements Packet<INetHandlerPlayServer> {
    @Override
    public EnumConnectionState getConnectionState() {
        return EnumConnectionState.PLAY;
    }
}