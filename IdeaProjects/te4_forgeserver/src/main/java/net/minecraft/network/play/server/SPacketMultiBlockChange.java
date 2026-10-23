package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketMultiBlockChange extends SNodePacketPlayBase {
    private int chunkX, chunkZ;
    private BlockUpdateData[] changedBlocks;

    public SPacketMultiBlockChange() {}

    public void init(int x, int z, BlockUpdateData[] data){
        this.chunkX = x;
        this.chunkZ = z;
        this.changedBlocks = data;
    }


    public SPacketMultiBlockChange(int count, short[] updates, Chunk chunk) {
        this.chunkX = chunk.x;
        this.chunkZ = chunk.z;
        BlockUpdateData[] changedBlocks = this.changedBlocks = new BlockUpdateData[count];

        for(int i = 0; i < changedBlocks.length; ++i) {
            changedBlocks[i] = new BlockUpdateData(this, updates[i], chunk);
        }
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();

        this.changedBlocks = new BlockUpdateData[buf.readVarInt()];

        for(int i = 0; i < this.changedBlocks.length; ++i) {
            this.changedBlocks[i] = new BlockUpdateData(this, buf.readShort(), Block.BLOCK_STATE_IDS.getByValue(buf.readVarInt()));
        }
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeVarInt(this.changedBlocks.length);
        BlockUpdateData[] var2 = this.changedBlocks;

        for (BlockUpdateData data : var2) {
            buf.writeShort(data.getOffset());
            buf.writeVarInt(data.getBlockState().getId());
        }
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);

        BlockUpdateData[] var2 = this.changedBlocks;
        NetUtils.writeVarInt(buf, var2.length);

        for (BlockUpdateData data : var2) {
            buf.writeShort(data.getOffset());
            NetUtils.writeVarInt(buf, data.getBlockState().getId());
        }
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleMultiBlockChange(this);
    }


    @SideOnly(Side.CLIENT)
    public BlockUpdateData[] getChangedBlocks() {
        return this.changedBlocks;
    }


    public static class BlockUpdateDataBase{
        void setParent(SPacketMultiBlockChange parent){}
    }


    public BlockUpdateData data(short offset, IBlockState state){
        return new BlockUpdateData(this, offset, state);
    }


    public static class BlockUpdateData extends BlockUpdateDataBase{
        private final short offset;
        private final IBlockState blockState;
        @SideOnly(Side.CLIENT)
        private SPacketMultiBlockChange parent;

        public BlockUpdateData(SPacketMultiBlockChange parent, short offset, IBlockState state) {
            setParent(parent);
            this.offset = offset;
            this.blockState = state;
        }


        @SideOnly(Side.CLIENT)
        void setParent(SPacketMultiBlockChange parent) {
            this.parent = parent;
        }


        public BlockUpdateData(SPacketMultiBlockChange parent, short offset, Chunk chunk) {
            setParent(parent);
            this.offset = offset;
            this.blockState = chunk.getBlockStateUnchecked(
                    (chunk.x << 4) + (offset >> 12 & 15),
                    offset & 255,
                    (chunk.z << 4) + (offset >> 8 & 15)
            );
        }


        @SideOnly(Side.CLIENT)
        public BlockPos getPos() {
            final int offset = this.offset;
            return new BlockPos(
                    (parent.chunkX << 4) + (offset >> 12 & 15),
                    offset & 255,
                    (parent.chunkZ << 4) + (offset >> 8 & 15)
            );
        }


        public short getOffset() {
            return this.offset;
        }


        public IBlockState getBlockState() {
            return this.blockState;
        }
    }
}
