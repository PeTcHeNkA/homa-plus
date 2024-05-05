package plus.fserver.utls.collections;


public class EPooledIntVHashMap<V> {
    private static final float growFactor = 0.75F;
    private EPooledIntVHashMap.Entry<V>[] slots = new EPooledIntVHashMap.Entry[16];

    private int count, threshold = 12;


    private int getSlotIndex(int integer) {
        integer = integer ^ integer >>> 20 ^ integer >>> 12;
        return (integer ^ integer >>> 7 ^ integer >>> 4) & this.slots.length -1;
    }


    private int getSlotIndex(int hash, int size) {
        hash = hash ^ hash >>> 20 ^ hash >>> 12;
        return (hash ^ hash >>> 7 ^ hash >>> 4) & size -1;
    }


    public V lookup(int hashEntry) {
        for (EPooledIntVHashMap.Entry<V> entry = this.slots[getSlotIndex(hashEntry)]; entry != null; entry = entry.nextEntry) {
            if (entry.hashEntry == hashEntry) {
                return entry.valueEntry;
            }
        }
        return (V)null;
    }


    public boolean containsItem(int hashEntry) {
        return this.lookupEntry(hashEntry) != null;
    }


    final EPooledIntVHashMap.Entry<V> lookupEntry(int hashEntry) {
        for (EPooledIntVHashMap.Entry<V> entry = this.slots[getSlotIndex(hashEntry)]; entry != null; entry = entry.nextEntry) {
            if (entry.hashEntry == hashEntry) {
                return entry;
            }
        }
        return null;
    }


    public void addKey(int hashEntry, V valueEntry) {
        int index = getSlotIndex(hashEntry);
        for (EPooledIntVHashMap.Entry<V> entry = this.slots[index]; entry != null; entry = entry.nextEntry) {
            if (entry.hashEntry == hashEntry) {
                entry.valueEntry = valueEntry;
                return;
            }
        }
        this.insert(hashEntry, hashEntry, valueEntry, index);
    }


    private void grow(int p_76047_1_) {
        EPooledIntVHashMap.Entry<V>[] entry = this.slots;
        int i = entry.length;

        if (i == 1073741824) {
            this.threshold = Integer.MAX_VALUE;
        } else {
            EPooledIntVHashMap.Entry<V>[] entry1 = new EPooledIntVHashMap.Entry[p_76047_1_];
            this.copyTo(entry1);
            this.slots = entry1;
            this.threshold = (int)((float)p_76047_1_ * this.growFactor);
        }
    }


    private void copyTo(EPooledIntVHashMap.Entry<V>[] p_76048_1_) {
        EPooledIntVHashMap.Entry<V>[] entry = this.slots;
        int i = p_76048_1_.length;

        for (int j = 0; j < entry.length; ++j) {
            EPooledIntVHashMap.Entry<V> entry1 = entry[j];

            if (entry1 != null) {
                entry[j] = null;

                while (true) {
                    EPooledIntVHashMap.Entry<V> entry2 = entry1.nextEntry;
                    int k = getSlotIndex(entry1.slotHash, i);
                    entry1.nextEntry = p_76048_1_[k];
                    p_76048_1_[k] = entry1;
                    entry1 = entry2;

                    if (entry2 == null) {
                        break;
                    }
                }
            }
        }
    }


    public V removeObject(int o) {
        EPooledIntVHashMap.Entry<V> entry = this.removeEntry(o);
        return (V)(entry == null ? null : entry.valueEntry);
    }


    final EPooledIntVHashMap.Entry<V> removeEntry(int hash) {
        int j = getSlotIndex(hash);
        EPooledIntVHashMap.Entry<V> entry = this.slots[j];
        EPooledIntVHashMap.Entry<V> entry1;
        EPooledIntVHashMap.Entry<V> entry2;

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
        EPooledIntVHashMap.Entry<V>[] entry = this.slots;
        for (int i = 0; i < entry.length; ++i) {
            EPooledIntVHashMap.Entry<V> entry1 = entry[i];
            while (entry1 != null) {
                freeEntry(entry1);
                entry1 = entry1.nextEntry;
            }
            entry[i] = null;
        }
        this.count = 0;
    }


    private void insert(int h, int h2, V val, int index) {
        EPooledIntVHashMap.Entry<V> entry = this.slots[index];
        this.slots[index] = newEntry(h, h2, val, entry);

        if (this.count++ >= this.threshold) {
            this.grow(2 * this.slots.length);
        }
    }

    private static final Entry[] EMPTY = new Entry[0];
    private EPooledIntVHashMap.Entry<V>[] unused = EMPTY;
    private int eCount = -1;

    public V tryGetValueOnInsert() {
        return eCount == -1 ? null : this.unused[eCount].valueEntry;
    }

    private Entry<V> newEntry(int hash, int eHash, V value, EPooledIntVHashMap.Entry<V> next) {
        if(eCount == -1)return new EPooledIntVHashMap.Entry<V>(hash, eHash, value, next);
        return unused[eCount--].initF(hash, eHash, value, next);
    }


    private void freeEntry(EPooledIntVHashMap.Entry<V> entry) {
        if(unused.length == ++eCount){
            EPooledIntVHashMap.Entry[] newNodes = new EPooledIntVHashMap.Entry[unused.length==0?slots.length:unused.length<<1];
            System.arraycopy(unused, 0, newNodes, 1, unused.length);
            newNodes[0] = entry;
            unused = newNodes;
        } else unused[eCount] = entry;
    }


    static class Entry<V> {
        int hashEntry, slotHash;
        V valueEntry;
        EPooledIntVHashMap.Entry<V> nextEntry;

        Entry(int hash, int eHash, V value, EPooledIntVHashMap.Entry<V> next) {
            this.valueEntry = value;
            this.nextEntry = next;
            this.hashEntry = eHash;
            this.slotHash = hash;
        }

        Entry<V> initF(int hash, int eHash, V value, EPooledIntVHashMap.Entry<V> next){
            this.valueEntry = value;
            this.nextEntry = next;
            this.hashEntry = eHash;
            this.slotHash = hash;
            return this;
        }

        public final int getHash() {
            return this.hashEntry;
        }


        public final V getValue() {
            return this.valueEntry;
        }


        public final boolean equals(Object obj) {
            if (!(obj instanceof EPooledIntVHashMap.Entry)) {
                return false;
            } else {
                EPooledIntVHashMap.Entry<V> entry = (EPooledIntVHashMap.Entry)obj;

                if (this.hashEntry == entry.hashEntry) {
                    Object object = this.getValue();
                    Object object1 = entry.getValue();
                    if (object == object1 || object != null && object.equals(object1)) {
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