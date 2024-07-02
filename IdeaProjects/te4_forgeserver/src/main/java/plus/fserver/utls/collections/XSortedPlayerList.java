package plus.fserver.utls.collections;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;


@Deprecated
public class XSortedPlayerList {
    private static Comparator<Entry> COMPARATOR = (o1, o2) -> {
        long l1 = o1.index, l2 = o2.index;
        return l1 < l2 ? -1 : l1 == l2 ? 0 : 1;
    };
    private Entry[] entries = new Entry[8];
    private int count = 0;

    private static class Entry{
        private EntityPlayer player;
        private long index;
    }

    private long indexOf(EntityPlayer player){
        return indexOf((int) player.posX, (int) player.posZ);
    }

    private long indexOf(int x, int z){
        return (((long) x) << 32) | z;
    }


    public void update() {
        Entry[] entries = this.entries;
        for (int i = 0; i < count; ++i) {
            entries[i].index = indexOf(entries[i].player);
        }
        Arrays.sort(entries, 0, count, COMPARATOR);
    }


    public void add(EntityPlayer player) {
        if (count == entries.length) {
            entries = Arrays.copyOf(entries, entries.length * 2);
        }
        Entry entry = entries[count] = new Entry();
        entry.player = player;
        entry.index = indexOf(player);
        ++count;
    }


    public void remove(EntityPlayer player) {
        int i = binarySearchUnsafe(0, count, indexOf(player));
        if (i >= 0 && entries[i].player == player) {
            --count;
            if (i < count) {
                System.arraycopy(entries, i + 1, entries, i, count - i);
            }
            entries[count] = null;
        }
    }


    private int binarySearchUnsafe(int fromIndex, int toIndex,
                                   long key) {
        Entry[] a = entries;
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midVal = a[mid].index;

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid;
        }
        return low;
    }


    public Iterator<EntityPlayer> getNear(Entity entity, int range){
        range += 1;
        return get(
                (int) (entity.posX - range),
                (int) (entity.posZ - range),
                (int) (entity.posX + range),
                (int) (entity.posZ + range)
        );
    }


    public Iterator<EntityPlayer> get(int xFrom, int zFrom, int xTo, int zTo){
        int fromIndex = binarySearchUnsafe(0, count, indexOf(xFrom, zFrom));
        int toIndex   = binarySearchUnsafe(fromIndex, count, indexOf(xTo, zTo));
        return new Itr(entries, fromIndex, toIndex);
    }


    private static class Itr implements Iterator<EntityPlayer> {
        private final Entry[] entries;
        final int fromIndex, toIndex;
        private int cursor;

        private Itr(Entry[] entries, int fromIndex, int toIndex) {
            this.entries = entries;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;

            cursor = fromIndex;
        }

        @Override
        public boolean hasNext() {
            return cursor < toIndex;
        }


        @Override
        public EntityPlayer next() {
            return entries[cursor++].player;
        }
    }
}