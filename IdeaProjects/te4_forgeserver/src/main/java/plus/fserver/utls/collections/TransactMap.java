package plus.fserver.utls.collections;

import javax.annotation.Nullable;


public final class TransactMap{
    private TransactMap.Entry[] slots = new Entry[4];
    private int count;
    private int threshold = 6;

    private static int computeHash(int hash) {
        hash ^= hash >>> 20 ^ hash >>> 12;
        return hash ^ hash >>> 7 ^ hash >>> 4;
    }


    private static int getSlotIndex(int hash, int length) {
        return hash & length - 1;
    }


    public short lookup(int id) {
        int hash = computeHash(id);
        Entry[] slots;
        for(Entry entry = (slots = this.slots)[getSlotIndex(hash, slots.length)]; entry != null; entry = entry.nextEntry) {
            if (entry.key == id) {
                return entry.valueEntry;
            }
        }
        return Short.MIN_VALUE;
    }


    public boolean containsItem(int id) {
        return this.lookupEntry(id) != null;
    }


    @Nullable
    final Entry lookupEntry(int id) {
        int hash = computeHash(id);

        for(Entry entry = this.slots[getSlotIndex(hash, this.slots.length)]; entry != null; entry = entry.nextEntry) {
            if (entry.key == id) {
                return entry;
            }
        }
        return null;
    }
    

    public void addKey(int id, short value) {
        int hash = computeHash(id);
        int index = getSlotIndex(hash, this.slots.length);

        for(Entry entry = this.slots[index]; entry != null; entry = entry.nextEntry) {
            if (entry.key == id) {
                entry.valueEntry = value;
                return;
            } else if(entry.valueEntry == Short.MIN_VALUE){
                entry.key = id;
                entry.valueEntry = value;
                return;
            }
        }

        this.insert(hash, id, value, index);
    }


    private void grow(int newSize) {
        Entry[] lvt_2_1_ = this.slots;
        int lvt_3_1_ = lvt_2_1_.length;
        if (lvt_3_1_ == 1073741824) {
            this.threshold = Integer.MAX_VALUE;
        } else {
            Entry[] lvt_4_1_ = new Entry[newSize];
            this.copyTo(lvt_4_1_);
            this.slots = lvt_4_1_;
            this.threshold = (int)((float)newSize * 0.75F);
        }
    }


    private void copyTo(Entry[] entries) {
        Entry[] cur = this.slots;
        int length = entries.length;

        for(int i = 0; i < cur.length; ++i) {
            Entry entry = cur[i];
            if (entry != null) {
                cur[i] = null;

                do {
                    Entry next = entry.nextEntry;
                    int index = getSlotIndex(entry.slotHash, length);
                    entry.nextEntry = entries[index];
                    entries[index] = entry;
                    entry = next;
                } while(entry != null);
            }
        }
    }


    public short removeObject(int id) {
        int hash = computeHash(id);
        int index = getSlotIndex(hash, this.slots.length);
        Entry cur = this.slots[index];
        if(cur == null)return Short.MIN_VALUE;
        if(cur.nextEntry != null) {
            Entry entry;
            Entry next;
            for (entry = cur; entry != null; entry = next) {
                next = entry.nextEntry;
                if (entry.key == id) {
                    --this.count;
                    if (cur == entry) {
                        this.slots[index] = next;
                    } else {
                        cur.nextEntry = next;
                    }

                    return entry.valueEntry;
                }

                cur = entry;
            }
            return Short.MIN_VALUE;
        } else {
            short result = cur.valueEntry;
            cur.valueEntry = Short.MIN_VALUE;
            --this.count;
            return result;
        }
    }

    
    public void clearMap() {
        Entry[] entries = this.slots;
        for(int i = 0; i < entries.length; ++i) {
            entries[i] = null;
        }
        this.count = 0;
    }


    private void insert(int hash, int p_76040_2_, short p_76040_3_, int p_76040_4_) {
        Entry entry = this.slots[p_76040_4_];
        this.slots[p_76040_4_] = new Entry(hash, p_76040_2_, p_76040_3_, entry);
        if (this.count++ >= this.threshold) {
            this.grow(2 * this.slots.length);
        }
    }


    static class Entry {
        int key;
        short valueEntry;
        Entry nextEntry;
        final int slotHash;

        Entry(int hash, int key, short value, Entry next) {
            this.valueEntry = value;
            this.nextEntry = next;
            this.key = key;
            this.slotHash = hash;
        }


        public final int getHash() {
            return this.key;
        }


        public final short getValue() {
            return this.valueEntry;
        }


        public final boolean equals(Object obj) {
            if (!(obj instanceof Entry)) {
                return false;
            } else {
                Entry lvt_2_1_ = (Entry)obj;
                if (this.key == lvt_2_1_.key) {
                    Object lvt_3_1_ = this.getValue();
                    Object lvt_4_1_ = lvt_2_1_.getValue();
                    if (lvt_3_1_ == lvt_4_1_ || lvt_3_1_ != null && lvt_3_1_.equals(lvt_4_1_)) {
                        return true;
                    }
                }
                return false;
            }
        }


        @Override
        public final int hashCode() {
            return computeHash(this.key);
        }


        @Override
        public final String toString() {
            return this.getHash() + "=" + this.getValue();
        }
    }
}
