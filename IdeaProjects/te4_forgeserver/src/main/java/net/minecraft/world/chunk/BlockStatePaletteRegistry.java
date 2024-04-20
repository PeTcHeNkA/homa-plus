package net.minecraft.world.chunk;

import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockIDIndexes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class BlockStatePaletteRegistry implements IBlockStatePalette {
    private static final int size = PacketBuffer.getVarIntSize(0);
    @Override
    public int idFor(IBlockState state) {
        int i = state.getId();
        return i == -1 ? 0 : i;
    }


    @Override //not null
    public IBlockState get(int indexKey) {
        return BlockIDIndexes.BLOCK_INDEX.get(indexKey);
    }


    @SideOnly(Side.CLIENT)
    public void read(PacketBuffer buf) {
        buf.readVarInt();
    }


    @Override
    public void write(PacketBuffer buf) {
        buf.writeVarInt(0);
    }


    @Override
    public int getSerializedSize() {
        return size;
    }
}