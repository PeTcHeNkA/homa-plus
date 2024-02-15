//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import java.util.concurrent.atomic.*;
import org.cache2k.*;
import org.cache2k.core.concurrency.*;

public class Hash2<K, V>
{
    private static final int LOCK_SEGMENTS;
    private static final int LOCK_MASK;
    private volatile int clearOrCloseCount;
    private long segmentMaxFill;
    private Entry<K, V>[] entries;
    private final OptimisticLock[] locks;
    private final AtomicLong[] segmentSize;
    private final Cache cache;
    
    public Hash2(final Cache _cache) {
        this.clearOrCloseCount = 0;
        this.locks = new OptimisticLock[Hash2.LOCK_SEGMENTS];
        for (int i = 0; i < Hash2.LOCK_SEGMENTS; ++i) {
            this.locks[i] = Locks.newOptimistic();
        }
        this.segmentSize = new AtomicLong[Hash2.LOCK_SEGMENTS];
        for (int i = 0; i < Hash2.LOCK_SEGMENTS; ++i) {
            this.segmentSize[i] = new AtomicLong();
        }
        this.initArray();
        this.cache = _cache;
    }
    
    private void initArray() {
        final int len = Math.max(HeapCache.TUNABLE.initialHashSize, Hash2.LOCK_SEGMENTS * 4);
        this.entries = (org.cache2k.core.Entry<K, V>[])new Entry[len];
        this.calcMaxFill();
    }
    
    public long getEntryCapacity() {
        return this.entries.length * 1L * HeapCache.TUNABLE.hashLoadPercent / 100L;
    }
    
    public long getSegmentMaxFill() {
        return this.segmentMaxFill;
    }
    
    private void calcMaxFill() {
        this.segmentMaxFill = this.getEntryCapacity() / Hash2.LOCK_SEGMENTS;
    }
    
    public Entry<K, V> lookup(final K key, final int _hash, final int _keyValue) {
        final OptimisticLock[] _locks = this.locks;
        final int si = _hash & Hash2.LOCK_MASK;
        final OptimisticLock l = _locks[si];
        long _stamp = l.tryOptimisticRead();
        Entry<K, V>[] tab = this.entries;
        if (tab == null) {
            throw new CacheClosedException(this.cache);
        }
        int n = tab.length;
        int _mask = n - 1;
        int idx = _hash & _mask;
        for (Entry<K, V> e = tab[idx]; e != null; e = (Entry<K, V>)e.another) {
            if (e.hashCode == _keyValue && this.keyObjIsEqual(key, e)) {
                return e;
            }
        }
        if (l.validate(_stamp)) {
            return null;
        }
        _stamp = l.readLock();
        try {
            tab = this.entries;
            if (tab == null) {
                throw new CacheClosedException(this.cache);
            }
            n = tab.length;
            _mask = n - 1;
            idx = (_hash & _mask);
            for (Entry<K, V> e = tab[idx]; e != null; e = (Entry<K, V>)e.another) {
                if (e.hashCode == _keyValue && this.keyObjIsEqual(key, e)) {
                    return e;
                }
            }
            return null;
        }
        finally {
            l.unlockRead(_stamp);
        }
    }
    
    protected boolean keyObjIsEqual(final K key, final Entry e) {
        final Object ek;
        return (ek = e.getKeyObj()) == key || ek.equals(key);
    }
    
    public Entry<K, V> insertWithinLock(final Entry<K, V> e, final int _hash, final int _keyValue) {
        final K key = (K)e.getKeyObj();
        final int si = _hash & Hash2.LOCK_MASK;
        final Entry<K, V>[] tab = this.entries;
        if (tab == null) {
            throw new CacheClosedException(this.cache);
        }
        final int n = tab.length;
        final int _mask = n - 1;
        final int idx = _hash & _mask;
        for (Entry<K, V> f = tab[idx]; f != null; f = (Entry<K, V>)f.another) {
            final Object ek;
            if (f.hashCode == _keyValue && ((ek = f.getKeyObj()) == key || ek.equals(key))) {
                return f;
            }
        }
        e.another = tab[idx];
        tab[idx] = e;
        this.segmentSize[si].incrementAndGet();
        return e;
    }
    
    public void checkExpand(final int _hash) {
        final int si = _hash & Hash2.LOCK_MASK;
        final long _size = this.segmentSize[si].get();
        if (_size > this.segmentMaxFill) {
            this.eventuallyExpand(si);
        }
    }
    
    public OptimisticLock getSegmentLock(final int _hash) {
        return this.locks[_hash & Hash2.LOCK_MASK];
    }
    
    public boolean remove(final Entry<K, V> e) {
        final int _hash = this.modifiedHashCode(e.hashCode);
        final OptimisticLock[] _locks = this.locks;
        final int si = _hash & Hash2.LOCK_MASK;
        final OptimisticLock l = _locks[si];
        final long _stamp = l.writeLock();
        try {
            final Entry<K, V>[] tab = this.entries;
            if (tab == null) {
                throw new CacheClosedException(this.cache);
            }
            final int n = tab.length;
            final int _mask = n - 1;
            final int idx = _hash & _mask;
            Entry<K, V> f = tab[idx];
            if (f == e) {
                tab[idx] = (Entry<K, V>)f.another;
                this.segmentSize[si].decrementAndGet();
                return true;
            }
            while (f != null) {
                final Entry<K, V> _another = (Entry<K, V>)f.another;
                if (_another == e) {
                    f.another = _another.another;
                    this.segmentSize[si].decrementAndGet();
                    return true;
                }
                f = _another;
            }
        }
        finally {
            l.unlockWrite(_stamp);
        }
        return false;
    }
    
    public boolean removeWithinLock(final Entry<K, V> e, final int _hash) {
        final int si = _hash & Hash2.LOCK_MASK;
        final Entry<K, V>[] tab = this.entries;
        if (tab == null) {
            throw new CacheClosedException(this.cache);
        }
        final int n = tab.length;
        final int _mask = n - 1;
        final int idx = _hash & _mask;
        Entry<K, V> f = tab[idx];
        if (f == e) {
            tab[idx] = (Entry<K, V>)f.another;
            this.segmentSize[si].decrementAndGet();
            return true;
        }
        while (f != null) {
            final Entry<K, V> _another = (Entry<K, V>)f.another;
            if (_another == e) {
                f.another = _another.another;
                this.segmentSize[si].decrementAndGet();
                return true;
            }
            f = _another;
        }
        return false;
    }
    
    private void eventuallyExpand(final int _segmentIndex) {
        final long[] _stamps = this.lockAll();
        try {
            final long _size = this.segmentSize[_segmentIndex].get();
            if (_size <= this.segmentMaxFill) {
                return;
            }
            this.rehash();
        }
        finally {
            this.unlockAll(_stamps);
        }
    }
    
    private long[] lockAll() {
        final OptimisticLock[] _locks = this.locks;
        final int sn = _locks.length;
        final long[] _stamps = new long[this.locks.length];
        for (int i = 0; i < sn; ++i) {
            final OptimisticLock l = _locks[i];
            _stamps[i] = l.writeLock();
        }
        return _stamps;
    }
    
    private void unlockAll(final long[] _stamps) {
        final OptimisticLock[] _locks = this.locks;
        for (int sn = _locks.length, i = 0; i < sn; ++i) {
            _locks[i].unlockWrite(_stamps[i]);
        }
    }
    
    protected int modifiedHashCode(final int hc) {
        return hc;
    }
    
    void rehash() {
        final Entry<K, V>[] src = this.entries;
        if (src == null) {
            throw new CacheClosedException(this.cache);
        }
        final int sl = src.length;
        final int n = sl * 2;
        final int _mask = n - 1;
        final Entry<K, V>[] tab = (Entry<K, V>[])new Entry[n];
        long _count = 0L;
        for (Entry e : src) {
            while (e != null) {
                ++_count;
                final Entry _next = e.another;
                final int idx = this.modifiedHashCode(e.hashCode) & _mask;
                e.another = tab[idx];
                tab[idx] = (Entry<K, V>)e;
                e = _next;
            }
        }
        this.entries = tab;
        this.calcMaxFill();
    }
    
    public long getSize() {
        long sum = 0L;
        for (final AtomicLong al : this.segmentSize) {
            sum += al.get();
        }
        return sum;
    }
    
    public <T> T runTotalLocked(final Job<T> j) {
        final long[] _stamps = this.lockAll();
        try {
            return (T)j.call();
        }
        finally {
            this.unlockAll(_stamps);
        }
    }
    
    public void clearWhenLocked() {
        for (final AtomicLong aSegmentSize : this.segmentSize) {
            aSegmentSize.set(0L);
        }
        ++this.clearOrCloseCount;
        this.initArray();
    }
    
    public int getClearOrCloseCount() {
        return this.clearOrCloseCount;
    }
    
    public void close() {
        ++this.clearOrCloseCount;
        this.entries = null;
    }
    
    public void calcHashCollisionInfo(final CollisionInfo inf) {
        for (Entry<K, V> e : this.entries) {
            if (e != null) {
                e = (Entry<K, V>)e.another;
                if (e != null) {
                    ++inf.collisionSlotCnt;
                    int _size;
                    for (_size = 1; e != null; e = (Entry<K, V>)e.another, ++_size) {
                        ++inf.collisionCnt;
                    }
                    if (inf.longestCollisionSize < _size) {
                        inf.longestCollisionSize = _size;
                    }
                }
            }
        }
    }
    
    public long calcEntryCount() {
        long _count = 0L;
        for (Entry e : this.entries) {
            while (e != null) {
                ++_count;
                e = e.another;
            }
        }
        return _count;
    }
    
    public Entry<K, V>[] getEntries() {
        return this.entries;
    }
    
    static {
        final int _ncpu = Runtime.getRuntime().availableProcessors();
        LOCK_SEGMENTS = 2 << 31 - Integer.numberOfLeadingZeros(_ncpu);
        LOCK_MASK = Hash2.LOCK_SEGMENTS - 1;
    }
}
