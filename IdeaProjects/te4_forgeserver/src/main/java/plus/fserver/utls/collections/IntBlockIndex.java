package plus.fserver.utls.collections;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.world.chunk.BlockStateContainer;
import java.util.Arrays;
import java.util.Map;


public class IntBlockIndex implements Int2ObjectFunction<IBlockState> {
    private IBlockState[] states;

    public void makeIndex(Int2ObjectOpenHashMap<IBlockState> map, int max) {
        states = new IBlockState[max + 1];
        Arrays.fill(states, BlockIDIndexes.AIR_STATE);
        for (Map.Entry<Integer, IBlockState> state : map.entrySet()) {
            states[state.getKey()] = state.getValue();
        }
        BlockStateContainer.REGISTRY_BASED_PALETTE.init(states);
    }


    public IBlockState[] raw(){
        return states;
    }


    @Override
    public IBlockState put(int key, IBlockState value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public IBlockState get(int key) {
        IBlockState[] states;
        if (key > (states = this.states).length) return BlockIDIndexes.AIR_STATE;
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
        return BlockIDIndexes.AIR_STATE;
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
