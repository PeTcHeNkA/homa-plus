package net.minecraft.network.play.client;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.CPacketPlayBase;


public class CPacketChatMessage extends CPacketPlayBase {
    private String message;

    public CPacketChatMessage() {}

    public CPacketChatMessage(String msg) {
        if (msg.length() > 256) {
            msg = msg.substring(0, 256);
        }
        this.message = msg;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.message = buf.readString(256);
    }


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {
        this.message = NetUtils.readString(buf,256);
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeString(this.message);
    }


    @Override
    public void processPacket(INetHandlerPlayServer proc) {
        proc.processChatMessage(this);
    }


    public String getMessage() {
        return this.message;
    }
}