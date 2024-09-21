package net.minecraft.world.chunk;

import io.netty.buffer.FastByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockIDIndexes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class BlockStatePaletteLinear extends BlockStatePaletteBase implements IBlockStatePalette {
    private final IBlockStatePaletteResizer resizeHandler;
    private final int bits;

    public BlockStatePaletteLinear(int bitsIn, IBlockStatePaletteResizer resizeHandlerIn) {
        this.states = new IBlockState[1 << bitsIn];
        this.bits = bitsIn;
        this.resizeHandler = resizeHandlerIn;
    }


    @Override
    public int idFor(IBlockState state) {
        final IBlockState[] states = this.states;
        final int size = this.arraySize;

        for (int i = 0; i < size; ++i) {
            if (states[i] == state) return i;
        }

        if (size < states.length) {
            states[size] = state;
            ++this.arraySize;
            return size;
        } else {
            return this.resizeHandler.onResize(this.bits + 1, state);
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void read(PacketBuffer buf) {
        this.arraySize = buf.readVarInt();
        for (int i = 0; i < this.arraySize; ++i) {
            this.states[i] = Block.BLOCK_STATE_IDS.getByValue(buf.readVarInt());
        }
    }


    @Override
    public void write(PacketBuffer buf) {
        buf.writeVarInt(this.arraySize);
        IBlockState state;
        for (int i = 0; i < this.arraySize; ++i) {
            state = this.states[i];
            buf.writeVarInt(state==null?BlockIDIndexes.AIR_STATE_ID:state.getId());
        }
    }


    @Override
    public void write(FastByteBuf buf) {
        final int size = this.arraySize;
        IBlockState[] states = this.states;
        buf.writeVarInt(size);
        IBlockState state;
        for (int i = 0; i < size; ++i) {
            state = states[i];
            buf.writeVarInt(state==null?BlockIDIndexes.AIR_STATE_ID:state.getId());
        }
    }


    @Override
    public int getSerializedSize() {
        final int size;
        int i = PacketBuffer.getVarIntSize(size = this.arraySize);
        IBlockState[] states = this.states;
        for (int j = 0; j < size; ++j) {
            IBlockState state = states[j];
            i += PacketBuffer.getVarIntSize(state==null? BlockIDIndexes.AIR_STATE_ID:state.getId());
        }
        return i;
    }
}