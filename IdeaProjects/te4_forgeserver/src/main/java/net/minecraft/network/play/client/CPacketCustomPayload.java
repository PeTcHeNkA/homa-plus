package net.minecraft.network.play.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.play.*;
import net.minecraftforge.fml.relauncher.*;
import java.io.*;
import net.minecraft.network.*;
import plus.fserver.api.Keep;
import plus.fserver.server.network.NetUtils;


@Keep
public class CPacketCustomPayload implements Packet<INetHandlerPlayServer> {
    private String channel;
    private PacketBuffer data;

    @Keep
    public CPacketCustomPayload() {}

    @SideOnly(Side.CLIENT)
    public CPacketCustomPayload(final String channelIn, final PacketBuffer bufIn) {
        this.channel = channelIn;
        this.data = bufIn;
        if (bufIn.writerIndex() > 32767) {
            throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
        }
    }


    @Deprecated
    public void readPacketData(final PacketBuffer buf) throws IOException {
        this.channel = buf.readString(20);
        final int i = buf.readableBytes();
        if (i >= 0 && i <= 32767) {
            this.data = new PacketBuffer(buf.readBytes(i));
            return;
        }
        throw new IOException("Payload may not be larger than 32767 bytes");
    }


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {
        this.channel = NetUtils.readString(buf, 20);
        final int i = buf.readableBytes();
        if (i >= 0 && i <= 32767) {
            this.data = new PacketBuffer(buf.readBytes(i));
            return;
        }
        throw new IOException("Payload may not be larger than 32767 bytes");
    }


    @Override
    public void writePacketData(final PacketBuffer buf) throws IOException {
        buf.writeString(this.channel);
        synchronized (this.data) {
            this.data.markReaderIndex();
            buf.writeBytes(this.data);
            this.data.resetReaderIndex();
        }
    }


    @Override
    public void processPacket(final INetHandlerPlayServer handler) {
        handler.processCustomPayload(this);
        if (this.data != null) {
            this.data.release();
        }
    }


    public PacketBuffer getAndRemData(){
        PacketBuffer buf = this.data;
        this.data = null;
        return buf;
    }


    public String getChannelName() {
        return this.channel;
    }


    public PacketBuffer getBufferData() {
        return this.data;
    }
}