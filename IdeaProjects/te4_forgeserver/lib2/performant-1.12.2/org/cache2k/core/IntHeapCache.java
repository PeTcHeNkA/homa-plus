//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.*;

public class IntHeapCache<V> extends HeapCache<Integer, V> implements IntCache<V>
{
    public Integer extractIntKeyObj(final Integer key) {
        return null;
    }
    
    public int extractIntKeyValue(final Integer key, final int hc) {
        return key;
    }
    
    public int extractModifiedHash(final Entry e) {
        return modifiedHash(e.hashCode);
    }
    
    public Integer extractKeyObj(final Entry<Integer, V> e) {
        return e.hashCode;
    }
    
    public Hash2<Integer, V> createHashTable() {
        return new Hash2<Integer, V>(this) {
            protected int modifiedHashCode(final int hc) {
                final IntHeapCache this$0 = IntHeapCache.this;
                return IntHeapCache.modifiedHash(hc);
            }
            
            protected boolean keyObjIsEqual(final Integer key, final Entry e) {
                return true;
            }
        };
    }
    
    public V peek(final int key) {
        final Entry<Integer, V> e = this.peekEntryInternal((Integer) null, modifiedHash(key), key);
        if (e != null) {
            return (V)this.returnValue((Entry)e);
        }
        return null;
    }
    
    public boolean containsKey(final int key) {
        final Entry e = this.lookupEntry((Integer) null, modifiedHash(key), key);
        if (e != null) {
            this.metrics.heapHitButNoRead();
            return e.hasFreshData(this.clock);
        }
        return false;
    }
    
    public void put(final int key, final V value) {
        while (true) {
            final Entry e = this.lookupOrNewEntry((Integer) null, modifiedHash(key), key);
            synchronized (e) {
                e.waitForProcessing();
                if (e.isGone()) {
                    this.metrics.goneSpin();
                    continue;
                }
                if (!e.isVirgin()) {
                    this.metrics.heapHitButNoRead();
                }
                this.putValue(e, (V) value);
            }
            break;
        }
    }
    
    public V get(final int key) {
        final Entry<Integer, V> e = this.getEntryInternal((Integer) null, modifiedHash(key), key);
        if (e == null) {
            return null;
        }
        return (V)this.returnValue((Entry)e);
    }
    
    public void remove(final int key) {
        super.remove((Integer) key);
    }
}
