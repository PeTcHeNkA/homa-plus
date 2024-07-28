package net.minecraft.world.chunk;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public interface IBlockStatePalette {
    int idFor(IBlockState state);

    /**
     * renamed, old name getBlockState
     * cause: Performance, boost BlockStatePaletteHashMap
     */
//    @Nullable
    IBlockState get(int indexKey);


    @SideOnly(Side.CLIENT)
    void read(PacketBuffer buf);


    void write(PacketBuffer buf);


    void write(ByteBuf buf);


    int getSerializedSize();
}
