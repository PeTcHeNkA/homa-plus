package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketBlockAction extends SPacketPlayBase {
    private BlockPos blockPosition;
    private int instrument;
    private int pitch;
    private Block block;

    public SPacketBlockAction() {}


    public SPacketBlockAction(BlockPos pos, Block block, int p_i46966_3_, int p_i46966_4_) {
        this.blockPosition = pos;
        this.instrument = p_i46966_3_;
        this.pitch = p_i46966_4_;
        this.block = block;
    }


    @Override
    public void readPacketData(PacketBuffer p_148837_1_) throws IOException {
        this.blockPosition = p_148837_1_.readBlockPos();
        this.instrument = p_148837_1_.readUnsignedByte();
        this.pitch = p_148837_1_.readUnsignedByte();
        this.block = Block.getBlockById(p_148837_1_.readVarInt() & 4095);
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeBlockPos(this.blockPosition);
        buf.writeByte(this.instrument);
        buf.writeByte(this.pitch);
        buf.writeVarInt(Block.getIdFromBlock(this.block) & 4095);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeBlockPos(buf, this.blockPosition);
        buf.writeByte(this.instrument);
        buf.writeByte(this.pitch);
        NetUtils.writeVarInt(buf,Block.getIdFromBlock(this.block) & 4095);
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleBlockAction(this);
    }


    @SideOnly(Side.CLIENT)
    public BlockPos getBlockPosition() {
        return this.blockPosition;
    }


    @SideOnly(Side.CLIENT)
    public int getData1() {
        return this.instrument;
    }


    @SideOnly(Side.CLIENT)
    public int getData2() {
        return this.pitch;
    }


    @SideOnly(Side.CLIENT)
    public Block getBlockType() {
        return this.block;
    }
}