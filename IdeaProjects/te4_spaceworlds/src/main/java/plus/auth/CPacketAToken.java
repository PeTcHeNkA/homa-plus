package plus.auth;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.io.IOException;

public class CPacketAToken implements Packet<INetHandlerLoginServer> {
    private String token;

    public CPacketAToken(){}

    public CPacketAToken(String token) {
        this.token = token;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        token = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        ByteBufUtils.writeUTF8String(buf, token);
    }

    @Override
    public void processPacket(INetHandlerLoginServer handler) {
        ((ISelfNetLoginServer)handler).processToken(token);
    }
}
