package net.minecraft.world.chunk;

import io.netty.buffer.FastByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockIDIndexes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class BlockStatePaletteLinear implements IBlockStatePalette {
    private final IBlockState[] states;
    private int arraySize;

    public BlockStatePaletteLinear(int bitsIn) {
        this.states = new IBlockState[1 << bitsIn];
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
            return -1;
        }
    }


    @Override // not null
    public IBlockState get(int indexKey) {
        return indexKey >= 0 && indexKey < this.arraySize ? this.states[indexKey] : BlockIDIndexes.AIR_STATE;
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
        int i = PacketBuffer.getVarIntSize(this.arraySize);
        IBlockState state;
        for (int j = 0; j < this.arraySize; ++j) {
            state = this.states[j];
            i += PacketBuffer.getVarIntSize(state==null? BlockIDIndexes.AIR_STATE_ID:state.getId());
        }
        return i;
    }
}