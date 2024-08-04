package net.minecraft.network.handshake.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.handshake.*;
import net.minecraftforge.fml.relauncher.*;
import java.io.*;
import net.minecraft.network.*;
import plus.fserver.server.network.NetUtils;


public class C00Handshake implements Packet<INetHandlerHandshakeServer> {
    private int protocolVersion;
    private String ip;
    private int port;
    private EnumConnectionState requestedState;
    private boolean hasFMLMarker;

    public C00Handshake() {
        this.hasFMLMarker = false;
    }

    @SideOnly(Side.CLIENT)
    public C00Handshake(final String ip, final int port, final EnumConnectionState state) {
        this.hasFMLMarker = false;
        this.protocolVersion = 340;
        this.ip = ip;
        this.port = port;
        this.requestedState = state;
    }

    public C00Handshake(final String ip, final int port, final EnumConnectionState state, final boolean fml) {
        this(ip, port, state);
        this.hasFMLMarker = fml;
    }


    @Deprecated
    public void readPacketData(final PacketBuffer buf) throws IOException {
        this.protocolVersion = buf.readVarInt();
        this.ip = buf.readString(255);
        this.port = buf.readUnsignedShort();
        this.requestedState = EnumConnectionState.getById(buf.readVarInt());
        this.hasFMLMarker = this.ip.contains("\u0000FML\u0000");
        this.ip = this.ip.split("\u0000")[0];
    }


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {
        this.protocolVersion = NetUtils.readVarInt(buf);
        this.ip = NetUtils.readString(buf, 255);
        this.port = buf.readUnsignedShort();
        this.requestedState = EnumConnectionState.getById(NetUtils.readVarInt(buf));
        this.hasFMLMarker = this.ip.contains("\u0000FML\u0000");
        this.ip = this.ip.split("\u0000")[0];
    }


    @Override
    public void writePacketData(final PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.protocolVersion);
        buf.writeString(this.ip + "\u0000FML\u0000");
        buf.writeShort(this.port);
        buf.writeVarInt(this.requestedState.getId());
    }


    @Override
    public void processPacket(final INetHandlerHandshakeServer handler) {
        handler.processHandshake(this);
    }


    public EnumConnectionState getRequestedState() {
        return this.requestedState;
    }


    public int getProtocolVersion() {
        return this.protocolVersion;
    }


    public boolean hasFMLMarker() {
        return this.hasFMLMarker;
    }
}