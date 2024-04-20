package net.minecraft.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import java.util.Arrays;
import java.util.Map;


public final class BlockIDIndexes {
    public static final EnumFacing[] FACING_HORIZONTAL = EnumFacing.HORIZONTALS;
    public static final EnumFacing[] FACING_VERTICAL   = EnumFacing.Plane.VERTICAL.facings();

    public static final IBlockState AIR_STATE   = Blocks.AIR.getDefaultState();
    public static final int AIR_STATE_ID        = AIR_STATE.getId();
    public static final IBlockState STONE_STATE = Blocks.STONE.getDefaultState();

    //todo use this ONLY THEN SERVER STARTED
    public static final Index BLOCK_INDEX = new Index();
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


    public static class Index implements Int2ObjectFunction<IBlockState>{
        private IBlockState[] states;
        private void makeIndex(Int2ObjectOpenHashMap<IBlockState> map, int max){
            states = new IBlockState[max+1];
            Arrays.fill(states, AIR_STATE);
            for (Map.Entry<Integer,IBlockState> state:map.entrySet()){
                states[state.getKey()] = state.getValue();
            }
        }

        @Override
        public IBlockState put(int key, IBlockState value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IBlockState get(int key) {
            if(key>states.length)return AIR_STATE;
            return states[key];
        }

        @Override
        public IBlockState remove(int key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(int key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void defaultReturnValue(IBlockState rv) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IBlockState defaultReturnValue() {
            return AIR_STATE;
        }

        @Override
        public IBlockState put(Integer key, IBlockState value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IBlockState get(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IBlockState remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return states.length;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }
}








