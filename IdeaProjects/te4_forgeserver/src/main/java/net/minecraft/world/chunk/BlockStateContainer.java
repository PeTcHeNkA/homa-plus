package net.minecraft.world.chunk;

import javax.annotation.Nullable;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BitArray;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.utls.NetUtils;


public class BlockStateContainer implements IBlockStatePaletteResizer {
    private static final IBlockStatePalette REGISTRY_BASED_PALETTE = new BlockStatePaletteRegistry();
    protected static final IBlockState AIR_BLOCK_STATE = Blocks.AIR.getDefaultState();
    protected BitArray storage;
    protected IBlockStatePalette palette;
    private int bits;

    public BlockStateContainer() {
        this.setBits(4, false);
    }


//    private int getIndex(int x, int y, int z) {
//        return y << 8 | z << 4 | x;
//    }


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
            this.storage = new BitArray(this.bits, 4096);
        }
    }


    @Override
    public int onResize(int bits, IBlockState state) {
        BitArray bitarray = this.storage;
        IBlockStatePalette state1 = this.palette;
        this.setBits(bits);
        for (int i = 0; i < bitarray.size(); ++i) {
            IBlockState iblockstate = state1.get(bitarray.getAt(i));
            if (iblockstate != null) {
                this.set(i, iblockstate);
            }
        }
        return this.palette.idFor(state);
    }


    public void set(int x, int y, int z, IBlockState state) {
        this.set(y << 8 | z << 4 | x, state);
    }


    protected void set(int index, IBlockState state) {
        int i = this.palette.idFor(state);
        this.storage.setAt(index, i);
    }


    public IBlockState get(int x, int y, int z) {
        return this.palette.get(this.storage.getAt(y << 8 | z << 4 | x));
    }


    public IBlockState update(int x, int y, int z, IBlockState state){
        IBlockState result = this.palette.get(this.storage.getAt(x = (y << 8 | z << 4 | x)));
        y = palette.idFor(state);
        storage.setAt(x, y);
        return result;
    }


    protected IBlockState get(int index) {
        return this.palette.get(this.storage.getAt(index));
    }


    @SideOnly(Side.CLIENT)
    public void read(PacketBuffer buf) {
        int i = buf.readByte();

        if (this.bits != i) {
            this.setBits(i, true);
        }

        this.palette.read(buf);
        buf.readLongArray(this.storage.getBackingLongArray());

        int regSize = MathHelper.log2DeBruijn(Block.BLOCK_STATE_IDS.size());
        if (this.palette == REGISTRY_BASED_PALETTE && this.bits != regSize)
            this.onResize(regSize, AIR_BLOCK_STATE);
    }


    public void write(PacketBuffer buf) {
        buf.writeByte(this.bits);
        this.palette.write(buf);
        buf.writeLongArray(this.storage.getBackingLongArray());
    }


    public void write(ByteBuf buf) {
        buf.writeByte(this.bits);
        this.palette.write(buf);
        NetUtils.writeLongArray(buf, this.storage.getBackingLongArray());
    }


    @Nullable
    public NibbleArray getDataForNBT(byte[] blockIds, NibbleArray data) {
        NibbleArray nibblearray = null;
        for (int i = 0, j, k, l, i1; i < 4096; ++i) {
            k = i & 15;
            l = i >> 8 & 15;
            i1 = i >> 4 & 15;
            if (((j = getId(i)) >> 12 & 15) != 0) {
                if (nibblearray == null) nibblearray = new NibbleArray();
                nibblearray.set(k, l, i1, j >> 12 & 15);
            }
            blockIds[i] = (byte)(j >> 4 & 255);
            data.set(k, l, i1, j & 15);
        }
        return nibblearray;
    }


    public void setDataFromNBT(byte[] blockIds, NibbleArray data, @Nullable NibbleArray blockIdExtension) {
        for (int i = 0, j, k, l; i < 4096; ++i) {
            j = i & 15;
            k = i >> 8 & 15;
            l = i >> 4 & 15;
            this.set(i, BlockIDIndexes.BLOCK_INDEX.get(
                    (blockIdExtension == null ? 0 : blockIdExtension.get(j, k, l) << 12) |
                            (blockIds[i] & 255) << 4 | data.get(j, k, l)));
        }
    }


    public int getSerializedSize() {
        return 1 + this.palette.getSerializedSize() + PacketBuffer.getVarIntSize(this.storage.size()) + this.storage.getBackingLongArray().length << 3;
    }


    private int getId(int index) {
        return this.palette.get(storage.getAt(index)).getId();
    }
}