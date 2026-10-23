package net.minecraft.world.chunk;

import io.netty.buffer.FastByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class BlockStatePaletteRegistry extends BlockStatePaletteBase{
    private static final int size = PacketBuffer.getVarIntSize(0);

    public void init(IBlockState[] stateRef){
        super.states = stateRef;
        super.arraySize = stateRef.length;
    }


    @Override
    public int idFor(IBlockState state) {
        int i = state.getId();
        return i == -1 ? 0 : i;
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
    public void write(FastByteBuf buf) {
        buf.writeVarInt(0);
    }


    @Override
    public int getSerializedSize() {
        return size;
    }
}