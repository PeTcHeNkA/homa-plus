package net.minecraft.network.login.client;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginServer;
import plus.fserver.server.network.NetUtils;


public class CPacketLoginStart implements Packet<INetHandlerLoginServer> {
    private GameProfile profile;

    public CPacketLoginStart() {}

    public CPacketLoginStart(GameProfile profile) {
        this.profile = profile;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.profile = new GameProfile(null, buf.readString(16));
    }


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {
        this.profile = new GameProfile(null, NetUtils.readString(buf, 16));
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeString(this.profile.getName());
    }


    @Override
    public void processPacket(INetHandlerLoginServer proc) {
        proc.processLoginStart(this);
    }


    public GameProfile getProfile() {
        return this.profile;
    }
}