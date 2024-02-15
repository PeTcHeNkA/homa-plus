//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.*;

public class RandomEviction extends AbstractEviction
{
    private int evictionIndex;
    private long size;
    private Entry head;
    
    public RandomEviction(final HeapCache _heapCache, final HeapCacheListener _listener, final long _maxSize, final Weigher _weigher, final long _maxWeight) {
        super(_heapCache, _listener, _maxSize, _weigher, _maxWeight, false);
        this.evictionIndex = 0;
        this.size = 0L;
        this.head = new Entry().shortCircuit();
    }
    
    public void updateWeight(final Entry e) {
    }
    
    protected void removeFromReplacementList(final Entry e) {
        Entry.removeFromList(e);
    }
    
    protected void insertIntoReplacementList(final Entry e) {
        ++this.size;
        Entry.insertInList(this.head, e);
    }
    
    protected Entry findEvictionCandidate(final Entry _previous) {
        final Entry[] h0 = this.heapCache.hash.getEntries();
        Entry e;
        for (int idx = this.evictionIndex % h0.length; (e = h0[idx]) == null; idx = 0) {
            if (++idx >= h0.length) {}
        }
        this.evictionIndex += e.hashCode;
        if (this.evictionIndex < 0) {
            this.evictionIndex = -this.evictionIndex;
        }
        return e;
    }
    
    public void checkIntegrity(final IntegrityState _integrityState) {
    }
    
    public long removeAll() {
        long _count = 0L;
        Entry _next;
        for (Entry _head = this.head, e = this.head.prev; e != _head; e = _next) {
            _next = e.prev;
            e.removedFromList();
            ++_count;
        }
        return _count;
    }
    
    public String getExtraStatistics() {
        return "";
    }
    
    public long getHitCount() {
        return 0L;
    }
    
    public long getSize() {
        return this.size;
    }
}
