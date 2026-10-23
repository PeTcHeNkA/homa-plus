package plus.fserver.utls.collections;

import net.minecraft.pathfinding.PathPoint;


public class PointHashMap {
    private static final float growFactor = 0.75F;
    private PointHashMap.Entry[] slots = new PointHashMap.Entry[16];

    private int count, threshold = 12;


    private int getSlotIndex(int hash) {
//        int h = hash * -1640531527;
//        return (h ^ h >>> 16) & this.slots.length -1;
        hash = hash ^ hash >>> 20 ^ hash >>> 12;
        return (hash ^ hash >>> 7 ^ hash >>> 4) & this.slots.length -1;
    }


    private static int getSlotIndex(int hash, int size) {
//        int h = hash * -1640531527;
//        return (h ^ h >>> 16) & size -1;
        hash = hash ^ hash >>> 20 ^ hash >>> 12;
        return (hash ^ hash >>> 7 ^ hash >>> 4) & size -1;
    }


    public PathPoint lookup(int hash) {
        for (PointHashMap.Entry entry = this.slots[getSlotIndex(hash)]; entry != null; entry = entry.nextEntry) {
            if (entry.hashEntry == hash) return entry.valueEntry;
        }
        return null;
    }


    public boolean containsItem(int hashEntry) {
        return this.lookupEntry(hashEntry) != null;
    }


    final PointHashMap.Entry lookupEntry(int hashEntry) {
        for (PointHashMap.Entry entry = this.slots[getSlotIndex(hashEntry)]; entry != null; entry = entry.nextEntry) {
            if (entry.hashEntry == hashEntry) {
                return entry;
            }
        }
        return null;
    }


    public void addKey(int hashEntry, PathPoint valueEntry) {
        int index = getSlotIndex(hashEntry);
        for (PointHashMap.Entry entry = this.slots[index]; entry != null; entry = entry.nextEntry) {
            if (entry.hashEntry == hashEntry) {
                entry.valueEntry = valueEntry;
                return;
            }
        }
        this.insert(hashEntry, hashEntry, valueEntry, index);
    }


    private void grow(int newSize) {
        PointHashMap.Entry[] entry = this.slots;
        int i = entry.length;

        if (i == 1073741824) {
            this.threshold = Integer.MAX_VALUE;
        } else {
            PointHashMap.Entry[] entry1 = new PointHashMap.Entry[newSize];
            this.copyTo(entry1);
            this.slots = entry1;
            this.threshold = (int)((float)newSize * this.growFactor);
        }
    }


    private void copyTo(PointHashMap.Entry[] src) {
        PointHashMap.Entry[] entry = this.slots;
        int i = src.length;

        for (int j = 0; j < entry.length; ++j) {
            PointHashMap.Entry entry1 = entry[j];

            if (entry1 != null) {
                entry[j] = null;

                while (true) {
                    PointHashMap.Entry entry2 = entry1.nextEntry;
                    int k = getSlotIndex(entry1.slotHash, i);
                    entry1.nextEntry = src[k];
                    src[k] = entry1;
                    entry1 = entry2;

                    if (entry2 == null) {
                        break;
                    }
                }
            }
        }
    }


    public PathPoint removeObject(int o) {
        PointHashMap.Entry entry = this.removeEntry(o);
        return (entry == null ? null : entry.valueEntry);
    }


    final PointHashMap.Entry removeEntry(int hash) {
        int j = getSlotIndex(hash);
        PointHashMap.Entry entry = this.slots[j];
        PointHashMap.Entry entry1;
        PointHashMap.Entry entry2;

        for (entry1 = entry; entry1 != null; entry1 = entry2) {
            entry2 = entry1.nextEntry;

            if (entry1.hashEntry == hash) {
                --this.count;

                if (entry == entry1) {
                    this.slots[j] = entry2;
                } else {
                    entry.nextEntry = entry2;
                }
                return entry1;
            }
            entry = entry1;
        }
        return entry1;
    }


    public void clearMap() {
        final PointHashMap.Entry[] entry = this.slots;
        PointHashMap.Entry[] unused = this.unused;
        int eCount = this.eCount, ul = unused.length;
        for (int i = 0, s = entry.length; i < s; ++i) {
            PointHashMap.Entry entry1 = entry[i];
            while (entry1 != null) {
                if(ul == ++eCount){
                    PointHashMap.Entry[] newNodes = new PointHashMap.Entry[ul==0?slots.length:ul<<1];
                    System.arraycopy(unused, 0, newNodes, 1, ul);
                    ul = newNodes.length;
                    newNodes[0] = entry1;
                    unused = this.unused = newNodes;
                } else {
                    unused[eCount] = entry1;
                }
                entry1 = entry1.nextEntry;
            }
            entry[i] = null;
        }
        this.eCount = eCount;
        this.count = 0;
    }


    private void freeEntry(PointHashMap.Entry entry) {
        if(unused.length == ++eCount){
            PointHashMap.Entry[] newNodes = new PointHashMap.Entry[unused.length==0?slots.length:unused.length<<1];
            System.arraycopy(unused, 0, newNodes, 1, unused.length);
            newNodes[0] = entry;
            unused = newNodes;
        } else unused[eCount] = entry;
    }


    private void insert(int h, int h2, PathPoint val, int index) {
        PointHashMap.Entry entry = this.slots[index];
        this.slots[index] = newEntry(h, h2, val, entry);

        if (this.count++ >= this.threshold) {
            this.grow(2 * this.slots.length);
        }
    }

    private static final Entry[] EMPTY = new Entry[0];
    private PointHashMap.Entry[] unused = EMPTY;
    private int eCount = -1;

    public PathPoint tryGetValueOnInsert() {
        return eCount == -1 ? null : this.unused[eCount].valueEntry;
    }

    private Entry newEntry(int hash, int eHash, PathPoint value, PointHashMap.Entry next) {
        if(eCount == -1)return new PointHashMap.Entry(hash, eHash, value, next);
        return unused[eCount--].initF(hash, eHash, value, next);
    }


    static class Entry {
        int hashEntry, slotHash;
        PathPoint valueEntry;
        PointHashMap.Entry nextEntry;

        Entry(int hash, int eHash, PathPoint value, PointHashMap.Entry next) {
            this.valueEntry = value;
            this.nextEntry = next;
            this.hashEntry = eHash;
            this.slotHash = hash;
        }

        Entry initF(int hash, int eHash, PathPoint value, PointHashMap.Entry next){
            this.valueEntry = value;
            this.nextEntry = next;
            this.hashEntry = eHash;
            this.slotHash = hash;
            return this;
        }

        public final int getHash() {
            return this.hashEntry;
        }


        public final PathPoint getValue() {
            return this.valueEntry;
        }


        public final boolean equals(Object obj) {
            if (!(obj instanceof PointHashMap.Entry)) {
                return false;
            } else {
                PointHashMap.Entry entry = (PointHashMap.Entry)obj;

                if (this.hashEntry == entry.hashEntry) {
                    PathPoint point1 = this.getValue();
                    PathPoint point2 = entry.getValue();
                    if (point1 == point2 || point1 != null && point1.equals(point2)) {
                        return true;
                    }
                }
                return false;
            }
        }


        public final int hashCode() {
            return hashEntry;
        }


        public final String toString() {
            return this.getHash() + "=" + this.getValue();
        }
    }
}