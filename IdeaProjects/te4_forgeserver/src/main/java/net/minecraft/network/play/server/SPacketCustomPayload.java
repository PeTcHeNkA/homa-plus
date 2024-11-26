package net.minecraft.network.play.server;

import net.minecraft.network.play.*;
import java.io.*;
import io.netty.buffer.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraft.network.*;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;


public class SPacketCustomPayload extends SNodePacketPlayBase {
    private String channel;
    private PacketBuffer data;

    public SPacketCustomPayload() {}

    public SPacketCustomPayload(final String channelIn, final PacketBuffer bufIn) {
        this.channel = channelIn;
        this.data = bufIn;
        if (bufIn.writerIndex() > 1048576) {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
    }


    @Deprecated
    public void readPacketData(final PacketBuffer buf) throws IOException {
        this.channel = buf.readString(20);
        final int i = buf.readableBytes();
        if (i >= 0 && i <= 1048576) {
            this.data = new PacketBuffer(buf.readBytes(i));
            return;
        }
        throw new IOException("Payload may not be larger than 1048576 bytes");
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeString(buf, this.channel);
        synchronized (this.data) {
            this.data.markReaderIndex();
            buf.writeBytes(this.data);
            this.data.resetReaderIndex();
        }
    }


    @Deprecated
    public void writePacketData(final PacketBuffer buf) throws IOException {
        buf.writeString(this.channel);
        synchronized (this.data) {
            this.data.markReaderIndex();
            buf.writeBytes(this.data);
            this.data.resetReaderIndex();
        }
    }


    @Override
    public void processPacket(final INetHandlerPlayClient handler) {
        handler.handleCustomPayload(this);
        if (this.data != null) {
            this.data.release();
        }
    }


    @SideOnly(Side.CLIENT)
    public String getChannelName() {
        return this.channel;
    }


    @SideOnly(Side.CLIENT)
    public PacketBuffer getBufferData() {
        return this.data;
    }
}
