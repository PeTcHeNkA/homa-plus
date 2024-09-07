package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketBlockBreakAnim extends SPacketPlayBase {
    private int breakerId;
    private BlockPos position;
    private int progress;

    public SPacketBlockBreakAnim() {}

    public SPacketBlockBreakAnim(int id, BlockPos pos, int progress) {
        this.breakerId = id;
        this.position = pos;
        this.progress = progress;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.breakerId = buf.readVarInt();
        this.position = buf.readBlockPos();
        this.progress = buf.readUnsignedByte();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.breakerId);
        buf.writeBlockPos(this.position);
        buf.writeByte(this.progress);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeVarInt(buf, this.breakerId);
        NetUtils.writeBlockPos(buf, this.position);
        buf.writeByte(this.progress);
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleBlockBreakAnim(this);
    }


    @SideOnly(Side.CLIENT)
    public int getBreakerId() {
        return this.breakerId;
    }


    @SideOnly(Side.CLIENT)
    public BlockPos getPosition() {
        return this.position;
    }


    @SideOnly(Side.CLIENT)
    public int getProgress() {
        return this.progress;
    }
}