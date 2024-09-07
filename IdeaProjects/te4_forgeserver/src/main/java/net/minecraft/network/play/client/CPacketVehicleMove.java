package net.minecraft.network.play.client;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import plus.fserver.server.network.packet.CPacketPlayBase;


public class CPacketVehicleMove extends CPacketPlayBase {
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public CPacketVehicleMove() {}

    public CPacketVehicleMove(Entity entity) {
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


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.yaw = buf.readFloat();
        this.pitch = buf.readFloat();
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
    }


    @Override
    public void processPacket(INetHandlerPlayServer proc) {
        proc.processVehicleMove(this);
    }


    public double getX() {
        return this.x;
    }


    public double getY() {
        return this.y;
    }


    public double getZ() {
        return this.z;
    }


    public float getYaw() {
        return this.yaw;
    }


    public float getPitch() {
        return this.pitch;
    }
}