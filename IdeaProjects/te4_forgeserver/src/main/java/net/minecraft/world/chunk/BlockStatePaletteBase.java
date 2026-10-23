package net.minecraft.world.chunk;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockIDIndexes;


public abstract class BlockStatePaletteBase implements IBlockStatePalette {
    IBlockState[] states;
    int arraySize;

    @Override
    public IBlockState get(int indexKey) {
        if(indexKey < this.arraySize)return this.states[indexKey];
        return BlockIDIndexes.AIR_STATE;
    }
}