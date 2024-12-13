package net.minecraft.network.play.server;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;


public class SPacketCustomSound extends SNodePacketPlayBase {
    private String soundName;
    private SoundCategory category;
    private int x;
    private int y = Integer.MAX_VALUE;
    private int z;
    private float volume;
    private float pitch;

    public SPacketCustomSound() {}


    public SPacketCustomSound(String name, SoundCategory category, double x, double y, double z, float volume, float pitch) {
        Validate.notNull(name, "name");
        this.soundName = name;
        this.category = category;
        this.x = (int)(x * 8.0);
        this.y = (int)(y * 8.0);
        this.z = (int)(z * 8.0);
        this.volume = volume;
        this.pitch = pitch;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.soundName = buf.readString(256);
        this.category = buf.readEnumValue(SoundCategory.class);
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeString(this.soundName);
        buf.writeEnumValue(this.category);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeFloat(this.volume);
        buf.writeFloat(this.pitch);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);
        NetUtils.writeString(buf, this.soundName);
        NetUtils.writeEnumValue(buf, this.category);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeFloat(this.volume);
        buf.writeFloat(this.pitch);
    }


    @SideOnly(Side.CLIENT)
    public String getSoundName() {
        return this.soundName;
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleCustomSound(this);
    }


    @SideOnly(Side.CLIENT)
    public SoundCategory getCategory() {
        return this.category;
    }


    @SideOnly(Side.CLIENT)
    public double getX() {
        return (double)((float)this.x / 8.0F);
    }


    @SideOnly(Side.CLIENT)
    public double getY() {
        return (float)this.y / 8.0F;
    }


    @SideOnly(Side.CLIENT)
    public double getZ() {
        return (float)this.z / 8.0F;
    }


    @SideOnly(Side.CLIENT)
    public float getVolume() {
        return this.volume;
    }


    @SideOnly(Side.CLIENT)
    public float getPitch() {
        return this.pitch;
    }
}