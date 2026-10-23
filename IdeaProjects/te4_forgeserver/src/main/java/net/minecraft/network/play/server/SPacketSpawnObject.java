package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.UUID;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;


public class SPacketSpawnObject extends SNodePacketPlayBase {
    private int entityId;
    private UUID uniqueId;
    private double x;
    private double y;
    private double z;
    private int speedX;
    private int speedY;
    private int speedZ;
    private int pitch;
    private int yaw;
    private int type;
    private int data;

    public SPacketSpawnObject() {}

    public SPacketSpawnObject(Entity entity, int type) {
        this(entity, type, 0);
    }


    public SPacketSpawnObject(int id, UUID uuid, double x, double y, double z, byte yaw, byte pith, int type, int data) {
        this.entityId = id;
        this.uniqueId = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pith;
        this.yaw = yaw;
        this.type = type;
        this.data = data;
    }


    public SPacketSpawnObject setSpeed(int x, int y, int z){
        this.speedX = x;
        this.speedY = y;
        this.speedZ = z;
        return this;
    }


    public SPacketSpawnObject(Entity entity, int type, int data) {
        this.entityId = entity.getEntityId();
        this.uniqueId = entity.getUniqueID();
        this.x = entity.posX;
        this.y = entity.posY;
        this.z = entity.posZ;
        this.pitch = MathHelper.floor(entity.rotationPitch * 256.0F / 360.0F);
        this.yaw = MathHelper.floor(entity.rotationYaw * 256.0F / 360.0F);
        this.type = type;
        this.data = data;
        this.speedX = (int)(MathHelper.clamp(entity.motionX, -3.9, 3.9) * 8000.0);
        this.speedY = (int)(MathHelper.clamp(entity.motionY, -3.9, 3.9) * 8000.0);
        this.speedZ = (int)(MathHelper.clamp(entity.motionZ, -3.9, 3.9) * 8000.0);
    }


    public SPacketSpawnObject(Entity entity, int type, int data, BlockPos pos) {
        this(entity, type, data);
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityId = buf.readVarInt();
        this.uniqueId = buf.readUniqueId();
        this.type = buf.readByte();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.pitch = buf.readByte();
        this.yaw = buf.readByte();
        this.data = buf.readInt();
        this.speedX = buf.readShort();
        this.speedY = buf.readShort();
        this.speedZ = buf.readShort();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.entityId);
        buf.writeUniqueId(this.uniqueId);
        buf.writeByte(this.type);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeByte(this.pitch);
        buf.writeByte(this.yaw);
        buf.writeInt(this.data);
        buf.writeShort(this.speedX);
        buf.writeShort(this.speedY);
        buf.writeShort(this.speedZ);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeVarInt(buf, this.entityId);
        NetUtils.writeUniqueId(buf, this.uniqueId);
        buf.writeByte(this.type);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeByte(this.pitch);
        buf.writeByte(this.yaw);
        buf.writeInt(this.data);
        buf.writeShort(this.speedX);
        buf.writeShort(this.speedY);
        buf.writeShort(this.speedZ);
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleSpawnObject(this);
    }


    @SideOnly(Side.CLIENT)
    public int getEntityID() {
        return this.entityId;
    }


    public void setSpeedX(int speed) {
        this.speedX = speed;
    }


    @SideOnly(Side.CLIENT)
    public UUID getUniqueId() {
        return this.uniqueId;
    }


    public void setSpeedY(int speed) {
        this.speedY = speed;
    }


    @SideOnly(Side.CLIENT)
    public double getX() {
        return this.x;
    }


    public void setSpeedZ(int speed) {
        this.speedZ = speed;
    }


    @SideOnly(Side.CLIENT)
    public double getY() {
        return this.y;
    }


    @SideOnly(Side.CLIENT)
    public double getZ() {
        return this.z;
    }


    @SideOnly(Side.CLIENT)
    public int getSpeedX() {
        return this.speedX;
    }


    @SideOnly(Side.CLIENT)
    public int getSpeedY() {
        return this.speedY;
    }


    @SideOnly(Side.CLIENT)
    public int getSpeedZ() {
        return this.speedZ;
    }


    @SideOnly(Side.CLIENT)
    public int getPitch() {
        return this.pitch;
    }


    @SideOnly(Side.CLIENT)
    public int getYaw() {
        return this.yaw;
    }


    @SideOnly(Side.CLIENT)
    public int getType() {
        return this.type;
    }


    @SideOnly(Side.CLIENT)
    public int getData() {
        return this.data;
    }


    @SideOnly(Side.CLIENT)
    public void setData(int data) {
        this.data = data;
    }
}