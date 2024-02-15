//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.*;
import org.cache2k.core.concurrency.*;

public abstract class AbstractEviction implements Eviction, EvictionMetrics
{
    public static final int MINIMAL_CHUNK_SIZE = 4;
    public static final int MAXIMAL_CHUNK_SIZE = 64;
    public static final long MINIMUM_CAPACITY_FOR_CHUNKING = 1000L;
    protected final long maxSize;
    protected final long maxWeight;
    protected final long correctedMaxSizeOrWeight;
    protected final HeapCache heapCache;
    private final Object lock;
    private long newEntryCounter;
    private long removedCnt;
    private long expiredRemovedCnt;
    private long virginRemovedCnt;
    private long evictedCount;
    private long currentWeight;
    private final HeapCacheListener listener;
    private final boolean noListenerCall;
    private Entry[] evictChunkReuse;
    private int chunkSize;
    private int evictionRunningCount;
    private long evictionRunningWeight;
    private final Weigher weigher;
    
    public AbstractEviction(final HeapCache _heapCache, final HeapCacheListener _listener, final long _maxSize, final Weigher _weigher, final long _maxWeight, final boolean _noChunking) {
        this.lock = new Object();
        this.evictionRunningCount = 0;
        this.evictionRunningWeight = 0L;
        this.weigher = _weigher;
        this.heapCache = _heapCache;
        this.listener = _listener;
        this.maxSize = _maxSize;
        this.maxWeight = _maxWeight;
        if (_maxSize < 1000L || _noChunking) {
            this.chunkSize = 1;
        }
        else {
            this.chunkSize = 4 + Runtime.getRuntime().availableProcessors() - 1;
            this.chunkSize = Math.min(64, this.chunkSize);
        }
        this.noListenerCall = (_listener instanceof HeapCacheListener.NoOperation);
        if (this.maxSize >= 0L) {
            if (this.maxSize == Long.MAX_VALUE) {
                this.correctedMaxSizeOrWeight = 4611686018427387903L;
            }
            else {
                this.correctedMaxSizeOrWeight = this.maxSize + this.chunkSize / 2;
            }
        }
        else {
            if (_maxWeight < 0L) {
                throw new IllegalArgumentException("either maxWeight or entryCapacity must be specified");
            }
            if (_maxWeight == Long.MAX_VALUE) {
                this.correctedMaxSizeOrWeight = 4611686018427387903L;
            }
            else {
                this.correctedMaxSizeOrWeight = _maxWeight;
            }
        }
    }
    
    @Override
    public boolean isWeigherPresent() {
        return this.weigher != null;
    }
    
    protected static long getWeightFromEntry(final Entry e) {
        return LongTo16BitFloatingPoint.toLong(e.getCompressedWeight());
    }
    
    protected static void updateWeightInEntry(final Entry e, final long _weight) {
        e.setCompressedWeight(LongTo16BitFloatingPoint.fromLong(_weight));
    }
    
    private long calculateWeight(final Entry e, final Object v) {
        long _weight;
        if (v instanceof ExceptionWrapper) {
            _weight = 1L;
        }
        else {
            _weight = this.weigher.weigh(e.getKey(), v);
        }
        if (_weight < 0L) {
            throw new IllegalArgumentException("weight must be positive.");
        }
        return _weight;
    }
    
    protected void insertAndWeighInLock(final Entry e) {
        if (!this.isWeigherPresent()) {
            return;
        }
    }
    
    protected void updateWeightInLock(final Entry e) {
        final Object v = e.getValueOrException();
        final long _weight = this.calculateWeight(e, v);
        final long _currentWeight = getWeightFromEntry(e);
        if (_currentWeight != _weight) {
            this.currentWeight += _weight - _currentWeight;
            updateWeightInEntry(e, _weight);
        }
    }
    
    protected void updateTotalWeightForRemove(final Entry e) {
        if (!this.isWeigherPresent()) {
            return;
        }
        this.currentWeight -= getWeightFromEntry(e);
    }
    
    @Override
    public void updateWeight(final Entry e) {
        if (!this.isWeigherPresent()) {
            return;
        }
        Entry[] _evictionChunk = null;
        synchronized (this.lock) {
            this.updateWeightInLock(e);
            _evictionChunk = this.fillEvictionChunk();
        }
        this.evictChunk(_evictionChunk);
        for (int _processCount = 1; this.isEvictionNeeded() && _processCount > 0; _processCount = this.evictChunk(_evictionChunk)) {
            synchronized (this.lock) {
                _evictionChunk = this.fillEvictionChunk();
            }
        }
    }
    
    Entry[] reuseChunkArray() {
        Entry[] ea = this.evictChunkReuse;
        if (ea != null) {
            this.evictChunkReuse = null;
        }
        else {
            ea = new Entry[this.chunkSize];
        }
        return ea;
    }
    
    private void removeEventually(final Entry e) {
        if (!e.isRemovedFromReplacementList()) {
            this.removeFromReplacementList(e);
            this.updateTotalWeightForRemove(e);
            final long nrt = e.getNextRefreshTime();
            if (nrt == 12L) {
                ++this.expiredRemovedCnt;
            }
            else if (nrt == 8L) {
                ++this.virginRemovedCnt;
            }
            else {
                ++this.removedCnt;
            }
        }
    }
    
    @Override
    public boolean submitWithoutEviction(final Entry e) {
        synchronized (this.lock) {
            if (e.isNotYetInsertedInReplacementList()) {
                this.insertIntoReplacementList(e);
                this.insertAndWeighInLock(e);
                ++this.newEntryCounter;
            }
            else {
                this.removeEventually(e);
                this.updateTotalWeightForRemove(e);
            }
            return this.isEvictionNeeded();
        }
    }
    
    boolean isEvictionNeeded() {
        if (this.isWeigherPresent()) {
            return this.currentWeight >= this.correctedMaxSizeOrWeight + this.evictionRunningWeight;
        }
        return this.getSize() >= this.correctedMaxSizeOrWeight + this.evictionRunningCount;
    }
    
    @Override
    public void evictEventually() {
        final Entry[] _chunk;
        synchronized (this.lock) {
            _chunk = this.fillEvictionChunk();
        }
        this.evictChunk(_chunk);
    }
    
    @Override
    public void evictEventually(final int _hashCodeHint) {
        this.evictEventually();
    }
    
    private Entry[] fillEvictionChunk() {
        if (!this.isEvictionNeeded()) {
            return null;
        }
        final Entry[] _chunk = this.reuseChunkArray();
        return this.refillChunk(_chunk);
    }
    
    private Entry[] refillChunk(Entry[] _chunk) {
        if (_chunk == null) {
            _chunk = new Entry[this.chunkSize];
        }
        this.evictionRunningCount += _chunk.length;
        for (int i = 0; i < _chunk.length; ++i) {
            _chunk[i] = this.findEvictionCandidate(null);
            if (this.isWeigherPresent()) {
                this.evictionRunningWeight += getWeightFromEntry(_chunk[i]);
            }
        }
        return _chunk;
    }
    
    private int evictChunk(final Entry[] _chunk) {
        if (_chunk == null) {
            return 0;
        }
        final int _processCount = this.removeFromHash(_chunk);
        if (_processCount > 0) {
            synchronized (this.lock) {
                this.removeAllFromReplacementListOnEvict(_chunk);
                this.evictChunkReuse = _chunk;
            }
        }
        return _processCount;
    }
    
    private int removeFromHash(final Entry[] _chunk) {
        if (!this.noListenerCall) {
            return this.removeFromHashWithListener(_chunk);
        }
        return this.removeFromHashWithoutListener(_chunk);
    }
    
    private int removeFromHashWithoutListener(final Entry[] _chunk) {
        int _processCount = 0;
        for (int i = 0; i < _chunk.length; ++i) {
            final Entry e = _chunk[i];
            synchronized (e) {
                if (e.isGone() || e.isProcessing()) {
                    --this.evictionRunningCount;
                    if (this.isWeigherPresent()) {
                        this.evictionRunningWeight -= getWeightFromEntry(e);
                    }
                    _chunk[i] = null;
                    continue;
                }
                this.heapCache.removeEntryForEviction(e);
            }
            ++_processCount;
        }
        return _processCount;
    }
    
    private int removeFromHashWithListener(final Entry[] _chunk) {
        int _processCount = 0;
        for (int i = 0; i < _chunk.length; ++i) {
            final Entry e = _chunk[i];
            synchronized (e) {
                if (e.isGone() || e.isProcessing()) {
                    --this.evictionRunningCount;
                    if (this.isWeigherPresent()) {
                        this.evictionRunningWeight -= getWeightFromEntry(e);
                    }
                    _chunk[i] = null;
                    continue;
                }
                e.startProcessing(16);
            }
            this.listener.onEvictionFromHeap(e);
            synchronized (e) {
                e.processingDone();
                this.heapCache.removeEntryForEviction(e);
            }
            ++_processCount;
        }
        return _processCount;
    }
    
    private void removeAllFromReplacementListOnEvict(final Entry[] _chunk) {
        for (int i = 0; i < _chunk.length; ++i) {
            final Entry e = _chunk[i];
            if (e != null) {
                if (!e.isRemovedFromReplacementList()) {
                    this.removeFromReplacementListOnEvict(e);
                    this.updateTotalWeightForRemove(e);
                    ++this.evictedCount;
                }
                --this.evictionRunningCount;
                if (this.isWeigherPresent()) {
                    this.evictionRunningWeight -= getWeightFromEntry(e);
                }
                _chunk[i] = null;
            }
        }
    }
    
    @Override
    public long getNewEntryCount() {
        return this.newEntryCounter;
    }
    
    @Override
    public long getRemovedCount() {
        return this.removedCnt;
    }
    
    @Override
    public long getVirginRemovedCount() {
        return this.virginRemovedCnt;
    }
    
    @Override
    public long getExpiredRemovedCount() {
        return this.expiredRemovedCnt;
    }
    
    @Override
    public long getEvictedCount() {
        return this.evictedCount;
    }
    
    @Override
    public long getMaxSize() {
        return this.maxSize;
    }
    
    @Override
    public long getMaxWeight() {
        return this.maxWeight;
    }
    
    @Override
    public long getCurrentWeight() {
        return this.currentWeight;
    }
    
    @Override
    public int getEvictionRunningCount() {
        return this.evictionRunningCount;
    }
    
    @Override
    public EvictionMetrics getMetrics() {
        return this;
    }
    
    @Override
    public void start() {
    }
    
    @Override
    public void stop() {
    }
    
    @Override
    public boolean drain() {
        return false;
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public <T> T runLocked(final Job<T> j) {
        synchronized (this.lock) {
            return j.call();
        }
    }
    
    protected void removeFromReplacementListOnEvict(final Entry e) {
        this.removeFromReplacementList(e);
    }
    
    protected abstract Entry findEvictionCandidate(final Entry p0);
    
    protected abstract void removeFromReplacementList(final Entry p0);
    
    protected abstract void insertIntoReplacementList(final Entry p0);
    
    @Override
    public String getExtraStatistics() {
        return "impl=" + this.getClass().getSimpleName() + ", chunkSize=" + this.chunkSize;
    }
}
