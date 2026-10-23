package net.minecraft.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import plus.fserver.utls.collections.IntBlockIndex;
import java.util.Map;


public final class BlockIDIndexes {
    public static final EnumFacing[] FACING_HORIZONTAL = EnumFacing.HORIZONTALS;
    public static final EnumFacing[] FACING_VERTICAL   = EnumFacing.Plane.VERTICAL.facings();

    public static final IBlockState AIR_STATE   = Blocks.AIR.getDefaultState();
    public static final int AIR_STATE_ID        = AIR_STATE.getId();
    public static final IBlockState STONE_STATE = Blocks.STONE.getDefaultState();

    //todo use this ONLY THEN SERVER STARTED
    public static final IntBlockIndex BLOCK_INDEX = new IntBlockIndex();
    public static void makeIndex(){
        Int2ObjectOpenHashMap<IBlockState> index = new Int2ObjectOpenHashMap<>();
        int max = 0;
        for (Map.Entry<IBlockState, Integer> entry : Block.BLOCK_STATE_IDS.identityMap.entrySet()) {
            entry.getKey().setId(entry.getValue());
            int cur = entry.getValue();
            if(cur>max)max = cur;
            index.put(cur, entry.getKey());
        }
        BLOCK_INDEX.makeIndex(index, max);
        //BLOCK_INDEX.trim();
    }
}