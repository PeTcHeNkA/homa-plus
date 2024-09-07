package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketEntityHeadLook extends SPacketPlayBase {
    private int entityId;
    private byte yaw;

    public SPacketEntityHeadLook() {}

    public SPacketEntityHeadLook(Entity entity, byte yaw) {
        this.entityId = entity.getEntityId();
        this.yaw = yaw;
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityId = buf.readVarInt();
        this.yaw = buf.readByte();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.entityId);
        buf.writeByte(this.yaw);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeVarInt(buf, this.entityId);
        buf.writeByte(this.yaw);
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleEntityHeadLook(this);
    }


    @SideOnly(Side.CLIENT)
    public Entity getEntity(World world) {
        return world.getEntityByID(this.entityId);
    }


    @SideOnly(Side.CLIENT)
    public byte getYaw() {
        return this.yaw;
    }
}