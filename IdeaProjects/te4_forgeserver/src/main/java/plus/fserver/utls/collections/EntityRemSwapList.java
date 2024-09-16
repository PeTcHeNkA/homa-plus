package plus.fserver.utls.collections;

import net.minecraft.entity.Entity;
import java.util.Arrays;
import java.util.Collection;


public class EntityRemSwapList<T extends Entity> extends EntityList<T> {
    public EntityRemSwapList(){}

    public EntityRemSwapList(Entity[] data, int len){
        this.data = data;
        this.size = len;
    }


    public EntityRemSwapList(Entity... data){
        this.data = data;
        this.size = data.length;
    }


    public static class E15<T extends Entity> extends EntityRemSwapList<T>{
        @Override
        public Entity[] newArrayAuto() {
            return Arrays.copyOf(data, data.length + 15);
        }
    }


    public static class E1V<T extends Entity> extends EntityRemSwapList<T> {
        public E1V(T v) {
            this.data = new Entity[]{v};
            this.size = 1;
        }

        @Override
        public Entity[] newArrayAuto() {
            return Arrays.copyOf(data, data.length == 1 ? 5 : data.length + 7);
        }
    }


    @Override
    public T remove(int index) {
        if(size == index+1){
            T prev = (T) data[--size];
            data[size] = null;
            return prev;
        }
        T prev = (T) data[index];
        Entity swap = data[size-1];
        data[index] = swap;
        data[size-1] = null;
        --size;
        return prev;
    }


    public void removeSwap(int index) {
        if(size == index+1){
            data[--size] = null;
            return;
        }
        Entity swap = data[size-1];
        data[index] = swap;
        data[size-1] = null;
        --size;
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        Object[] data = this.data;
        int size, prevSize = size = this.size;
        Object swap;
        for (int i = 0; i < size; i++){
            if(c.contains(data[i])){
                if (i != --size) {
                    swap = data[size];
                    data[i--] = swap;
                }
                data[size] = null;
            }
        }
        this.size = size;
        return size != prevSize;
    }


    @Override
    public boolean contains(Object o) {
        for (int i = 0, s = size; i < s; i++) if(o.equals(data[i]))return true;
        return false;
    }


    @Override
    public boolean remove(Object o) {
        for (int index = 0, s = size; index < s; index++)
            if (o.equals(data[index])) {
                removeSwap(index);
                return true;
            }
        return false;
    }


    public boolean removeRefSwap(Object o) {
        for (int index = 0, s = size; index < s; index++)
            if (o == data[index]) {
                removeSwap(index);
                return true;
            }
        return false;
    }


    @Override
    public boolean add(T obj) {
        if(obj == null){return false;}
        return super.add(obj);
    }


    public boolean removeNotNulDef(Object o) {
        for (int index = 0; index < size; index++)
            if (o.equals(data[index])) {
                int numMoved = size - index - 1;
                if (numMoved > 0) System.arraycopy(data, index + 1, data, index, numMoved);
                data[--size] = null;
                return true;
            }
        return false;
    }
}
