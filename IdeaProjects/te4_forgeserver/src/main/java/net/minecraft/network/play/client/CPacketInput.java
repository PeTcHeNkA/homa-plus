package net.minecraft.network.play.client;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class CPacketInput implements Packet<INetHandlerPlayServer> {
    private float strafeSpeed;
    private float forwardSpeed;
    private boolean jumping;
    private boolean sneaking;

    public CPacketInput() {}

    @SideOnly(Side.CLIENT)
    public CPacketInput(float strafe, float forward, boolean jumping, boolean sneaking) {
        this.strafeSpeed = strafe;
        this.forwardSpeed = forward;
        this.jumping = jumping;
        this.sneaking = sneaking;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.strafeSpeed = buf.readFloat();
        this.forwardSpeed = buf.readFloat();
        byte flags = buf.readByte();
        this.jumping = (flags & 1) > 0;
        this.sneaking = (flags & 2) > 0;
    }


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {
        this.strafeSpeed = buf.readFloat();
        this.forwardSpeed = buf.readFloat();
        byte flags = buf.readByte();
        this.jumping = (flags & 1) > 0;
        this.sneaking = (flags & 2) > 0;
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeFloat(this.strafeSpeed);
        buf.writeFloat(this.forwardSpeed);
        byte bflags = 0;
        if (this.jumping) {
            bflags = (byte)(bflags | 1);
        }

        if (this.sneaking) {
            bflags = (byte)(bflags | 2);
        }

        buf.writeByte(bflags);
    }


    public void processPacket(INetHandlerPlayServer proc) {
        proc.processInput(this);
    }


    public float getStrafeSpeed() {
        return this.strafeSpeed;
    }


    public float getForwardSpeed() {
        return this.forwardSpeed;
    }


    public boolean isJumping() {
        return this.jumping;
    }


    public boolean isSneaking() {
        return this.sneaking;
    }
}