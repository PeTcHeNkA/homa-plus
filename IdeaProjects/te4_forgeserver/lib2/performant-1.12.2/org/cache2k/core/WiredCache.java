//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.integration.*;
import org.cache2k.event.*;
import org.cache2k.core.util.*;
import org.cache2k.configuration.*;
import java.util.concurrent.atomic.*;
import java.util.*;
import org.cache2k.processor.*;
import org.cache2k.core.operation.*;
import org.cache2k.core.storageApi.*;
import org.cache2k.*;
import java.util.concurrent.*;

public class WiredCache<K, V> extends BaseCache<K, V> implements StorageAdapter.Parent, HeapCacheListener<K, V>
{
    final Operations<K, V> SPEC;
    HeapCache<K, V> heapCache;
    StorageAdapter storage;
    AdvancedCacheLoader<K, V> loader;
    AsyncCacheLoader<K, V> asyncLoader;
    CacheWriter<K, V> writer;
    CacheEntryRemovedListener<K, V>[] syncEntryRemovedListeners;
    CacheEntryCreatedListener<K, V>[] syncEntryCreatedListeners;
    CacheEntryUpdatedListener<K, V>[] syncEntryUpdatedListeners;
    CacheEntryExpiredListener<K, V>[] syncEntryExpiredListeners;
    CacheEntryEvictedListener<K, V>[] syncEntryEvictedListeners;
    private static final EntryAction.ActionCompletedCallback NOOP_CALLBACK;
    
    public WiredCache() {
        this.SPEC = (org.cache2k.core.operation.Operations<K, V>)Operations.SINGLETON;
    }
    
    private CommonMetrics.Updater metrics() {
        return this.heapCache.metrics;
    }
    
    public Log getLog() {
        return this.heapCache.getLog();
    }
    
    public HeapCache getHeapCache() {
        return this.heapCache;
    }
    
    public InternalClock getClock() {
        return this.heapCache.getClock();
    }
    
    public boolean isNullValuePermitted() {
        return this.heapCache.isNullValuePermitted();
    }
    
    public String getName() {
        return this.heapCache.getName();
    }
    
    public CacheType getKeyType() {
        return this.heapCache.getKeyType();
    }
    
    public CacheType getValueType() {
        return this.heapCache.getValueType();
    }
    
    public CacheManager getCacheManager() {
        return this.heapCache.getCacheManager();
    }
    
    public V computeIfAbsent(final K key, final Callable<V> callable) {
        return (V)this.returnValue((V) this.execute(key, this.SPEC.computeIfAbsent((K) key, callable)));
    }
    
    public V peekAndPut(final K key, final V value) {
        return (V)this.returnValue(this.execute(key, this.SPEC.peekAndPut(key, value)));
    }
    
    public V peekAndRemove(final K key) {
        return (V)this.returnValue(this.execute(key, this.SPEC.peekAndRemove((K) key)));
    }
    
    public V peekAndReplace(final K key, final V value) {
        return (V)this.returnValue(this.execute(key, this.SPEC.peekAndReplace(key, value)));
    }
    
    public CacheEntry<K, V> peekEntry(final K key) {
        return this.execute(key, this.SPEC.peekEntry(key));
    }
    
    public void prefetch(final K key) {
        if (!this.isLoaderPresent()) {
            return;
        }
        final Entry<K, V> e = this.heapCache.lookupEntryNoHitRecord(key);
        if (e != null && e.hasFreshData(this.getClock())) {
            return;
        }
        try {
            this.heapCache.getPrefetchExecutor().execute((Runnable)new HeapCache.RunWithCatch(this) {
                public void action() {
                    WiredCache.this.load(key);
                }
            });
        }
        catch (RejectedExecutionException ex) {}
    }
    
    public void prefetchAll(final Iterable<? extends K> _keys, final CacheOperationCompletionListener l) {
        final CacheOperationCompletionListener _listener = (l != null) ? l : HeapCache.DUMMY_LOAD_COMPLETED_LISTENER;
        if (!this.isLoaderPresent()) {
            _listener.onCompleted();
            return;
        }
        final AtomicInteger _count = new AtomicInteger(2);
        try {
            final Set<K> _keysToLoad = this.heapCache.checkAllPresent(_keys);
            for (final K key : _keysToLoad) {
                final Runnable r = new HeapCache.RunWithCatch(this) {
                    public void action() {
                        try {
                            WiredCache.this.load(key);
                        }
                        finally {
                            if (_count.decrementAndGet() == 0) {
                                _listener.onCompleted();
                            }
                        }
                    }
                };
                try {
                    this.heapCache.getPrefetchExecutor().execute(r);
                    _count.incrementAndGet();
                }
                catch (RejectedExecutionException ex) {}
            }
        }
        finally {
            if (_count.addAndGet(-2) == 0) {
                _listener.onCompleted();
            }
        }
    }
    
    private void load(final K key) {
        final Entry<K, V> e = this.lookupQuick(key);
        if (e != null && e.hasFreshData(this.getClock())) {
            return;
        }
        this.load(key, e);
    }
    
    private void load(final K key, final Entry<K, V> _e) {
        this.execute(key, _e, this.SPEC.get(key));
    }
    
    public boolean containsKey(final K key) {
        return this.execute(key, this.SPEC.contains(key));
    }
    
    public boolean putIfAbsent(final K key, final V value) {
        return this.execute(key, this.SPEC.putIfAbsent(key, value));
    }
    
    public void put(final K key, final V value) {
        this.execute(key, this.SPEC.put(key, value));
    }
    
    public void putAll(final Map<? extends K, ? extends V> m) {
        for (final Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }
    
    public void remove(final K key) {
        this.execute(key, this.SPEC.remove(key));
    }
    
    public boolean removeIfEquals(final K key, final V value) {
        return this.execute(key, this.SPEC.remove(key, value));
    }
    
    public boolean containsAndRemove(final K key) {
        return this.execute(key, this.SPEC.containsAndRemove(key));
    }
    
    public boolean replace(final K key, final V _newValue) {
        return this.execute(key, this.SPEC.replace(key, _newValue));
    }
    
    public boolean replaceIfEquals(final K key, final V _oldValue, final V _newValue) {
        return this.execute(key, this.SPEC.replace(key, _oldValue, _newValue));
    }
    
    public void loadAll(final Iterable<? extends K> _keys, final CacheOperationCompletionListener l) {
        this.checkLoaderPresent();
        final CacheOperationCompletionListener _listener = (l != null) ? l : HeapCache.DUMMY_LOAD_COMPLETED_LISTENER;
        final Set<K> _keysToLoad = this.heapCache.checkAllPresent(_keys);
        if (_keysToLoad.isEmpty()) {
            _listener.onCompleted();
            return;
        }
        if (this.asyncLoader != null) {
            this.loadAllWithAsyncLoader(_listener, _keysToLoad);
        }
        else {
            this.loadAllWithSyncLoader(_listener, _keysToLoad);
        }
    }
    
    private void loadAllWithAsyncLoader(final CacheOperationCompletionListener _listener, final Set<K> _keysToLoad) {
        final AtomicInteger _countDown = new AtomicInteger(_keysToLoad.size());
        final EntryAction.ActionCompletedCallback cb = (EntryAction.ActionCompletedCallback)new EntryAction.ActionCompletedCallback() {
            public void entryActionCompleted(final EntryAction ea) {
                final int v = _countDown.decrementAndGet();
                if (v == 0) {
                    _listener.onCompleted();
                }
            }
        };
        for (final K k : _keysToLoad) {
            final K key2;
            final K key = key2 = k;
            final org.cache2k.core.Entry<K, V> e = null;
            final org.cache2k.core.operation.Operations<K, V> spec = this.SPEC;
            this.executeAsync(key2, e, (org.cache2k.core.operation.Semantic<K, V, Object>)Operations.GET, cb);
        }
    }
    
    private void loadAllWithSyncLoader(final CacheOperationCompletionListener _listener, final Set<K> _keysToLoad) {
        final AtomicInteger _countDown = new AtomicInteger(_keysToLoad.size());
        for (final K key : _keysToLoad) {
            final K k = key;
            final Runnable r = (Runnable)new HeapCache.RunWithCatch(this) {
                public void action() {
                    try {
                        WiredCache.this.load(key);
                    }
                    finally {
                        final int v = _countDown.decrementAndGet();
                        if (v == 0) {
                            _listener.onCompleted();
                        }
                    }
                }
            };
            try {
                this.heapCache.loaderExecutor.execute(r);
            }
            catch (RejectedExecutionException ex) {
                r.run();
            }
        }
    }
    
    public void reloadAll(final Iterable<? extends K> _keys, final CacheOperationCompletionListener l) {
        this.checkLoaderPresent();
        final CacheOperationCompletionListener _listener = (l != null) ? l : HeapCache.DUMMY_LOAD_COMPLETED_LISTENER;
        final Set<K> _keySet = (Set<K>)this.heapCache.generateKeySet((Iterable)_keys);
        if (this.asyncLoader != null) {
            this.reloadAllWithAsyncLoader(_listener, _keySet);
        }
        else {
            this.reloadAllWithSyncLoader(_listener, _keySet);
        }
    }
    
    private void reloadAllWithAsyncLoader(final CacheOperationCompletionListener _listener, final Set<K> _keySet) {
        final AtomicInteger _countDown = new AtomicInteger(_keySet.size());
        final EntryAction.ActionCompletedCallback cb = (EntryAction.ActionCompletedCallback)new EntryAction.ActionCompletedCallback() {
            public void entryActionCompleted(final EntryAction ea) {
                if (_countDown.decrementAndGet() == 0) {
                    _listener.onCompleted();
                }
            }
        };
        for (final K key : _keySet) {
            final K k = key;
            this.executeAsync(key, null, (org.cache2k.core.operation.Semantic<K, V, Object>)this.SPEC.UNCONDITIONAL_LOAD, cb);
        }
    }
    
    private <R> void executeAsync(final K key, final Entry<K, V> e, final Semantic<K, V, R> op, final EntryAction.ActionCompletedCallback cb) {
        final EntryAction<K, V, R> _action = (EntryAction<K, V, R>)new MyEntryAction((org.cache2k.core.operation.Semantic<K, V, Object>)op, key, e, cb);
        this.execute((Semantic)op, (EntryAction)_action);
    }
    
    private void reloadAllWithSyncLoader(final CacheOperationCompletionListener _listener, final Set<K> _keySet) {
        final AtomicInteger _countDown = new AtomicInteger(_keySet.size());
        for (final K key : _keySet) {
            final K k = key;
            final Runnable r = (Runnable)new HeapCache.RunWithCatch(this) {
                public void action() {
                    try {
                        WiredCache.this.execute(key, WiredCache.this.SPEC.UNCONDITIONAL_LOAD);
                    }
                    finally {
                        if (_countDown.decrementAndGet() == 0) {
                            _listener.onCompleted();
                        }
                    }
                }
            };
            try {
                this.heapCache.loaderExecutor.execute(r);
            }
            catch (RejectedExecutionException ex) {
                r.run();
            }
        }
    }
    
    public void expireAt(final K key, final long _millis) {
        this.execute(key, this.SPEC.expire(key, _millis));
    }
    
    private void checkLoaderPresent() {
        if (!this.isLoaderPresent()) {
            throw new UnsupportedOperationException("loader not set");
        }
    }
    
    public boolean isWeigherPresent() {
        return this.heapCache.eviction.isWeigherPresent();
    }
    
    public boolean isLoaderPresent() {
        return this.loader != null || this.asyncLoader != null;
    }
    
    V returnValue(final V v) {
        return (V)this.heapCache.returnValue(v);
    }
    
    V returnValue(final Entry<K, V> e) {
        return (V)this.returnValue(e.getValueOrException());
    }
    
    Entry<K, V> lookupQuick(final K key) {
        return this.heapCache.lookupEntry(key);
    }
    
    public V get(final K key) {
        final Entry<K, V> e = this.lookupQuick(key);
        if (e != null && e.hasFreshData(this.getClock())) {
            return this.returnValue(e);
        }
        return this.returnValue(this.execute(key, e, this.SPEC.get(key)));
    }
    
    public Map<K, V> getAll(final Iterable<? extends K> keys) {
        final Map<K, CacheEntry<K, V>> map = new HashMap<>();
        for (final K k : keys) {
            final CacheEntry<K, V> e = this.execute(k, this.SPEC.getEntry(k));
            if (e != null) {
                map.put(k, e);
            }
        }
        return this.heapCache.convertCacheEntry2ValueMap(map);
    }
    
    public CacheEntry<K, V> getEntry(final K key) {
        return this.execute(key, this.SPEC.getEntry(key));
    }
    
    public int getTotalEntryCount() {
        if (this.storage != null) {
            return (int)this.storage.getTotalEntryCount();
        }
        return this.heapCache.getTotalEntryCount();
    }
    
    public <R> R invoke(final K key, final EntryProcessor<K, V, R> entryProcessor) {
        return (R)this.execute(key, this.SPEC.invoke(key, this.loader != null, (EntryProcessor)entryProcessor));
    }
    
    public Iterator<CacheEntry<K, V>> iterator() {
        Iterator<CacheEntry<K, V>> tor;
        if (this.storage == null) {
            tor = new HeapCache.IteratorFilterEntry2Entry<>(this.heapCache, this.heapCache.iterateAllHeapEntries(), true);
        }
        else {
            tor = new HeapCache.IteratorFilterEntry2Entry(this.heapCache, this.storage.iterateAll(), false);
        }
        final Iterator<CacheEntry<K, V>> it = tor;
        final Iterator<CacheEntry<K, V>> _adapted = new Iterator<CacheEntry<K, V>>() {
            CacheEntry<K, V> entry;
            
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
            
            @Override
            public CacheEntry<K, V> next() {
                return this.entry = (CacheEntry<K, V>)it.next();
            }
            
            @Override
            public void remove() {
                if (this.entry == null) {
                    throw new IllegalStateException("call next first");
                }
                WiredCache.this.remove(this.entry.getKey());
            }
        };
        return _adapted;
    }
    
    public V peek(final K key) {
        final Entry<K, V> e = this.lookupQuick(key);
        if (e != null && e.hasFreshData(this.getClock())) {
            return this.returnValue(e);
        }
        return (V)this.returnValue(this.execute(key, this.SPEC.peek(key)));
    }
    
    public Map<K, V> peekAll(final Iterable<? extends K> keys) {
        final Map<K, CacheEntry<K, V>> map = new HashMap<>();
        for (final K k : keys) {
            final CacheEntry<K, V> e = this.execute(k, this.SPEC.peekEntry((K) k));
            if (e != null) {
                map.put(k, e);
            }
        }
        return this.heapCache.convertCacheEntry2ValueMap(map);
    }
    
    public InternalCacheInfo getLatestInfo() {
        return this.heapCache.getLatestInfo(this);
    }
    
    public InternalCacheInfo getInfo() {
        return this.heapCache.getInfo(this);
    }
    
    public CommonMetrics getCommonMetrics() {
        return this.heapCache.getCommonMetrics();
    }
    
    public void logAndCountInternalException(final String s, final Throwable t) {
        this.heapCache.logAndCountInternalException(s, t);
    }
    
    public void checkIntegrity() {
        this.heapCache.checkIntegrity();
    }
    
    public boolean isClosed() {
        return this.heapCache.isClosed();
    }
    
    public void init() {
        if (this.storage == null && this.heapCache.eviction.getMetrics().getMaxSize() == 0L) {
            throw new IllegalArgumentException("maxElements must be >0");
        }
        if (this.storage != null) {
            this.storage.open();
        }
        this.heapCache.timing.init((InternalCache)this);
        this.heapCache.initWithoutTimerHandler();
    }
    
    public void cancelTimerJobs() {
        synchronized (this.lockObject()) {
            this.heapCache.cancelTimerJobs();
            if (this.storage != null) {
                this.storage.cancelTimerJobs();
            }
        }
    }
    
    public void clear() {
        if (this.storage != null) {
            this.storage.clear();
            return;
        }
        this.heapCache.clear();
    }
    
    public void close() {
        try {
            this.heapCache.closePart1();
        }
        catch (CacheClosedException ex2) {
            return;
        }
        Future<Void> _waitForStorage = null;
        if (this.storage != null) {
            _waitForStorage = (Future<Void>)this.storage.shutdown();
        }
        if (_waitForStorage != null) {
            try {
                _waitForStorage.get();
            }
            catch (Exception ex) {
                StorageAdapter.rethrow("shutdown", (Throwable)ex);
            }
        }
        synchronized (this.lockObject()) {
            this.storage = null;
        }
        this.heapCache.closePart2((InternalCache)this);
        this.closeCustomization((Object)this.writer, "writer");
        if (this.syncEntryCreatedListeners != null) {
            for (final Object l : this.syncEntryCreatedListeners) {
                this.closeCustomization(l, "entryCreatedListener");
            }
        }
        if (this.syncEntryUpdatedListeners != null) {
            for (final Object l : this.syncEntryUpdatedListeners) {
                this.closeCustomization(l, "entryUpdatedListener");
            }
        }
        if (this.syncEntryRemovedListeners != null) {
            for (final Object l : this.syncEntryRemovedListeners) {
                this.closeCustomization(l, "entryRemovedListener");
            }
        }
        if (this.syncEntryExpiredListeners != null) {
            for (final Object l : this.syncEntryExpiredListeners) {
                this.closeCustomization(l, "entryExpiredListener");
            }
        }
    }
    
    public CacheEntry<K, V> returnCacheEntry(final ExaminationEntry<K, V> e) {
        return (CacheEntry<K, V>)this.heapCache.returnCacheEntry((ExaminationEntry)e);
    }
    
    private Object lockObject() {
        return this.heapCache.lock;
    }
    
    public void resetStorage(final StorageAdapter _from, final StorageAdapter to) {
        synchronized (this.lockObject()) {
            this.storage = to;
        }
    }
    
    public StorageAdapter getStorage() {
        return this.storage;
    }
    
    public void lockAndRunForPurge(final K key, final PurgeableStorage.PurgeAction _action) {
        throw new UnsupportedOperationException();
    }
    
    public void onEvictionFromHeap(final Entry<K, V> e) {
        final CacheEntry<K, V> _currentEntry = (CacheEntry<K, V>)this.heapCache.returnCacheEntry((ExaminationEntry)e);
        if (this.syncEntryEvictedListeners != null) {
            for (final CacheEntryEvictedListener<K, V> l : this.syncEntryEvictedListeners) {
                l.onEntryEvicted((org.cache2k.Cache<K, V>)this, _currentEntry);
            }
        }
    }
    
    protected <R> EntryAction<K, V, R> createEntryAction(final K key, final Entry<K, V> e, final Semantic<K, V, R> op) {
        return (EntryAction<K, V, R>)new MyEntryAction(op, key, e);
    }
    
    public String getEntryState(final K key) {
        return this.heapCache.getEntryState((K) key);
    }
    
    public void timerEventExpireEntry(final Entry<K, V> e) {
        this.metrics().timerEvent();
        synchronized (e) {
            this.expireOrScheduleFinalExpireEvent(e);
        }
    }
    
    public void expireOrScheduleFinalExpireEvent(final Entry<K, V> e) {
        this.heapCache.expireOrScheduleFinalExpireEvent((Entry)e);
        if (e.isExpired() || e.isGone()) {
            this.callExpiryListeners(e);
        }
    }
    
    private void callExpiryListeners(final Entry<K, V> e) {
        if (this.syncEntryExpiredListeners != null) {
            final CacheEntry<K, V> _entryCopy = (CacheEntry<K, V>)this.heapCache.returnCacheEntry((ExaminationEntry)e);
            for (final CacheEntryExpiredListener<K, V> l : this.syncEntryExpiredListeners) {
                l.onEntryExpired(this, _entryCopy);
            }
        }
    }
    
    public void timerEventRefresh(final Entry<K, V> e) {
        this.metrics().timerEvent();
        synchronized (e) {
            if (e.isGone()) {
                return;
            }
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (WiredCache.this.storage == null) {
                        synchronized (e) {
                            e.waitForProcessing();
                            if (e.isGone()) {
                                return;
                            }
                        }
                    }
                    try {
                        WiredCache.this.executeAsync(e.getKey(), e, Operations.REFRESH, WiredCache.NOOP_CALLBACK);
                    }
                    catch (CacheClosedException ex2) {}
                    catch (Throwable ex) {
                        WiredCache.this.getLog().warn("Exception starting async refresh", ex);
                        try {
                            synchronized (e) {
                                WiredCache.this.heapCache.expireEntry(e);
                            }
                        }
                        catch (CacheClosedException ex3) {}
                    }
                }
            };
            try {
                this.heapCache.loaderExecutor.execute(r);
            }
            catch (RejectedExecutionException ex) {
                this.metrics().refreshFailed();
                this.expireOrScheduleFinalExpireEvent(e);
            }
        }
    }
    
    public void timerEventProbationTerminated(final Entry<K, V> e) {
        this.metrics().timerEvent();
        synchronized (e) {
            this.expireOrScheduleFinalExpireEvent(e);
        }
    }
    
    static {
        NOOP_CALLBACK = ea -> {};
    }
    
    class MyEntryAction<R> extends EntryAction<K, V, R>
    {
        public MyEntryAction(final Semantic<K, V, R> op, final K _k, final Entry<K, V> e) {
            super(WiredCache.this.heapCache, WiredCache.this, (Semantic)op, _k, e);
        }
        
        public MyEntryAction(final Semantic<K, V, R> op, final K _k, final Entry<K, V> e, final EntryAction.ActionCompletedCallback cb) {
            super(WiredCache.this.heapCache, WiredCache.this, (Semantic)op, _k, e, cb);
        }
        
        protected boolean mightHaveListeners() {
            return true;
        }
        
        protected CacheEntryCreatedListener<K, V>[] entryCreatedListeners() {
            return WiredCache.this.syncEntryCreatedListeners;
        }
        
        protected CacheEntryRemovedListener<K, V>[] entryRemovedListeners() {
            return WiredCache.this.syncEntryRemovedListeners;
        }
        
        protected CacheEntryUpdatedListener<K, V>[] entryUpdatedListeners() {
            return WiredCache.this.syncEntryUpdatedListeners;
        }
        
        protected CacheEntryExpiredListener<K, V>[] entryExpiredListeners() {
            return WiredCache.this.syncEntryExpiredListeners;
        }
        
        protected CacheWriter<K, V> writer() {
            return WiredCache.this.writer;
        }
        
        protected TimingHandler<K, V> timing() {
            return (TimingHandler<K, V>)this.heapCache.timing;
        }
        
        public Executor getLoaderExecutor() {
            return this.heapCache.loaderExecutor;
        }
        
        protected AsyncCacheLoader<K, V> asyncLoader() {
            return WiredCache.this.asyncLoader;
        }
    }
}
