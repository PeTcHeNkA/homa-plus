package net.minecraft.world.chunk;

import javax.annotation.Nullable;
import io.netty.buffer.FastByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.utls.collections.BitArrayMut;
import plus.fserver.utls.collections.IntBlockIndex;


public class BlockStateContainer extends BitArrayMut implements IBlockStatePaletteResizer {
    private static final IBlockStatePalette REGISTRY_BASED_PALETTE = new BlockStatePaletteRegistry();
    protected static final IBlockState AIR_BLOCK_STATE = Blocks.AIR.getDefaultState();
    private IBlockStatePalette palette;
    private int bits;

    public BlockStateContainer() {
        super(4, 4096);
        this.palette = new BlockStatePaletteLinear(this.bits = 4, this);
    }


    private void setBits(int bitsIn) {
        setBits(bitsIn, false);
    }


    private void setBits(int bitsIn, boolean forceBits) {
        if (bitsIn != this.bits) {
            this.bits = bitsIn;

            if (this.bits <= 4) {
                this.bits = 4;
                this.palette = new BlockStatePaletteLinear(this.bits, this);
            } else if (this.bits <= 8) {
                this.palette = new BlockStatePaletteHashMap(this.bits, this);
            } else {
                this.palette = REGISTRY_BASED_PALETTE;
                this.bits = MathHelper.log2DeBruijn(BlockIDIndexes.BLOCK_INDEX.size());
                if (forceBits)
                    this.bits = bitsIn;
            }
            this.palette.idFor(AIR_BLOCK_STATE);
            super.init(this.bits, 4096);
        }
    }


    @Override
    public int onResize(int bits, IBlockState state) {
        IBlockStatePalette palette = this.palette;
        this.setBits(bits);
        for (int i = 0, size = super.size(); i < size; ++i) {
            this.set(i, palette.get(super.getAt(i)));
        }
        return this.palette.idFor(state);
    }


    public final void setC(int x, int y, int z, IBlockState state) {
        this.set(y << 8 | z << 4 | x, state);
    }


    protected void set(int index, IBlockState state) {
        int i = this.palette.idFor(state);
        super.setAt(index, i);
    }


    public IBlockState get(int x, int y, int z) {
        return this.palette.get(super.getAt(y << 8 | z << 4 | x));
    }


    public IBlockState update(int x, int y, int z, final IBlockState state){
        IBlockState result = this.palette.get(super.getAt(x = (y << 8 | z << 4 | x)));
        y = palette.idFor(state);
        super.setAt(x, y);
        return result;
    }


    protected IBlockState get(int index) {
        return this.palette.get(super.getAt(index));
    }


    @SideOnly(Side.CLIENT)
    public void read(PacketBuffer buf) {
        int i = buf.readByte();

        if (this.bits != i) {
            this.setBits(i, true);
        }

        this.palette.read(buf);
        buf.readLongArray(super.getBackingLongArray());

        int regSize = MathHelper.log2DeBruijn(Block.BLOCK_STATE_IDS.size());
        if (this.palette == REGISTRY_BASED_PALETTE && this.bits != regSize)
            this.onResize(regSize, AIR_BLOCK_STATE);
    }


    public void write(PacketBuffer buf) {
        buf.writeByte(this.bits);
        this.palette.write(buf);
        buf.writeLongArray(super.getBackingLongArray());
    }


    public void write(FastByteBuf buf) {
        buf.writeByteV(this.bits);
        this.palette.write(buf);
        buf.writeLongArray(super.getBackingLongArray());
    }


    @Nullable
    public NibbleArray getDataForNBT(byte[] blockIds, NibbleArray data) {
        NibbleArray array = null;
        for (int i = 0, j, x, y, z; i < 4096; ++i) {
            x = i & 15;
            y = i >> 8 & 15;
            z = i >> 4 & 15;
            if (((j = getId(i)) >> 12 & 15) != 0) {
                if (array == null) array = new NibbleArray();
                array.set(x, y, z, j >> 12 & 15);
            }
            blockIds[i] = (byte)(j >> 4 & 255);
            data.set(x, y, z, j & 15);
        }
        return array;
    }


    public void setDataFromNBT(final byte[] blockIds, final NibbleArray data, final @Nullable NibbleArray arr) {
        final IntBlockIndex index = BlockIDIndexes.BLOCK_INDEX;
        if(arr == null) {
            for (int i = 0, x, y, z; i < 4096; ++i) {
                x = i & 15;
                y = i >> 8 & 15;
                z = i >> 4 & 15;
                this.set(i, index.get((blockIds[i] & 255) << 4 | data.get(x, y, z)));
            }
        } else {
            for (int i = 0, x, y, z; i < 4096; ++i) {
                x = i & 15;
                y = i >> 8 & 15;
                z = i >> 4 & 15;
                this.set(i, index.get(arr.get(x, y, z) << 12 | (blockIds[i] & 255) << 4 | data.get(x, y, z)));
            }
        }
    }


    public int getSerializedSize() {
        return 1 + this.palette.getSerializedSize() + PacketBuffer.getVarIntSize(super.size()) + super.getBackingLongArray().length << 3;
    }


    private int getId(int index) {
        return this.palette.get(super.getAt(index)).getId();
    }
}