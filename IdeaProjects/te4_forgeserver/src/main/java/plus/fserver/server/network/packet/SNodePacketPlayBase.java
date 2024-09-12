package plus.fserver.server.network.packet;

import net.minecraft.network.Packet;
import plus.fserver.server.network.IPacketNodeQueue;


public abstract class SNodePacketPlayBase extends SPacketPlayBase implements IPacketNodeQueue {
    @Override
    public Packet<?> packet() {
        return this;
    }
}
