package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SEntityPacketPlayBase;


public class SPacketEntityTeleport extends SEntityPacketPlayBase {
    private int entityId;
    private double posX;
    private double posY;
    private double posZ;
    private byte yaw;
    private byte pitch;
    private boolean onGround;

    public SPacketEntityTeleport() {}

    public SPacketEntityTeleport(int id, double x, double y, double z, byte yaw, byte pith, boolean onGround){
        this.entityId = id;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.yaw = yaw;
        this.pitch = pith;
        this.onGround = onGround;
    }


    public SPacketEntityTeleport(Entity entity) {
        this.entityId = entity.getEntityId();
        this.posX = entity.posX;
        this.posY = entity.posY;
        this.posZ = entity.posZ;
        this.yaw = (byte)((int)(entity.rotationYaw * 256.0F / 360.0F));
        this.pitch = (byte)((int)(entity.rotationPitch * 256.0F / 360.0F));
        this.onGround = entity.onGround;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityId = buf.readVarInt();
        this.posX = buf.readDouble();
        this.posY = buf.readDouble();
        this.posZ = buf.readDouble();
        this.yaw = buf.readByte();
        this.pitch = buf.readByte();
        this.onGround = buf.readBoolean();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.entityId);
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeByte(this.yaw);
        buf.writeByte(this.pitch);
        buf.writeBoolean(this.onGround);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeVarInt(buf, this.entityId);
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeByte(this.yaw);
        buf.writeByte(this.pitch);
        buf.writeBoolean(this.onGround);
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleEntityTeleport(this);
    }


    @SideOnly(Side.CLIENT)
    public int getEntityId() {
        return this.entityId;
    }

    @SideOnly(Side.CLIENT)
    public double getX() {
        return this.posX;
    }

    @SideOnly(Side.CLIENT)
    public double getY() {
        return this.posY;
    }

    @SideOnly(Side.CLIENT)
    public double getZ() {
        return this.posZ;
    }

    @SideOnly(Side.CLIENT)
    public byte getYaw() {
        return this.yaw;
    }

    @SideOnly(Side.CLIENT)
    public byte getPitch() {
        return this.pitch;
    }

    @SideOnly(Side.CLIENT)
    public boolean getOnGround() {
        return this.onGround;
    }
}