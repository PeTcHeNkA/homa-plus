package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.IBlockEAccess;


public class SPacketBlockChange implements Packet<INetHandlerPlayClient> {
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


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeBlockPos(this.blockPosition);
        buf.writeVarInt(blockState.getId());
        //buf.writeVarInt(Block.BLOCK_STATE_IDS.get(this.blockState));
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