package net.minecraft.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.ElcaConfig;
import plus.fserver.utls.collections.BitArrayMut;


public class BlockStateContainer extends BitArrayMut implements IBlockStatePaletteResizer {
    public static final BlockStatePaletteRegistry REGISTRY_BASED_PALETTE = new BlockStatePaletteRegistry();
    protected BlockStatePaletteBase blPlt;
    protected int bit;

    public BlockStateContainer() {
        this.setBits(ElcaConfig.INIT_PALETTE_LINEAR_BITS, false);
    }


    private BitArrayMut setBits(int bitsIn, boolean forceBits) {
        if (bitsIn != this.bit) {
            this.bit = bitsIn;

            if (this.bit <= 2) {
                this.blPlt = new BlockStatePaletteLinear(this.bit);
            } else if (this.bit <= 8) {
                this.blPlt = new BlockStatePaletteHashMap(this.bit);
            } else {
                this.blPlt = REGISTRY_BASED_PALETTE;
                this.bit = MathHelper.log2DeBruijn(BlockIDIndexes.BLOCK_INDEX.size());
                if (forceBits) this.bit = bitsIn;
            }
            if(this.blPlt.idFor(BlockIDIndexes.AIR_STATE) == -1){
                onResize(this.bit + 1, BlockIDIndexes.AIR_STATE);
            }
            BitArrayMut result = super.copy();
            super.init(this.bit, 4096);
            return result;
        }
        return this;
    }


    @Override
    public int onResize(int bits, IBlockState state) {
        IBlockStatePalette palette = this.blPlt;
        BitArrayMut bitarray = this.setBits(bits, false);

        for (int i = 0, size = bitarray.size(); i < size; ++i) {
            this.setC(i, palette.get(bitarray.getAt(i)));
        }
        return this.blPlt.idFor(state);
    }


    public final void setC(int x, int y, int z, IBlockState state) {
        this.setC(y << 8 | z << 4 | x, state);
    }


    protected void setC(int index, IBlockState state) {
        int i = this.blPlt.idFor(state);
        if(i == -1)i = onResize(bit +1, state);
        super.setAt(index, i);
    }


    public IBlockState getC(int x, int y, int z) {
        return this.blPlt.get(super.getAt(y << 8 | z << 4 | x));
    }


    public IBlockState update(int x, int y, int z, final IBlockState state){
        IBlockState result = this.blPlt.get(super.getAt(x = (y << 8 | z << 4 | x)));
        y = blPlt.idFor(state);
        if(y == -1)y = onResize(bit +1, state);
        super.setAt(x, y);
        return result;
    }


    protected IBlockState get(int index) {
        return this.blPlt.get(super.getAt(index));
    }


    @SideOnly(Side.CLIENT)
    public void read(PacketBuffer buf) {
        int i = buf.readByte();

        if (this.bit != i) {
            this.setBits(i, true);
        }

        this.blPlt.read(buf);
        buf.readLongArray(super.getBackingLongArray());

        int regSize = MathHelper.log2DeBruijn(Block.BLOCK_STATE_IDS.size());
        if (this.blPlt == REGISTRY_BASED_PALETTE && this.bit != regSize)
            this.onResize(regSize, BlockIDIndexes.AIR_STATE);
    }
}