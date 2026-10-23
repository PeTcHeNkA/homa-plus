package plus.fserver.world.generator.vr;

import io.netty.util.collection.ShortObjectHashMap;
import net.minecraft.util.math.BlockPos;
import java.util.Collection;
import java.util.Map;
import java.util.Set;


public class BlockPosChunkMap<V> implements Map<BlockPos,V> {
    private final ShortObjectHashMap<V> map = new ShortObjectHashMap<>();
    private short join(int x, int y, int z){
        return (short) (x & 15 | (y & 15)<<4 | (z & 255)<<8);
    }
    private short join(BlockPos pos){
        return join(pos.getX(), pos.getY(), pos.getZ());
    }


    @Override
    public int size() {
        return map.size();
    }


    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }


    @Override
    public boolean containsKey(Object key) {
        return containsKey((BlockPos) key);
    }


    public boolean containsKey(BlockPos key) {
        return map.containsKey(join(key));
    }


    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }


    @Override
    public V get(Object key) {
        return map.get(join((BlockPos)key));
    }


    public V get(BlockPos pos){
        return map.get(join(pos));
    }


    @Override
    public V put(BlockPos key, V value) {
        return null;
    }


    @Override
    public V remove(Object key) {
        return map.remove(join((BlockPos) key));
    }


    public V remove(BlockPos pos){
        return map.remove(join(pos));
    }


    @Override
    public void putAll(Map<? extends BlockPos, ? extends V> m) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void clear() {
        map.clear();
    }


    @Override
    public Set<BlockPos> keySet() {
        throw new UnsupportedOperationException();
    }


    @Override
    public Collection<V> values() {
        return map.values();
    }


    @Override
    public Set<Entry<BlockPos, V>> entrySet() {
        throw new UnsupportedOperationException();
    }
}