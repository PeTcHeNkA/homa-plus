package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketAnimation extends SPacketPlayBase {
    private int entityId;
    private int type;

    public SPacketAnimation() {}

    public SPacketAnimation(Entity entity, int type) {
        this.entityId = entity.getEntityId();
        this.type = type;
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityId = buf.readVarInt();
        this.type = buf.readUnsignedByte();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.entityId);
        buf.writeByte(this.type);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeVarInt(buf, this.entityId);
        buf.writeByte(this.type);
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleAnimation(this);
    }


    @SideOnly(Side.CLIENT)
    public int getEntityID() {
        return this.entityId;
    }


    @SideOnly(Side.CLIENT)
    public int getAnimationType() {
        return this.type;
    }
}