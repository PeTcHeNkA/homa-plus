package net.minecraft.world.chunk;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockIDIndexes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.utls.NetUtils;


public class BlockStatePaletteLinear implements IBlockStatePalette {
    private final IBlockState[] states;
    private final IBlockStatePaletteResizer resizeHandler;
    private final int bits;
    private int arraySize;

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
    public void write(ByteBuf buf) {
        final int size = this.arraySize;
        IBlockState[] states = this.states;
        NetUtils.writeVarInt(buf, size);
        IBlockState state;
        for (int i = 0; i < size; ++i) {
            state = states[i];
            NetUtils.writeVarInt(buf, state==null?BlockIDIndexes.AIR_STATE_ID:state.getId());
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