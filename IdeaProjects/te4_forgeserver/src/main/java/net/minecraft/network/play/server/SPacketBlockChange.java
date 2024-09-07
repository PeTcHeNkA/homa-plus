package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketBlockChange extends SPacketPlayBase {
    private BlockPos blockPosition;
    public IBlockState blockState;

    public SPacketBlockChange() {}


    public SPacketBlockChange(World worldIn, BlockPos posIn) {
        this.blockPosition = posIn;
        this.blockState = worldIn.access().getBlockState(posIn);
    }


    public SPacketBlockChange(IBlockEAccess access, BlockPos posIn) {
        this.blockPosition = posIn;
        this.blockState = access.getBlockState(posIn);
    }


    public SPacketBlockChange(IBlockState state, BlockPos posIn) {
        this.blockPosition = posIn;
        this.blockState = state;
    }



    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.blockPosition = buf.readBlockPos();
        this.blockState = BlockIDIndexes.BLOCK_INDEX.get(buf.readVarInt());
        //this.blockState = Block.BLOCK_STATE_IDS.getByValue(buf.readVarInt());
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeBlockPos(this.blockPosition);
        buf.writeVarInt(blockState.getId());
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeBlockPos(buf, this.blockPosition);
        NetUtils.writeVarInt(buf, blockState.getId());
    }


    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleBlockChange(this);
    }


    @SideOnly(Side.CLIENT)
    public IBlockState getBlockState() {
        return this.blockState;
    }


    @SideOnly(Side.CLIENT)
    public BlockPos getBlockPosition() {
        return this.blockPosition;
    }
}