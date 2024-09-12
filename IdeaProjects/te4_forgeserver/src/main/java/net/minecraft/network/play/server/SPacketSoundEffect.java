package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;


public class SPacketSoundEffect extends SNodePacketPlayBase {
    private SoundEvent sound;
    private SoundCategory category;
    private int posX;
    private int posY;
    private int posZ;
    private float soundVolume;
    private float soundPitch;

    public SPacketSoundEffect() {}


    public SPacketSoundEffect(SoundEvent sound, SoundCategory category, double x, double y, double z, float volume, float pith) {
        if(sound == null)throw new NullPointerException();
        this.sound = sound;
        this.category = category;
        this.posX = (int)(x * 8.0);
        this.posY = (int)(y * 8.0);
        this.posZ = (int)(z * 8.0);
        this.soundVolume = volume;
        this.soundPitch = pith;
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.sound = SoundEvent.REGISTRY.getObjectById(buf.readVarInt());
        this.category = buf.readEnumValue(SoundCategory.class);
        this.posX = buf.readInt();
        this.posY = buf.readInt();
        this.posZ = buf.readInt();
        this.soundVolume = buf.readFloat();
        this.soundPitch = buf.readFloat();
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(SoundEvent.REGISTRY.getIDForObject(this.sound));
        buf.writeEnumValue(this.category);
        buf.writeInt(this.posX);
        buf.writeInt(this.posY);
        buf.writeInt(this.posZ);
        buf.writeFloat(this.soundVolume);
        buf.writeFloat(this.soundPitch);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeVarInt(buf, SoundEvent.REGISTRY.getIDForObject(this.sound));
        NetUtils.writeEnumValue(buf, this.category);
        buf.writeInt(this.posX);
        buf.writeInt(this.posY);
        buf.writeInt(this.posZ);
        buf.writeFloat(this.soundVolume);
        buf.writeFloat(this.soundPitch);
    }


    @SideOnly(Side.CLIENT)
    public SoundEvent getSound() {
        return this.sound;
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleSoundEffect(this);
    }


    @SideOnly(Side.CLIENT)
    public SoundCategory getCategory() {
        return this.category;
    }


    @SideOnly(Side.CLIENT)
    public double getX() {
        return (float)this.posX / 8.0F;
    }


    @SideOnly(Side.CLIENT)
    public double getY() {
        return (float)this.posY / 8.0F;
    }


    @SideOnly(Side.CLIENT)
    public double getZ() {
        return (float)this.posZ / 8.0F;
    }


    @SideOnly(Side.CLIENT)
    public float getVolume() {
        return this.soundVolume;
    }


    @SideOnly(Side.CLIENT)
    public float getPitch() {
        return this.soundPitch;
    }
}