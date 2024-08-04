package net.minecraft.network.play.client;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;


public class CPacketPlayerTryUseItemOnBlock implements Packet<INetHandlerPlayServer> {
    private BlockPos position;
    private EnumFacing placedBlockDirection;
    private EnumHand hand;
    private float facingX;
    private float facingY;
    private float facingZ;

    public CPacketPlayerTryUseItemOnBlock() {}

    @SideOnly(Side.CLIENT)
    public CPacketPlayerTryUseItemOnBlock(BlockPos pos, EnumFacing facing, EnumHand hand, float fx, float fy, float fz) {
        this.position = pos;
        this.placedBlockDirection = facing;
        this.hand = hand;
        this.facingX = fx;
        this.facingY = fy;
        this.facingZ = fz;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.position = buf.readBlockPos();
        this.placedBlockDirection = buf.readEnumValue(EnumFacing.class);
        this.hand = buf.readEnumValue(EnumHand.class);
        this.facingX = buf.readFloat();
        this.facingY = buf.readFloat();
        this.facingZ = buf.readFloat();
    }


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {
        this.position = NetUtils.readBlockPos(buf);
        //todo check this?
        this.placedBlockDirection = NetUtils.readEnumValueFast(buf, EnumFacing.VALUES);
        this.hand = NetUtils.readEnumValueFast(buf, EnumHand.VALUES);
        this.facingX = buf.readFloat();
        this.facingY = buf.readFloat();
        this.facingZ = buf.readFloat();
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeBlockPos(this.position);
        buf.writeEnumValue(this.placedBlockDirection);
        buf.writeEnumValue(this.hand);
        buf.writeFloat(this.facingX);
        buf.writeFloat(this.facingY);
        buf.writeFloat(this.facingZ);
    }


    @Override
    public void processPacket(INetHandlerPlayServer proc) {
        proc.processTryUseItemOnBlock(this);
    }


    public BlockPos getPos() {
        return this.position;
    }


    public EnumFacing getDirection() {
        return this.placedBlockDirection;
    }


    public EnumHand getHand() {
        return this.hand;
    }


    public float getFacingX() {
        return this.facingX;
    }


    public float getFacingY() {
        return this.facingY;
    }


    public float getFacingZ() {
        return this.facingZ;
    }
}