package net.minecraft.network.play.server;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;


public class SPacketEntityVelocity implements Packet<INetHandlerPlayClient> {
    private int entityID;
    private int motionX;
    private int motionY;
    private int motionZ;

    public SPacketEntityVelocity() {}

    public SPacketEntityVelocity(Entity entity) {
        this(entity.getEntityId(), entity.motionX, entity.motionY, entity.motionZ);
    }


    public SPacketEntityVelocity(int id, double mx, double my, double mz) {
        this.entityID = id;
        if (mx < -3.9) {
            mx = -3.9;
        }

        if (my < -3.9) {
            my = -3.9;
        }

        if (mz < -3.9) {
            mz = -3.9;
        }

        if (mx > 3.9) {
            mx = 3.9;
        }

        if (my > 3.9) {
            my = 3.9;
        }

        if (mz > 3.9) {
            mz = 3.9;
        }

        this.motionX = (int)(mx * 8000.0);
        this.motionY = (int)(my * 8000.0);
        this.motionZ = (int)(mz * 8000.0);
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityID = buf.readVarInt();
        this.motionX = buf.readShort();
        this.motionY = buf.readShort();
        this.motionZ = buf.readShort();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.entityID);
        buf.writeShort(this.motionX);
        buf.writeShort(this.motionY);
        buf.writeShort(this.motionZ);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeVarInt(buf, this.entityID);
        buf.writeShort(this.motionX);
        buf.writeShort(this.motionY);
        buf.writeShort(this.motionZ);
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleEntityVelocity(this);
    }


    @SideOnly(Side.CLIENT)
    public int getEntityID() {
        return this.entityID;
    }


    @SideOnly(Side.CLIENT)
    public int getMotionX() {
        return this.motionX;
    }


    @SideOnly(Side.CLIENT)
    public int getMotionY() {
        return this.motionY;
    }


    @SideOnly(Side.CLIENT)
    public int getMotionZ() {
        return this.motionZ;
    }
}