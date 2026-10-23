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


public class SPacketMoveVehicle extends SPacketPlayBase {
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public SPacketMoveVehicle() {}

    public SPacketMoveVehicle(Entity entity) {
        this.x = entity.posX;
        this.y = entity.posY;
        this.z = entity.posZ;
        this.yaw = entity.rotationYaw;
        this.pitch = entity.rotationPitch;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.yaw = buf.readFloat();
        this.pitch = buf.readFloat();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleMoveVehicle(this);
    }


    @SideOnly(Side.CLIENT)
    public double getX() {
        return this.x;
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
    public float getYaw() {
        return this.yaw;
    }


    @SideOnly(Side.CLIENT)
    public float getPitch() {
        return this.pitch;
    }
}