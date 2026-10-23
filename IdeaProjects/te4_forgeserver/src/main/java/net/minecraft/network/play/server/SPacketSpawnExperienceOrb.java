package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketSpawnExperienceOrb extends SPacketPlayBase {
    private int entityID;
    private double posX;
    private double posY;
    private double posZ;
    private int xpValue;

    public SPacketSpawnExperienceOrb() {}

    public SPacketSpawnExperienceOrb(EntityXPOrb orb) {
        this.entityID = orb.getEntityId();
        this.posX = orb.posX;
        this.posY = orb.posY;
        this.posZ = orb.posZ;
        this.xpValue = orb.getXpValue();
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityID = buf.readVarInt();
        this.posX = buf.readDouble();
        this.posY = buf.readDouble();
        this.posZ = buf.readDouble();
        this.xpValue = buf.readShort();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.entityID);
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeShort(this.xpValue);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeVarInt(buf, this.entityID);
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeShort(this.xpValue);
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleSpawnExperienceOrb(this);
    }


    @SideOnly(Side.CLIENT)
    public int getEntityID() {
        return this.entityID;
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
    public int getXPValue() {
        return this.xpValue;
    }
}