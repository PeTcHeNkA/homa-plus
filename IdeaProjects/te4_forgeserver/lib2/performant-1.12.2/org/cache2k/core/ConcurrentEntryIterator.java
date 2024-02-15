//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import java.util.*;
import org.cache2k.*;
import org.cache2k.core.concurrency.*;

public class ConcurrentEntryIterator<K, V> implements Iterator<Entry<K, V>>
{
    private HeapCache<K, V> cache;
    private Entry<K, V> lastEntry;
    private Entry<K, V> nextEntry;
    private long clearCount;
    private Hash2<K, V> hash;
    private Entry<K, V>[] hashArray;
    private HashMap<K, K> seen;
    
    public ConcurrentEntryIterator(final HeapCache<K, V> _cache) {
        this.lastEntry = null;
        this.nextEntry = null;
        this.seen = new HashMap<K, K>();
        this.cache = _cache;
        this.hash = this.cache.hash;
        this.switchAndCheckAbort();
    }
    
    @Override
    public boolean hasNext() {
        final Entry<K, V> nextEntry = this.nextEntry();
        this.nextEntry = nextEntry;
        return nextEntry != null;
    }
    
    @Override
    public Entry<K, V> next() {
        if (this.nextEntry != null) {
            final Entry<K, V> e = this.nextEntry;
            this.nextEntry = null;
            return e;
        }
        final Entry<K, V> e = this.nextEntry();
        if (e == null) {
            throw new NoSuchElementException("not available");
        }
        return e;
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    public boolean hasBeenIterated(final K key, final int _hashCode) {
        return this.seen.containsKey(key);
    }
    
    public void markIterated(final K key, final int _hashCode) {
        this.seen.put(key, key);
    }
    
    private Entry<K, V> nextEntry() {
        if (this.hashArray == null) {
            return null;
        }
        if (this.needsAbort()) {
            if (this.hash != null && this.cache.isClosed()) {
                this.clearOutReferences();
                throw new CacheClosedException((Cache)this.cache);
            }
            this.clearOutReferences();
            return null;
        }
        else {
            int idx = 0;
            if (this.lastEntry != null) {
                Entry<K, V> e = (Entry<K, V>)this.lastEntry.another;
                if (e != null) {
                    e = this.checkIteratedOrNext(e);
                    if (e != null) {
                        return this.lastEntry = e;
                    }
                }
                idx = (this.cache.extractModifiedHash(this.lastEntry) & this.hashArray.length - 1) + 1;
            }
            while (true) {
                if (idx >= this.hashArray.length) {
                    if (this.switchAndCheckAbort()) {
                        return null;
                    }
                    idx = 0;
                }
                Entry<K, V> e = this.hashArray[idx];
                if (e != null) {
                    e = this.checkIteratedOrNext(e);
                    if (e != null) {
                        return this.lastEntry = e;
                    }
                }
                ++idx;
            }
        }
    }
    
    private boolean needsAbort() {
        return this.clearCount != this.hash.getClearOrCloseCount();
    }
    
    private Entry<K, V> checkIteratedOrNext(Entry<K, V> e) {
        do {
            final K _key = this.cache.extractKeyObj(e);
            final boolean _notYetIterated = !this.seen.containsKey(_key);
            if (_notYetIterated) {
                this.markIterated(_key, this.cache.extractModifiedHash(e));
                return e;
            }
            e = (Entry<K, V>)e.another;
        } while (e != null);
        return null;
    }
    
    private boolean switchAndCheckAbort() {
        if (Thread.holdsLock(this.cache.lock)) {
            return this.switchCheckAndAbortLocked();
        }
        return this.cache.executeWithGlobalLock((org.cache2k.core.concurrency.Job<Boolean>)new Job<Boolean>() {
            public Boolean call() {
                return ConcurrentEntryIterator.this.switchCheckAndAbortLocked();
            }
        });
    }
    
    private Boolean switchCheckAndAbortLocked() {
        if (!this.hasExpansionOccurred()) {
            this.clearOutReferences();
            return true;
        }
        this.hashArray = this.hash.getEntries();
        this.clearCount = this.hash.getClearOrCloseCount();
        final boolean _cacheClosed = this.hashArray == null;
        if (_cacheClosed) {
            this.clearOutReferences();
            throw new CacheClosedException((Cache)this.cache);
        }
        return false;
    }
    
    private void clearOutReferences() {
        this.hash = null;
        this.hashArray = null;
        this.seen = null;
    }
    
    private boolean hasExpansionOccurred() {
        return this.hashArray != this.hash.getEntries();
    }
}
