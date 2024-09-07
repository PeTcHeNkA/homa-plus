package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketUpdateHealth extends SPacketPlayBase {
    private float health;
    private int foodLevel;
    private float saturationLevel;

    public SPacketUpdateHealth() {}

    public SPacketUpdateHealth(float health, int foodLvl, float saturationLvl) {
        this.health = health;
        this.foodLevel = foodLvl;
        this.saturationLevel = saturationLvl;
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.health = buf.readFloat();
        this.foodLevel = buf.readVarInt();
        this.saturationLevel = buf.readFloat();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeFloat(this.health);
        buf.writeVarInt(this.foodLevel);
        buf.writeFloat(this.saturationLevel);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeFloat(this.health);
        NetUtils.writeVarInt(buf, this.foodLevel);
        buf.writeFloat(this.saturationLevel);
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleUpdateHealth(this);
    }


    @SideOnly(Side.CLIENT)
    public float getHealth() {
        return this.health;
    }


    @SideOnly(Side.CLIENT)
    public int getFoodLevel() {
        return this.foodLevel;
    }


    @SideOnly(Side.CLIENT)
    public float getSaturationLevel() {
        return this.saturationLevel;
    }
}