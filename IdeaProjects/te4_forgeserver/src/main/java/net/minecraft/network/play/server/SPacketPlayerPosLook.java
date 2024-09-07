package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketPlayerPosLook extends SPacketPlayBase {
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private Set<EnumFlags> flags;
    private int teleportId;

    public SPacketPlayerPosLook() {}


    public SPacketPlayerPosLook(double x, double y, double z, float yaw, float pith, Set<EnumFlags> flags, int id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pith;
        this.flags = flags;
        this.teleportId = id;
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.yaw = buf.readFloat();
        this.pitch = buf.readFloat();
        this.flags = SPacketPlayerPosLook.EnumFlags.unpack(buf.readUnsignedByte());
        this.teleportId = buf.readVarInt();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeByte(SPacketPlayerPosLook.EnumFlags.pack(this.flags));
        buf.writeVarInt(this.teleportId);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeByte(SPacketPlayerPosLook.EnumFlags.pack(this.flags));
        NetUtils.writeVarInt(buf, this.teleportId);
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handlePlayerPosLook(this);
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


    @SideOnly(Side.CLIENT)
    public int getTeleportId() {
        return this.teleportId;
    }


    @SideOnly(Side.CLIENT)
    public Set<EnumFlags> getFlags() {
        return this.flags;
    }


    public static enum EnumFlags {
        X(0),
        Y(1),
        Z(2),
        Y_ROT(3),
        X_ROT(4);

        private final int bit;

        private EnumFlags(int p_i46690_3_) {
            this.bit = p_i46690_3_;
        }

        private int getMask() {
            return 1 << this.bit;
        }

        private boolean isSet(int p_187043_1_) {
            return (p_187043_1_ & this.getMask()) == this.getMask();
        }

        public static Set<EnumFlags> unpack(int p_187044_0_) {
            Set<EnumFlags> lvt_1_1_ = EnumSet.noneOf(EnumFlags.class);
            EnumFlags[] var2 = values();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                EnumFlags lvt_5_1_ = var2[var4];
                if (lvt_5_1_.isSet(p_187044_0_)) {
                    lvt_1_1_.add(lvt_5_1_);
                }
            }

            return lvt_1_1_;
        }

        public static int pack(Set<EnumFlags> p_187040_0_) {
            int lvt_1_1_ = 0;

            EnumFlags lvt_3_1_;
            for(Iterator var2 = p_187040_0_.iterator(); var2.hasNext(); lvt_1_1_ |= lvt_3_1_.getMask()) {
                lvt_3_1_ = (EnumFlags)var2.next();
            }

            return lvt_1_1_;
        }
    }
}
