package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;


public class SPacketEntity implements Packet<INetHandlerPlayClient> {
    protected int entityId;

    public SPacketEntity() {}

    public SPacketEntity(int id) {
        this.entityId = id;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityId = buf.readVarInt();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.entityId);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);
        NetUtils.writeVarInt(buf, this.entityId);
    }


    @Deprecated
    public void processPacket(INetHandlerPlayClient client) {
        client.handleEntityMovement(this);
    }


    @Deprecated
    public String toString() {
        return "Entity_" + super.toString();
    }


    @SideOnly(Side.CLIENT)
    public Entity getEntity(World world) {
        return world.getEntityByID(this.entityId);
    }

    @SideOnly(Side.CLIENT) public int     getX()        {return 0    ;}
    @SideOnly(Side.CLIENT) public int     getY()        {return 0    ;}
    @SideOnly(Side.CLIENT) public int     getZ()        {return 0    ;}
    @SideOnly(Side.CLIENT) public byte    getYaw()      {return 0    ;}
    @SideOnly(Side.CLIENT) public byte    getPitch()    {return 0    ;}
    @SideOnly(Side.CLIENT) public boolean isRotating()  {return false;}
    @SideOnly(Side.CLIENT) public boolean getOnGround() {return false;}


    public static class SPacketEntityPos extends SPacketEntity{
        protected int posX;
        protected int posY;
        protected int posZ;
        protected boolean onGround;
        public SPacketEntityPos(){}
        public SPacketEntityPos(int id) {super(id);}
        @SideOnly(Side.CLIENT) public int getX() {return this.posX;}
        @SideOnly(Side.CLIENT) public int getY() {return this.posY;}
        @SideOnly(Side.CLIENT) public int getZ() {return this.posZ;}
        @SideOnly(Side.CLIENT) public boolean getOnGround() {return this.onGround;}
    }


    public static class S16PacketEntityLook extends SPacketEntity {
        protected byte yaw;
        protected byte pitch;
        protected boolean rotating;
        protected boolean onGround;
        @SideOnly(Side.CLIENT) public byte getYaw() {return this.yaw;}
        @SideOnly(Side.CLIENT) public byte getPitch() {return this.pitch;}
        @SideOnly(Side.CLIENT) public boolean isRotating() {return this.rotating;}
        @SideOnly(Side.CLIENT) public boolean getOnGround() {return this.onGround;}

        public S16PacketEntityLook() {
            this.rotating = true;
        }

        public S16PacketEntityLook(int id, byte yaw, byte pith, boolean onGround) {
            super(id);
            this.yaw = yaw;
            this.pitch = pith;
            this.rotating = true;
            this.onGround = onGround;
        }


        @Deprecated
        public void readPacketData(PacketBuffer buf) throws IOException {
            super.readPacketData(buf);
            this.yaw = buf.readByte();
            this.pitch = buf.readByte();
            this.onGround = buf.readBoolean();
        }


        @Deprecated
        public void writePacketData(PacketBuffer buf) throws IOException {
            super.writePacketData(buf);
            buf.writeByte(this.yaw);
            buf.writeByte(this.pitch);
            buf.writeBoolean(this.onGround);
        }


        @Override
        public void writePacketData(int id, ByteBuf buf) throws IOException {
            super.writePacketData(id, buf);
            buf.writeByte(this.yaw);
            buf.writeByte(this.pitch);
            buf.writeBoolean(this.onGround);
        }
    }

    public static class S15PacketEntityRelMove extends SPacketEntityPos {
        public S15PacketEntityRelMove() {
            super();
        }

        public S15PacketEntityRelMove(int id, long posX, long posY, long posZ, boolean onGround) {
            super(id);
            this.posX = (int)posX;
            this.posY = (int)posY;
            this.posZ = (int)posZ;
            this.onGround = onGround;
        }


        @Deprecated
        public void readPacketData(PacketBuffer buf) throws IOException {
            super.readPacketData(buf);
            this.posX = buf.readShort();
            this.posY = buf.readShort();
            this.posZ = buf.readShort();
            this.onGround = buf.readBoolean();
        }


        @Deprecated
        public void writePacketData(PacketBuffer buf) throws IOException {
            super.writePacketData(buf);
            buf.writeShort(this.posX);
            buf.writeShort(this.posY);
            buf.writeShort(this.posZ);
            buf.writeBoolean(this.onGround);
        }


        @Override
        public void writePacketData(int id, ByteBuf buf) throws IOException {
            super.writePacketData(id, buf);
            buf.writeShort(this.posX);
            buf.writeShort(this.posY);
            buf.writeShort(this.posZ);
            buf.writeBoolean(this.onGround);
        }
    }

    public static class S17PacketEntityLookMove extends SPacketEntityPos {
        protected byte yaw;
        protected byte pitch;
        protected boolean rotating;
        @SideOnly(Side.CLIENT) public byte getYaw() {return this.yaw;}
        @SideOnly(Side.CLIENT) public byte getPitch() {return this.pitch;}
        @SideOnly(Side.CLIENT) public boolean isRotating() {return this.rotating;}

        public S17PacketEntityLookMove() {
            this.rotating = true;
        }

        public S17PacketEntityLookMove(int id, long posX, long posY, long posZ, byte yaw, byte pith, boolean onGround) {
            super(id);
            this.posX = (int)posX;
            this.posY = (int)posY;
            this.posZ = (int)posZ;
            this.yaw = yaw;
            this.pitch = pith;
            this.onGround = onGround;
            this.rotating = true;
        }


        @Deprecated
        public void readPacketData(PacketBuffer buf) throws IOException {
            super.readPacketData(buf);
            this.posX = buf.readShort();
            this.posY = buf.readShort();
            this.posZ = buf.readShort();
            this.yaw = buf.readByte();
            this.pitch = buf.readByte();
            this.onGround = buf.readBoolean();
        }


        @Deprecated
        public void writePacketData(PacketBuffer buf) throws IOException {
            super.writePacketData(buf);
            buf.writeShort(this.posX);
            buf.writeShort(this.posY);
            buf.writeShort(this.posZ);
            buf.writeByte(this.yaw);
            buf.writeByte(this.pitch);
            buf.writeBoolean(this.onGround);
        }


        @Override
        public void writePacketData(int id, ByteBuf buf) throws IOException {
            super.writePacketData(id, buf);
            buf.writeShort(this.posX);
            buf.writeShort(this.posY);
            buf.writeShort(this.posZ);
            buf.writeByte(this.yaw);
            buf.writeByte(this.pitch);
            buf.writeBoolean(this.onGround);
        }
    }
}