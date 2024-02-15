//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.event.*;
import org.cache2k.configuration.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.cache2k.integration.*;
import org.cache2k.core.operation.*;
import org.cache2k.processor.*;
import java.util.*;
import org.cache2k.*;
import org.cache2k.core.util.*;
import org.cache2k.core.concurrency.*;

public class HeapCache<K, V> extends BaseCache<K, V>
{
    static final CacheOperationCompletionListener DUMMY_LOAD_COMPLETED_LISTENER;
    static int cacheCnt;
    public static final Tunable TUNABLE;
    public static final ExceptionPropagator DEFAULT_EXCEPTION_PROPAGATOR;
    protected String name;
    public CacheManagerImpl manager;
    protected AdvancedCacheLoader<K, V> loader;
    protected InternalClock clock;
    protected TimingHandler<K, V> timing;
    public final Object lock;
    protected CacheBaseInfo info;
    CommonMetrics.Updater metrics;
    protected volatile long keyMutationCnt;
    protected long clearedTime;
    protected long startedTime;
    Eviction eviction;
    protected long clearRemovedCnt;
    protected long clearCnt;
    protected long internalExceptionCnt;
    protected volatile Executor loaderExecutor;
    protected volatile Executor prefetchExecutor;
    protected final Hash2<K, V> hash;
    private volatile boolean closing;
    protected CacheType keyType;
    protected CacheType valueType;
    protected ExceptionPropagator<K> exceptionPropagator;
    private Collection<CustomizationSupplier<CacheClosedListener>> cacheClosedListeners;
    private int featureBits;
    private static final int KEEP_AFTER_EXPIRED = 2;
    private static final int REJECT_NULL_VALUES = 8;
    private static final int BACKGROUND_REFRESH = 16;
    private static final int UPDATE_TIME_NEEDED = 32;
    private static final int RECORD_REFRESH_TIME = 64;
    static final byte INSERT_STAT_LOAD = 1;
    static final byte INSERT_STAT_PUT = 2;
    private static final Object RESTART_AFTER_EVICTION;
    
    public HeapCache() {
        this.timing = (TimingHandler<K, V>)TimingHandler.ETERNAL;
        this.lock = new Object();
        this.keyMutationCnt = 0L;
        this.clearedTime = 0L;
        this.clearRemovedCnt = 0L;
        this.clearCnt = 0L;
        this.internalExceptionCnt = 0L;
        this.loaderExecutor = new LazyLoaderExecutor();
        this.prefetchExecutor = new LazyPrefetchExecutor();
        this.hash = this.createHashTable();
        this.closing = true;
        this.exceptionPropagator = (ExceptionPropagator<K>)HeapCache.DEFAULT_EXCEPTION_PROPAGATOR;
        this.cacheClosedListeners = (Collection<CustomizationSupplier<CacheClosedListener>>)Collections.EMPTY_LIST;
        this.featureBits = 0;
    }
    
    protected final boolean isKeepAfterExpired() {
        return (this.featureBits & 0x2) > 0;
    }
    
    protected final boolean isRejectNullValues() {
        return (this.featureBits & 0x8) > 0;
    }
    
    public final boolean isNullValuePermitted() {
        return !this.isRejectNullValues();
    }
    
    protected final boolean isRefreshAhead() {
        return (this.featureBits & 0x10) > 0;
    }
    
    protected final boolean isUpdateTimeNeeded() {
        return (this.featureBits & 0x20) > 0;
    }
    
    protected final boolean isRecordRefreshTime() {
        return (this.featureBits & 0x40) > 0;
    }
    
    protected final void setFeatureBit(final int _bitmask, final boolean _flag) {
        if (_flag) {
            this.featureBits |= _bitmask;
        }
        else {
            this.featureBits &= ~_bitmask;
        }
    }
    
    public String getCompleteName() {
        return this.manager.getName() + ":" + this.name;
    }
    
    public Log getLog() {
        return Log.getLog(Cache.class.getName() + '/' + this.getCompleteName());
    }
    
    public void setCacheConfig(final Cache2kConfiguration c) {
        this.valueType = c.getValueType();
        this.keyType = c.getKeyType();
        if (this.name != null) {
            throw new IllegalStateException("already configured");
        }
        this.setName(c.getName());
        this.setFeatureBit(2, c.isKeepDataAfterExpired());
        this.setFeatureBit(8, !c.isPermitNullValues());
        this.setFeatureBit(16, c.isRefreshAhead());
        this.setFeatureBit(32, c.isRecordRefreshedTime());
        this.setFeatureBit(64, c.isRecordRefreshedTime());
        this.metrics = HeapCache.TUNABLE.commonMetricsFactory.create((CommonMetricsFactory.Parameters)new CommonMetricsFactory.Parameters() {
            public boolean isDisabled() {
                return c.isDisableStatistics();
            }
            
            public boolean isPrecise() {
                return false;
            }
        });
        if (c.getLoaderExecutor() != null) {
            this.loaderExecutor = (Executor)this.createCustomization(c.getLoaderExecutor());
        }
        else if (c.getLoaderThreadCount() > 0) {
            this.loaderExecutor = this.provideDefaultLoaderExecutor(c.getLoaderThreadCount());
        }
        if (c.getPrefetchExecutor() != null) {
            this.prefetchExecutor = (Executor)this.createCustomization(c.getPrefetchExecutor());
        }
    }
    
    String getThreadNamePrefix() {
        return "cache2k-loader-" + Util.compactFullName((CacheManager)this.manager, this.name);
    }
    
    Executor provideDefaultLoaderExecutor(final int _threadCount) {
        return (Executor)new ExclusiveExecutor(_threadCount, this.getThreadNamePrefix());
    }
    
    public void setTiming(final TimingHandler<K, V> rh) {
        this.timing = rh;
        if (!(rh instanceof TimingHandler.TimeAgnostic)) {
            this.setFeatureBit(32, true);
        }
    }
    
    public void setClock(final InternalClock _clock) {
        this.clock = _clock;
    }
    
    public void setExceptionPropagator(final ExceptionPropagator ep) {
        this.exceptionPropagator = (ExceptionPropagator<K>)ep;
    }
    
    public void setAdvancedLoader(final AdvancedCacheLoader<K, V> al) {
        this.loader = al;
    }
    
    public void setName(String n) {
        if (n == null) {
            n = this.getClass().getSimpleName() + "#" + HeapCache.cacheCnt++;
        }
        this.name = n;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setCacheManager(final CacheManagerImpl cm) {
        this.manager = cm;
    }
    
    public CacheType getKeyType() {
        return this.keyType;
    }
    
    public CacheType getValueType() {
        return this.valueType;
    }
    
    public void init() {
        this.timing.init((InternalCache<K, V>)this);
        this.initWithoutTimerHandler();
    }
    
    public void initWithoutTimerHandler() {
        if (this.name == null) {
            this.name = String.valueOf(HeapCache.cacheCnt++);
        }
        synchronized (this.lock) {
            this.initializeHeapCache();
            if (this.isRefreshAhead() && this.timing instanceof TimingHandler.TimeAgnostic) {
                throw new IllegalArgumentException("refresh ahead enabled, but no expiry variant defined");
            }
            this.closing = false;
        }
    }
    
    public void checkClosed() {
        if (this.closing) {
            throw new CacheClosedException((Cache)this);
        }
    }


    public final void clear() {
        this.executeWithGlobalLock((Job<Void>) () -> {
            HeapCache.this.clearLocalCache();
            return null;
        });
    }


    public final void clearLocalCache() {
        final long _removed = this.eviction.removeAll();
        this.clearRemovedCnt += _removed;
        ++this.clearCnt;
        this.initializeHeapCache();
        this.hash.clearWhenLocked();
        this.clearedTime = this.clock.millis();
    }
    
    protected void initializeHeapCache() {
        if (this.startedTime == 0L) {
            this.startedTime = this.clock.millis();
        }
        this.timing.reset();
    }
    
    public void cancelTimerJobs() {
        this.timing.close();
    }
    
    public boolean isClosed() {
        return this.closing;
    }


    public void closePart1() throws CacheClosedException {
        this.executeWithGlobalLock((Job<Void>) () -> {
            HeapCache.this.closing = true;
            return null;
        });
        this.closeCustomization(this.loaderExecutor, "loaderExecutor");
        this.cancelTimerJobs();
    }


    public void close() {
        try {
            this.closePart1();
        }
        catch (CacheClosedException ex) {
            return;
        }
        this.closePart2((InternalCache)this);
    }


    public void closePart2(final InternalCache _userCache) {
        this.executeWithGlobalLock((Job<Void>) () -> {
            HeapCache.this.eviction.close();
            HeapCache.this.timing.shutdown();
            HeapCache.this.hash.close();
            HeapCache.this.closeCustomization((Object)HeapCache.this.loader, "loader");
            for (final CustomizationSupplier<CacheClosedListener> s : HeapCache.this.cacheClosedListeners) {
                ((CacheClosedListener)HeapCache.this.createCustomization((CustomizationSupplier)s)).onCacheClosed((Cache)_userCache);
            }
            HeapCache.this.manager.cacheDestroyed((Cache)_userCache);
            return null;
        }, false);
    }
    
    public void setCacheClosedListeners(final Collection<CustomizationSupplier<CacheClosedListener>> l) {
        this.cacheClosedListeners = l;
    }
    
    public Iterator<CacheEntry<K, V>> iterator() {
        return new IteratorFilterEntry2Entry<K, V>(this, (Iterator<org.cache2k.core.Entry<K, V>>)this.iterateAllHeapEntries(), true);
    }
    
    protected void recordHit(final Entry e) {
        ++e.hitCnt;
    }
    
    public V get(final K key) {
        final Entry<K, V> e = (Entry<K, V>)this.getEntryInternal(key);
        if (e == null) {
            return null;
        }
        return this.returnValue(e);
    }
    
    protected CacheEntry<K, V> returnEntry(final ExaminationEntry<K, V> e) {
        if (e == null) {
            return null;
        }
        return this.returnCacheEntry(e);
    }
    
    public String getEntryState(final K key) {
        final Entry e = this.lookupEntry(key);
        if (e == null) {
            return null;
        }
        synchronized (e) {
            return e.toString(this);
        }
    }
    
    public CacheEntry<K, V> returnCacheEntry(final ExaminationEntry<K, V> _entry) {
        return this.returnCacheEntry(_entry.getKey(), _entry.getValueOrException());
    }
    
    public CacheEntry<K, V> returnCacheEntry(final K key, final V _valueOrException) {
        if (_valueOrException instanceof ExceptionWrapper) {
            final ExceptionWrapper _warpper = (ExceptionWrapper)_valueOrException;
            final ExceptionPropagator<K> _exceptionPropagator = this.exceptionPropagator;
            return (CacheEntry<K, V>)new BaseCacheEntry<K, V>() {
                public K getKey() {
                    return key;
                }
                
                public V getValue() {
                    throw _exceptionPropagator.propagateException(key, (ExceptionInformation)_warpper);
                }
                
                @Override
                public Throwable getException() {
                    return _warpper.getException();
                }
            };
        }
        return (CacheEntry<K, V>)new BaseCacheEntry<K, V>() {
            V _value = _valueOrException;
            
            public K getKey() {
                return key;
            }
            
            public V getValue() {
                return this._value;
            }
        };
    }
    
    public CacheEntry<K, V> getEntry(final K key) {
        return this.returnEntry((ExaminationEntry<K, V>)this.getEntryInternal(key));
    }
    
    protected Entry getEntryInternal(final K key) {
        final int hc = modifiedHash(key.hashCode());
        return this.getEntryInternal(key, hc, this.extractIntKeyValue(key, hc));
    }
    
    protected Entry<K, V> getEntryInternal(final K key, final int hc, final int val) {
        if (this.loader == null) {
            return this.peekEntryInternal(key, hc, val);
        }
        while (true) {
            final Entry e = this.lookupOrNewEntry(key, hc, val);
            if (e.hasFreshData(this.clock)) {
                return (Entry<K, V>)e;
            }
            synchronized (e) {
                e.waitForProcessing();
                if (e.hasFreshData(this.clock)) {
                    return (Entry<K, V>)e;
                }
                if (e.isGone()) {
                    this.metrics.goneSpin();
                    continue;
                }
                e.startProcessing(4);
            }
            boolean _finished = false;
            try {
                this.load((org.cache2k.core.Entry<K, V>)e);
                _finished = true;
            }
            finally {
                e.ensureAbort(_finished);
            }
            if (e.getValueOrException() == null && this.isRejectNullValues()) {
                return null;
            }
            return (Entry<K, V>)e;
        }
    }
    
    protected void finishLoadOrEviction(final Entry e, final long _nextRefreshTime) {
        if (e.getProcessingState() != 7) {
            this.restartTimer(e, _nextRefreshTime);
        }
        else {
            this.startRefreshProbationTimer((org.cache2k.core.Entry<K, V>)e, _nextRefreshTime);
        }
        e.processingDone();
    }
    
    private void restartTimer(final Entry e, final long _nextRefreshTime) {
        e.setNextRefreshTime(this.timing.stopStartTimer(_nextRefreshTime, (org.cache2k.core.Entry<K, V>)e));
        this.checkIfImmediatelyExpired(e);
    }
    
    private void checkIfImmediatelyExpired(final Entry e) {
        if (e.isExpired()) {
            this.expireAndRemoveEventuallyAfterProcessing(e);
        }
    }
    
    protected boolean removeEntry(final Entry e) {
        final int hc = this.extractModifiedHash(e);
        final OptimisticLock l = this.hash.getSegmentLock(hc);
        final long _stamp = l.writeLock();
        boolean _removed;
        try {
            _removed = this.hash.removeWithinLock(e, hc);
            e.setGone();
            if (_removed) {
                this.eviction.submitWithoutEviction(e);
            }
        }
        finally {
            l.unlockWrite(_stamp);
        }
        this.checkForHashCodeChange((org.cache2k.core.Entry<K, V>)e);
        this.timing.cancelExpiryTimer((org.cache2k.core.Entry<K, V>)e);
        return _removed;
    }
    
    public V peekAndPut(final K key, final V _value) {
        final int hc = modifiedHash(key.hashCode());
        final int val = this.extractIntKeyValue(key, hc);
        V _previousValue = null;
        while (true) {
            final Entry e = this.lookupOrNewEntry(key, hc, val);
            synchronized (e) {
                e.waitForProcessing();
                if (e.isGone()) {
                    this.metrics.goneSpin();
                    continue;
                }
                final boolean _hasFreshData = e.hasFreshData(this.clock);
                if (_hasFreshData) {
                    _previousValue = (V)e.getValueOrException();
                }
                else if (e.isVirgin()) {
                    this.metrics.peekMiss();
                }
                else {
                    this.metrics.peekHitNotFresh();
                }
                this.putValue(e, _value);
            }
            break;
        }
        return this.returnValue(_previousValue);
    }
    
    public V peekAndReplace(final K key, final V _value) {
        while (true) {
            final Entry e = this.lookupEntry(key);
            if (e == null) {
                break;
            }
            synchronized (e) {
                e.waitForProcessing();
                if (e.isGone()) {
                    this.metrics.goneSpin();
                    continue;
                }
                if (e.hasFreshData(this.clock)) {
                    final V _previousValue = (V)e.getValueOrException();
                    this.putValue(e, _value);
                    return this.returnValue(_previousValue);
                }
            }
            break;
        }
        this.metrics.peekMiss();
        return null;
    }
    
    protected final void putValue(final Entry e, final V _value) {
        if (!this.isUpdateTimeNeeded()) {
            this.insertOrUpdateAndCalculateExpiry((org.cache2k.core.Entry<K, V>)e, _value, 0L, 0L, 0L, (byte)2);
        }
        else {
            final long t = this.clock.millis();
            this.insertOrUpdateAndCalculateExpiry((org.cache2k.core.Entry<K, V>)e, _value, t, t, t, (byte)2);
        }
    }
    
    public boolean replace(final K key, final V _newValue) {
        return this.replace(key, false, null, _newValue);
    }
    
    public boolean replaceIfEquals(final K key, final V _oldValue, final V _newValue) {
        return this.replace(key, true, _oldValue, _newValue);
    }
    
    protected boolean replace(final K key, final boolean _compare, final V _oldValue, final V _newValue) {
        final Entry e = this.lookupEntry(key);
        if (e == null) {
            this.metrics.peekMiss();
            return false;
        }
        synchronized (e) {
            e.waitForProcessing();
            if (e.isGone() || !e.hasFreshData(this.clock)) {
                return false;
            }
            if (_compare && !e.equalsValue((Object)_oldValue)) {
                return false;
            }
            this.putValue(e, _newValue);
        }
        return true;
    }
    
    protected final Entry<K, V> peekEntryInternal(final K key) {
        final int hc = modifiedHash(key.hashCode());
        return this.peekEntryInternal(key, hc, this.extractIntKeyValue(key, hc));
    }
    
    protected final Entry<K, V> peekEntryInternal(final K key, final int hc, final int val) {
        final Entry e = this.lookupEntry(key, hc, val);
        if (e == null) {
            this.metrics.peekMiss();
            return null;
        }
        if (e.hasFreshData(this.clock)) {
            return (Entry<K, V>)e;
        }
        this.metrics.peekHitNotFresh();
        return null;
    }
    
    public boolean containsKey(final K key) {
        final Entry e = this.lookupEntry(key);
        if (e != null) {
            this.metrics.heapHitButNoRead();
            return e.hasFreshData(this.clock);
        }
        return false;
    }
    
    public V peek(final K key) {
        final Entry<K, V> e = this.peekEntryInternal(key);
        if (e != null) {
            return this.returnValue(e);
        }
        return null;
    }
    
    public CacheEntry<K, V> peekEntry(final K key) {
        return this.returnEntry((ExaminationEntry<K, V>)this.peekEntryInternal(key));
    }
    
    public V computeIfAbsent(final K key, final Callable<V> callable) {
        while (true) {
            final Entry<K, V> e = this.lookupOrNewEntry(key);
            if (e.hasFreshData(this.clock)) {
                return this.returnValue(e);
            }
            synchronized (e) {
                e.waitForProcessing();
                if (e.hasFreshData(this.clock)) {
                    return this.returnValue(e);
                }
                if (e.isGone()) {
                    this.metrics.goneSpin();
                    continue;
                }
                e.startProcessing(6);
            }
            this.metrics.peekMiss();
            boolean _finished = false;
            long t = 0L;
            long t2 = 0L;
            try {
                V _value;
                if (!this.isUpdateTimeNeeded()) {
                    _value = callable.call();
                }
                else {
                    t2 = this.clock.millis();
                    _value = callable.call();
                    if (!this.metrics.isDisabled()) {
                        t = this.clock.millis();
                    }
                }
                synchronized (e) {
                    this.insertOrUpdateAndCalculateExpiry(e, _value, t2, t, t2, (byte)2);
                    e.processingDone();
                }
                _finished = true;
            }
            catch (RuntimeException ex) {
                throw ex;
            }
            catch (Exception ex2) {
                throw new CacheLoaderException(ex2);
            }
            finally {
                e.ensureAbort(_finished);
            }
            return this.returnValue(e);
        }
    }
    
    public boolean putIfAbsent(final K key, final V value) {
        while (true) {
            final Entry e = this.lookupOrNewEntry(key);
            synchronized (e) {
                e.waitForProcessing();
                if (e.isGone()) {
                    this.metrics.goneSpin();
                }
                else {
                    if (e.hasFreshData(this.clock)) {
                        return false;
                    }
                    this.metrics.peekMiss();
                    this.putValue(e, value);
                    return true;
                }
            }
        }
    }
    
    public void put(final K key, final V value) {
        while (true) {
            final Entry e = this.lookupOrNewEntry(key);
            synchronized (e) {
                e.waitForProcessing();
                if (e.isGone()) {
                    this.metrics.goneSpin();
                    continue;
                }
                if (!e.isVirgin()) {
                    this.metrics.heapHitButNoRead();
                }
                this.putValue(e, value);
            }
            break;
        }
    }
    
    public boolean containsAndRemove(final K key) {
        final Entry e = this.lookupEntryNoHitRecord(key);
        if (e == null) {
            return false;
        }
        synchronized (e) {
            e.waitForProcessing();
            if (e.isGone()) {
                return false;
            }
            final boolean f = e.hasFreshData(this.clock);
            this.removeEntry(e);
            return f;
        }
    }
    
    public boolean removeIfEquals(final K key, final V _value) {
        final Entry e = this.lookupEntry(key);
        if (e == null) {
            this.metrics.peekMiss();
            return false;
        }
        synchronized (e) {
            e.waitForProcessing();
            if (e.isGone()) {
                this.metrics.peekMiss();
                return false;
            }
            final boolean f = e.hasFreshData(this.clock);
            if (!f) {
                this.metrics.peekHitNotFresh();
                return false;
            }
            if (!e.equalsValue((Object)_value)) {
                return false;
            }
            this.removeEntry(e);
            return f;
        }
    }
    
    public void remove(final K key) {
        this.containsAndRemove(key);
    }
    
    public V peekAndRemove(final K key) {
        final Entry e = this.lookupEntry(key);
        if (e == null) {
            this.metrics.peekMiss();
            return null;
        }
        synchronized (e) {
            e.waitForProcessing();
            if (e.isGone()) {
                this.metrics.peekMiss();
                return null;
            }
            V _value = null;
            final boolean f = e.hasFreshData(this.clock);
            if (f) {
                _value = (V)e.getValueOrException();
            }
            else {
                this.metrics.peekHitNotFresh();
            }
            this.removeEntry(e);
            return this.returnValue(_value);
        }
    }
    
    public Executor getPrefetchExecutor() {
        return this.prefetchExecutor;
    }
    
    public void prefetch(final K key) {
        if (this.loader == null) {
            return;
        }
        final Entry<K, V> e = this.lookupEntryNoHitRecord(key);
        if (e != null && e.hasFreshData(this.clock)) {
            return;
        }
        try {
            this.getPrefetchExecutor().execute(new RunWithCatch(this) {
                public void action() {
                    HeapCache.this.getEntryInternal(key);
                }
            });
        }
        catch (RejectedExecutionException ex) {}
    }
    
    public void prefetchAll(final Iterable<? extends K> _keys, final CacheOperationCompletionListener l) {
        final CacheOperationCompletionListener _listener = (l != null) ? l : HeapCache.DUMMY_LOAD_COMPLETED_LISTENER;
        if (this.loader == null) {
            _listener.onCompleted();
            return;
        }
        final Set<K> _keysToLoad = this.checkAllPresent(_keys);
        final AtomicInteger _count = new AtomicInteger(2);
        try {
            for (final K key : _keysToLoad) {
                final K k = key;
                final Runnable r = new RunWithCatch(this) {
                    public void action() {
                        try {
                            HeapCache.this.getEntryInternal(key);
                        }
                        finally {
                            if (_count.decrementAndGet() == 0) {
                                _listener.onCompleted();
                            }
                        }
                    }
                };
                try {
                    this.getPrefetchExecutor().execute(r);
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
    
    public void loadAll(final Iterable<? extends K> _keys, final CacheOperationCompletionListener l) {
        this.checkLoaderPresent();
        final CacheOperationCompletionListener _listener = (l != null) ? l : HeapCache.DUMMY_LOAD_COMPLETED_LISTENER;
        final Set<K> _keysToLoad = this.checkAllPresent(_keys);
        if (_keysToLoad.isEmpty()) {
            _listener.onCompleted();
            return;
        }
        final AtomicInteger _countDown = new AtomicInteger(_keysToLoad.size());
        for (final K key : _keysToLoad) {
            final K k = key;
            final Runnable r = new RunWithCatch(this) {
                public void action() {
                    try {
                        HeapCache.this.getEntryInternal(key);
                    }
                    finally {
                        if (_countDown.decrementAndGet() == 0) {
                            _listener.onCompleted();
                        }
                    }
                }
            };
            try {
                this.loaderExecutor.execute(r);
            }
            catch (RejectedExecutionException ex) {
                r.run();
            }
        }
    }
    
    public void reloadAll(final Iterable<? extends K> _keys, final CacheOperationCompletionListener l) {
        this.checkLoaderPresent();
        final CacheOperationCompletionListener _listener = (l != null) ? l : HeapCache.DUMMY_LOAD_COMPLETED_LISTENER;
        final Set<K> _keySet = this.generateKeySet(_keys);
        final AtomicInteger _countDown = new AtomicInteger(_keySet.size());
        for (final K key : _keySet) {
            final K k = key;
            final Runnable r = new RunWithCatch(this) {
                public void action() {
                    try {
                        HeapCache.this.loadAndReplace(key);
                    }
                    finally {
                        if (_countDown.decrementAndGet() == 0) {
                            _listener.onCompleted();
                        }
                    }
                }
            };
            try {
                this.loaderExecutor.execute(r);
            }
            catch (RejectedExecutionException ex) {
                r.run();
            }
        }
    }
    
    public Set<K> generateKeySet(final Iterable<? extends K> _keys) {
        final Set<K> _keySet = new HashSet<K>();
        for (final K k : _keys) {
            _keySet.add(k);
        }
        return _keySet;
    }
    
    public Set<K> checkAllPresent(final Iterable<? extends K> keys) {
        final Set<K> _keysToLoad = new HashSet<K>();
        for (final K k : keys) {
            final Entry<K, V> e = this.lookupEntryNoHitRecord(k);
            if (e == null || !e.hasFreshData(this.clock)) {
                _keysToLoad.add(k);
            }
        }
        return _keysToLoad;
    }
    
    protected void loadAndReplace(final K key) {
        Entry e;
        while (true) {
            e = this.lookupOrNewEntry(key);
            synchronized (e) {
                e.waitForProcessing();
                if (e.isGone()) {
                    this.metrics.goneSpin();
                    continue;
                }
                e.startProcessing(4);
            }
            break;
        }
        boolean _finished = false;
        try {
            this.load((org.cache2k.core.Entry<K, V>)e);
            _finished = true;
        }
        finally {
            e.ensureAbort(_finished);
        }
    }
    
    protected Entry<K, V> lookupOrNewEntry(final K key) {
        final int hc = modifiedHash(key.hashCode());
        return this.lookupOrNewEntry(key, hc, this.extractIntKeyValue(key, hc));
    }
    
    protected Entry<K, V> lookupOrNewEntry(final K key, final int hc, final int val) {
        final Entry e = this.lookupEntry(key, hc, val);
        if (e == null) {
            return this.insertNewEntry(key, hc, val);
        }
        return (Entry<K, V>)e;
    }
    
    protected Entry<K, V> lookupOrNewEntryNoHitRecord(final K key) {
        final int hc = modifiedHash(key.hashCode());
        Entry e = this.lookupEntryNoHitRecord(key, hc, this.extractIntKeyValue(key, hc));
        if (e == null) {
            e = this.insertNewEntry(key, hc, this.extractIntKeyValue(key, hc));
        }
        return (Entry<K, V>)e;
    }
    
    protected V returnValue(final V v) {
        if (v instanceof ExceptionWrapper) {
            final ExceptionWrapper<K> w = (ExceptionWrapper<K>)v;
            throw this.exceptionPropagator.propagateException((K)w.getKey(), (ExceptionInformation)w);
        }
        return v;
    }
    
    protected V returnValue(final Entry<K, V> e) {
        final V v = (V)e.getValueOrException();
        if (v instanceof ExceptionWrapper) {
            final ExceptionWrapper<K> w = (ExceptionWrapper<K>)v;
            throw this.exceptionPropagator.propagateException((K)w.getKey(), (ExceptionInformation)w);
        }
        return v;
    }
    
    protected Entry<K, V> lookupEntry(final K key) {
        final int hc = modifiedHash(key.hashCode());
        return this.lookupEntry(key, hc, this.extractIntKeyValue(key, hc));
    }
    
    protected Entry<K, V> lookupEntryNoHitRecord(final K key) {
        final int hc = modifiedHash(key.hashCode());
        return this.lookupEntryNoHitRecord(key, hc, this.extractIntKeyValue(key, hc));
    }
    
    protected final Entry<K, V> lookupEntry(final K key, final int hc, final int val) {
        final Entry e = this.lookupEntryNoHitRecord(key, hc, val);
        if (e != null) {
            this.recordHit(e);
            return (Entry<K, V>)e;
        }
        return null;
    }
    
    protected final Entry<K, V> lookupEntryNoHitRecord(final K key, final int hc, final int val) {
        return this.hash.lookup(this.extractIntKeyObj(key), hc, val);
    }
    
    protected Entry<K, V> insertNewEntry(final K key, final int hc, final int val) {
        final Entry<K, V> e = new Entry<>(this.extractIntKeyObj(key), val);
        this.eviction.evictEventually(hc);
        final OptimisticLock l = this.hash.getSegmentLock(hc);
        final long _stamp = l.writeLock();
        Entry<K, V> e2;
        try {
            e2 = this.hash.insertWithinLock(e, hc, val);
            if (e == e2) {
                this.eviction.submitWithoutEviction(e);
            }
        }
        finally {
            l.unlockWrite(_stamp);
        }
        this.hash.checkExpand(hc);
        return e2;
    }
    
    public void removeEntryForEviction(final Entry<K, V> e) {
        final boolean f = this.hash.remove((Entry)e);
        this.checkForHashCodeChange(e);
        this.timing.cancelExpiryTimer(e);
        e.setGone();
    }
    
    private void checkForHashCodeChange(final Entry<K, V> e) {
        final K key = this.extractKeyObj(e);
        if (this.extractIntKeyValue(key, modifiedHash(key.hashCode())) != e.hashCode) {
            if (this.keyMutationCnt == 0L) {
                this.getLog().warn("Key mismatch! Key hashcode changed! keyClass=" + e.getKey().getClass().getName());
                try {
                    final String s = e.getKey().toString();
                    if (s != null) {
                        this.getLog().warn("Key mismatch! key.toString(): " + s);
                    }
                }
                catch (Throwable t) {
                    this.getLog().warn("Key mismatch! key.toString() threw exception", t);
                }
            }
            ++this.keyMutationCnt;
        }
    }
    
    protected void load(final Entry<K, V> e) {
        long refreshTime;
        final long t0 = refreshTime = (this.isUpdateTimeNeeded() ? this.clock.millis() : 0L);
        if (e.getNextRefreshTime() == 6L && this.entryInRefreshProbationAccessed(e, t0)) {
            return;
        }
        V v;
        try {
            this.checkLoaderPresent();
            if (e.isVirgin()) {
                v = this.loader.load(this.extractKeyObj(e), t0, null);
            }
            else {
                v = this.loader.load(this.extractKeyObj(e), t0, (org.cache2k.CacheEntry<K, V>)e);
            }
            if (v instanceof RefreshedTimeWrapper) {
                final RefreshedTimeWrapper wr = RefreshedTimeWrapper.class.cast(v);
                refreshTime = wr.getRefreshTime();
                v = (V)wr.getValue();
            }
        }
        catch (Throwable _ouch) {
            long t2 = t0;
            if (!this.metrics.isDisabled() && this.isUpdateTimeNeeded()) {
                t2 = this.clock.millis();
            }
            this.loadGotException(e, t0, t2, _ouch);
            return;
        }
        long t3 = t0;
        if (!this.metrics.isDisabled() && this.isUpdateTimeNeeded()) {
            t3 = this.clock.millis();
        }
        this.insertOrUpdateAndCalculateExpiry(e, v, t0, t3, refreshTime, (byte)1);
    }
    
    private boolean entryInRefreshProbationAccessed(final Entry<K, V> e, final long now) {
        final long nrt = e.getRefreshProbationNextRefreshTime();
        if (nrt > now) {
            this.reviveRefreshedEntry(e, nrt);
            return true;
        }
        return false;
    }
    
    private void reviveRefreshedEntry(final Entry<K, V> e, final long _nrt) {
        synchronized (e) {
            this.metrics.refreshedHit();
            this.finishLoadOrEviction(e, _nrt);
        }
    }
    
    private void loadGotException(final Entry<K, V> e, final long t0, final long t, final Throwable _wrappedException) {
        final ExceptionWrapper<K> _value = new ExceptionWrapper<>(this.extractKeyObj(e), _wrappedException, t0, e);
        long _nextRefreshTime = 0L;
        boolean _suppressException = false;
        try {
            if ((e.isDataValid() || e.isExpired()) && e.getException() == null) {
                _nextRefreshTime = this.timing.suppressExceptionUntil(e, _value);
            }
            if (_nextRefreshTime > t0) {
                _suppressException = true;
            }
            else {
                _nextRefreshTime = this.timing.cacheExceptionUntil(e, _value);
            }
        }
        catch (Exception ex) {
            this.resiliencePolicyException(e, t0, t, new ResiliencePolicyException(ex));
            return;
        }
        synchronized (e) {
            this.insertUpdateStats(e, (V) _value, t0, t, (byte)1, _nextRefreshTime, _suppressException);
            if (_suppressException) {
                e.setSuppressedLoadExceptionInformation(_value);
            }
            else {
                if (this.isRecordRefreshTime()) {
                    e.setRefreshTime(t0);
                }
                e.setValueOrException((V) _value);
            }
            _value.setUntil(Math.abs(_nextRefreshTime));
            this.finishLoadOrEviction(e, _nextRefreshTime);
        }
    }
    
    private void resiliencePolicyException(final Entry<K, V> e, final long t0, final long t, final Throwable _exception) {
        final ExceptionWrapper<K> _value = new ExceptionWrapper<>(this.extractKeyObj(e), _exception, t0, e);
        this.insert(e, (V) _value, t0, t, t0, (byte)1, 0L);
    }
    
    private void checkLoaderPresent() {
        if (!this.isLoaderPresent()) {
            throw new UnsupportedOperationException("loader not set");
        }
    }
    
    public boolean isLoaderPresent() {
        return this.loader != null;
    }
    
    public boolean isWeigherPresent() {
        return this.eviction.isWeigherPresent();
    }
    
    protected final void insertOrUpdateAndCalculateExpiry(final Entry<K, V> e, final V v, final long t0, final long t, final long _refreshTime, final byte _updateStatistics) {
        long _nextRefreshTime;
        try {
            _nextRefreshTime = this.timing.calculateNextRefreshTime(e, v, _refreshTime);
        }
        catch (Exception ex) {
            final RuntimeException _wrappedException = (RuntimeException)new ExpiryPolicyException((Throwable)ex);
            if (_updateStatistics == 1) {
                this.loadGotException(e, t0, t, _wrappedException);
                return;
            }
            this.insertUpdateStats(e, v, t0, t, _updateStatistics, Long.MAX_VALUE, false);
            throw _wrappedException;
        }
        this.insert(e, v, t0, t, _refreshTime, _updateStatistics, _nextRefreshTime);
    }
    
    public RuntimeException returnNullValueDetectedException() {
        return new NullPointerException("null value not allowed");
    }
    
    protected final void insert(final Entry<K, V> e, final V _value, final long t0, final long t, final long _refreshTime, final byte _updateStatistics, final long _nextRefreshTime) {
        if (_updateStatistics == 1) {
            if (_value == null && this.isRejectNullValues() && _nextRefreshTime != 0L) {
                this.loadGotException(e, t0, t, this.returnNullValueDetectedException());
                return;
            }
            synchronized (e) {
                if (this.isRecordRefreshTime()) {
                    e.setRefreshTime(_refreshTime);
                }
                this.insertUpdateStats(e, _value, t0, t, _updateStatistics, _nextRefreshTime, false);
                e.setValueOrException(_value);
                e.resetSuppressedLoadExceptionInformation();
                this.finishLoadOrEviction(e, _nextRefreshTime);
            }
        }
        else {
            if (_value == null && this.isRejectNullValues()) {
                throw this.returnNullValueDetectedException();
            }
            if (this.isRecordRefreshTime()) {
                e.setRefreshTime(_refreshTime);
            }
            e.setValueOrException(_value);
            e.resetSuppressedLoadExceptionInformation();
            this.insertUpdateStats(e, _value, t0, t, _updateStatistics, _nextRefreshTime, false);
            this.restartTimer(e, _nextRefreshTime);
        }
    }
    
    private void insertUpdateStats(final Entry<K, V> e, final V _value, final long t0, final long t, final byte _updateStatistics, final long _nextRefreshTime, final boolean _suppressException) {
        if (_updateStatistics == 1) {
            if (_suppressException) {
                this.metrics.suppressedException();
            }
            else if (_value instanceof ExceptionWrapper) {
                this.metrics.loadException();
            }
            final long _millis = t - t0;
            if (e.isGettingRefresh()) {
                this.metrics.refresh(_millis);
            }
            else if (e.isVirgin()) {
                this.metrics.load(_millis);
            }
            else {
                this.metrics.reload(_millis);
            }
        }
        else if (_nextRefreshTime != 0L) {
            this.metrics.putNewEntry();
        }
    }
    
    public void timerEventRefresh(final Entry<K, V> e) {
        this.metrics.timerEvent();
        synchronized (e) {
            if (e.isGone()) {
                return;
            }
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    HeapCache.this.refreshEntry(e);
                }
            };
            try {
                this.loaderExecutor.execute(r);
            }
            catch (RejectedExecutionException ex) {
                this.metrics.refreshFailed();
                this.expireOrScheduleFinalExpireEvent(e);
            }
        }
    }
    
    private void refreshEntry(final Entry<K, V> e) {
        synchronized (e) {
            e.waitForProcessing();
            if (e.isGone()) {
                return;
            }
            e.startProcessing(7);
        }
        boolean _finished = false;
        try {
            this.load(e);
            _finished = true;
        }
        catch (CacheClosedException ignored) {}
        catch (Throwable ex) {
            this.logAndCountInternalException("Refresh exception", ex);
            try {
                synchronized (e) {
                    this.expireEntry(e);
                }
            }
            catch (CacheClosedException ignored) {}
        }
        finally {
            e.ensureAbort(_finished);
        }
    }
    
    public void startRefreshProbationTimer(final Entry<K, V> e, final long _nextRefreshTime) {
        final boolean _expired = this.timing.startRefreshProbationTimer(e, _nextRefreshTime);
        if (_expired) {
            this.expireAndRemoveEventually(e);
        }
    }
    
    public void timerEventProbationTerminated(final Entry<K, V> e) {
        this.metrics.timerEvent();
        synchronized (e) {
            this.expireEntry(e);
        }
    }
    
    public void logAndCountInternalException(final String _text, final Throwable _exception) {
        synchronized (this.lock) {
            ++this.internalExceptionCnt;
        }
        this.getLog().warn(_text, _exception);
    }
    
    public void timerEventExpireEntry(final Entry<K, V> e) {
        this.metrics.timerEvent();
        synchronized (e) {
            this.expireOrScheduleFinalExpireEvent(e);
        }
    }
    
    public void expireOrScheduleFinalExpireEvent(final Entry<K, V> e) {
        final long nrt = e.getNextRefreshTime();
        if (e.isGone() || e.isExpired()) {
            return;
        }
        final long t = this.clock.millis();
        if (t >= Math.abs(nrt)) {
            try {
                this.expireEntry(e);
            }
            catch (CacheClosedException ex) {}
        }
        else {
            if (nrt <= 0L) {
                return;
            }
            this.timing.scheduleFinalTimerForSharpExpiry(e);
            e.setNextRefreshTime(-nrt);
        }
    }
    
    protected void expireEntry(final Entry e) {
        if (e.isGone() || e.isExpired()) {
            return;
        }
        e.setExpiredState();
        this.expireAndRemoveEventually(e);
    }
    
    private void expireAndRemoveEventually(final Entry e) {
        if (this.isKeepAfterExpired() || e.isProcessing()) {
            this.metrics.expiredKept();
        }
        else {
            this.removeEntry(e);
        }
    }
    
    private void expireAndRemoveEventuallyAfterProcessing(final Entry e) {
        if (this.isKeepAfterExpired()) {
            this.metrics.expiredKept();
        }
        else {
            this.removeEntry(e);
        }
    }
    
    public final ConcurrentEntryIterator<K, V> iterateAllHeapEntries() {
        return (ConcurrentEntryIterator<K, V>)new ConcurrentEntryIterator(this);
    }
    
    public Map<K, V> getAll(final Iterable<? extends K> _inputKeys) {
        final Map<K, ExaminationEntry<K, V>> map = new HashMap<K, ExaminationEntry<K, V>>();
        for (final K k : _inputKeys) {
            final Entry<K, V> e = (Entry<K, V>)this.getEntryInternal(k);
            if (e != null) {
                map.put(this.extractKeyObj(e), (ExaminationEntry<K, V>)ReadOnlyCacheEntry.of((org.cache2k.CacheEntry<Object, Object>)e));
            }
        }
        return this.convertValueMap(map);
    }
    
    public Map<K, V> convertValueMap(final Map<K, ExaminationEntry<K, V>> _map) {
        return new MapValueConverterProxy<K, V, ExaminationEntry<K, V>>(_map) {
            @Override
            protected V convert(final ExaminationEntry<K, V> v) {
                return HeapCache.this.returnValue(v.getValueOrException());
            }
        };
    }
    
    public Map<K, V> convertCacheEntry2ValueMap(final Map<K, CacheEntry<K, V>> _map) {
        return new MapValueConverterProxy<K, V, CacheEntry<K, V>>(_map) {
            @Override
            protected V convert(final CacheEntry<K, V> v) {
                return (V)v.getValue();
            }
        };
    }
    
    public Map<K, V> peekAll(final Iterable<? extends K> _inputKeys) {
        final Map<K, ExaminationEntry<K, V>> map = new HashMap<K, ExaminationEntry<K, V>>();
        for (final K k : _inputKeys) {
            final ExaminationEntry<K, V> e = (ExaminationEntry<K, V>)this.peekEntryInternal(k);
            if (e != null) {
                map.put(k, e);
            }
        }
        return this.convertValueMap(map);
    }
    
    public void putAll(final Map<? extends K, ? extends V> valueMap) {
        for (final Map.Entry<? extends K, ? extends V> e : valueMap.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }
    
    Operations<K, V> spec() {
        return (Operations<K, V>)Operations.SINGLETON;
    }
    
    protected <R> EntryAction<K, V, R> createEntryAction(final K key, final Entry<K, V> e, final Semantic<K, V, R> op) {
        return new EntryAction<K, V, R>(this, this, op, key, e) {
            protected TimingHandler<K, V> timing() {
                return HeapCache.this.timing;
            }
            
            public Executor getLoaderExecutor() {
                return null;
            }
        };
    }
    
    public <R> R invoke(final K key, final EntryProcessor<K, V, R> entryProcessor) {
        return (R)this.execute(key, this.spec().invoke(key, this.loader != null, entryProcessor));
    }
    
    public void expireAt(final K key, final long _millis) {
        this.execute(key, this.spec().expire(key, _millis));
    }
    
    public final long getLocalSize() {
        return this.hash.getSize();
    }
    
    public final int getTotalEntryCount() {
        return this.executeWithGlobalLock(() -> (int)HeapCache.this.getLocalSize());
    }
    
    protected IntegrityState getIntegrityState() {
        final EvictionMetrics em = this.eviction.getMetrics();
        final IntegrityState is = new IntegrityState().checkEquals("hash.getSize() == hash.calcEntryCount()", this.hash.getSize(), this.hash.calcEntryCount());
        if (em.getEvictionRunningCount() > 0) {
            is.check("eviction running: hash.getSize() == eviction.getSize()", true).check("eviction running: newEntryCnt == hash.getSize() + evictedCnt ....", true);
        }
        else {
            is.checkEquals("hash.getSize() == eviction.getSize()", this.getLocalSize(), em.getSize()).checkEquals("newEntryCnt == hash.getSize() + evictedCnt + expiredRemoveCnt + removeCnt + clearedCnt + virginRemovedCnt", em.getNewEntryCount(), this.getLocalSize() + em.getEvictedCount() + em.getExpiredRemovedCount() + em.getRemovedCount() + this.clearRemovedCnt + em.getVirginRemovedCount());
        }
        this.eviction.checkIntegrity(is);
        return is;
    }
    
    public final void checkIntegrity() {
        this.executeWithGlobalLock((Job<Void>) () -> {
            final IntegrityState is = HeapCache.this.getIntegrityState();
            if (is.getStateFlags() > 0L) {
                throw new Error("cache2k integrity error: " + is.getStateDescriptor() + ", " + is.getFailingChecks() + ", " + HeapCache.this.generateInfoUnderLock((InternalCache)HeapCache.this, HeapCache.this.clock.millis()).toString());
            }
            return null;
        });
    }
    
    public final InternalCacheInfo getInfo() {
        return this.getInfo((InternalCache)this);
    }
    
    public final InternalCacheInfo getLatestInfo() {
        return this.getLatestInfo((InternalCache)this);
    }
    
    public final InternalCacheInfo getInfo(final InternalCache _userCache) {
        synchronized (this.lock) {
            final long t = this.clock.millis();
            if (this.info != null && this.info.getInfoCreatedTime() + this.info.getInfoCreationDeltaMs() * HeapCache.TUNABLE.minimumStatisticsCreationTimeDeltaFactor + HeapCache.TUNABLE.minimumStatisticsCreationDeltaMillis > t) {
                return (InternalCacheInfo)this.info;
            }
            this.info = this.generateInfo(_userCache, t);
        }
        return this.info;
    }


    public final InternalCacheInfo getLatestInfo(final InternalCache _userCache) {
        return this.generateInfo(_userCache, this.clock.millis());
    }


    private CacheBaseInfo generateInfo(final InternalCache _userCache, final long t) {
        return this.executeWithGlobalLock(() -> HeapCache.this.generateInfoUnderLock(_userCache, t));
    }


    private CacheBaseInfo generateInfoUnderLock(final InternalCache _userCache, final long t) {
        (this.info = new CacheBaseInfo(this, _userCache, t)).setInfoCreationDeltaMs((int)(this.clock.millis() - t));
        return this.info;
    }
    
    public CommonMetrics getCommonMetrics() {
        return (CommonMetrics)this.metrics;
    }
    
    public <T> T executeWithGlobalLock(final Job<T> job) {
        return this.executeWithGlobalLock(job, true);
    }
    
    private <T> T executeWithGlobalLock(final Job<T> job, final boolean _checkClosed) {
        synchronized (this.lock) {
            if (_checkClosed) {
                this.checkClosed();
            }
            this.eviction.stop();
            try {
                T _result = (T)this.hash.runTotalLocked((Job) () -> {
                    if (_checkClosed) {
                        HeapCache.this.checkClosed();
                    }
                    final boolean f = HeapCache.this.eviction.drain();
                    if (f) {
                        return (T)HeapCache.RESTART_AFTER_EVICTION;
                    }
                    return (T)HeapCache.this.eviction.runLocked((Job)new Job<T>() {
                        public T call() {
                            return (T)job.call();
                        }
                    });
                });
                if (_result == HeapCache.RESTART_AFTER_EVICTION) {
                    this.eviction.evictEventually();
                    _result = (T)this.hash.runTotalLocked((Job) () -> {
                        if (_checkClosed) {
                            HeapCache.this.checkClosed();
                        }
                        HeapCache.this.eviction.drain();
                        return (T)HeapCache.this.eviction.runLocked((Job)new Job<T>() {
                            public T call() {
                                return (T)job.call();
                            }
                        });
                    });
                }
                return _result;
            }
            finally {
                this.eviction.start();
            }
        }
    }
    
    public final InternalClock getClock() {
        return this.clock;
    }
    
    public CacheManager getCacheManager() {
        return (CacheManager)this.manager;
    }
    
    public static int modifiedHash(final int h) {
        return h ^ h >>> 16;
    }
    
    public int extractIntKeyValue(final K key, final int hc) {
        return hc;
    }
    
    public K extractIntKeyObj(final K key) {
        return key;
    }
    
    public int extractModifiedHash(final Entry e) {
        return e.hashCode;
    }
    
    public K extractKeyObj(final Entry<K, V> e) {
        return (K)e.getKeyObj();
    }
    
    public Hash2<K, V> createHashTable() {
        return (Hash2<K, V>)new Hash2(this);
    }
    
    static {
        DUMMY_LOAD_COMPLETED_LISTENER = (CacheOperationCompletionListener)new CacheOperationCompletionListener() {
            public void onCompleted() {
            }
            
            public void onException(final Throwable _exception) {
            }
        };
        HeapCache.cacheCnt = 0;
        TUNABLE = TunableFactory.get(Tunable.class);
        DEFAULT_EXCEPTION_PROPAGATOR = HeapCache.TUNABLE.exceptionPropagator;
        RESTART_AFTER_EVICTION = new Object();
    }
    
    private class LazyLoaderExecutor implements Executor
    {
        @Override
        public void execute(final Runnable _command) {
            synchronized (HeapCache.this.lock) {
                HeapCache.this.checkClosed();
                if (HeapCache.this.loaderExecutor == this) {
                    final int _threadCount = Runtime.getRuntime().availableProcessors() * HeapCache.TUNABLE.loaderThreadCountCpuFactor;
                    HeapCache.this.loaderExecutor = HeapCache.this.provideDefaultLoaderExecutor(_threadCount);
                }
                HeapCache.this.loaderExecutor.execute(_command);
            }
        }
    }
    
    private class LazyPrefetchExecutor implements Executor {
        @Override
        public void execute(final Runnable _command) {
            synchronized (HeapCache.this.lock) {
                HeapCache.this.checkClosed();
                HeapCache.this.loaderExecutor.execute(_command);
                HeapCache.this.prefetchExecutor = HeapCache.this.loaderExecutor;
            }
        }
    }
    
    static class IteratorFilterEntry2Entry<K, V> implements Iterator<CacheEntry<K, V>> {
        HeapCache<K, V> cache;
        Iterator<Entry<K, V>> iterator;
        Entry entry;
        CacheEntry<K, V> lastEntry;
        boolean filter;
        
        IteratorFilterEntry2Entry(final HeapCache<K, V> c, final Iterator<Entry<K, V>> it, final boolean _filter) {
            this.cache = c;
            this.iterator = it;
            this.filter = _filter;
        }
        
        @Override
        public boolean hasNext() {
            if (this.entry != null) {
                return true;
            }
            if (this.iterator == null) {
                return false;
            }
            while (this.iterator.hasNext()) {
                final Entry e = this.iterator.next();
                if (!this.filter) {
                    this.entry = e;
                    return true;
                }
                if (e.hasFreshData(this.cache.getClock())) {
                    this.entry = e;
                    return true;
                }
            }
            this.entry = null;
            return false;
        }
        
        @Override
        public CacheEntry<K, V> next() {
            if (this.entry == null && !this.hasNext()) {
                throw new NoSuchElementException("not available");
            }
            this.lastEntry = this.cache.returnEntry((ExaminationEntry<K, V>)this.entry);
            this.entry = null;
            return this.lastEntry;
        }
        
        @Override
        public void remove() {
            if (this.lastEntry == null) {
                throw new IllegalStateException("Unable to remove, hasNext() / next() not called or end of iteration reached");
            }
            this.cache.remove((K)this.lastEntry.getKey());
        }
    }
    
    abstract static class BaseCacheEntry<K, V> extends AbstractCacheEntry<K, V>
    {
        public Throwable getException() {
            return null;
        }
        
        public String toString() {
            return "CacheEntry(key=" + this.getKey() + ((this.getException() != null) ? (", exception=" + this.getException() + ", ") : (", value=" + this.getValue()));
        }
    }
    
    public abstract static class RunWithCatch implements Runnable
    {
        InternalCache cache;
        
        public RunWithCatch(final InternalCache _cache) {
            this.cache = _cache;
        }
        
        protected abstract void action();
        
        @Override
        public final void run() {
            if (this.cache.isClosed()) {
                return;
            }
            try {
                this.action();
            }
            catch (CacheClosedException ex) {}
            catch (Throwable t) {
                this.cache.logAndCountInternalException("Loader thread exception (" + Thread.currentThread().getName() + ")", t);
            }
        }
    }
    
    public static class Tunable extends TunableConstants
    {
        public int initialHashSize;
        public int hashLoadPercent;
        public long sharpExpirySafetyGapMillis;
        public int minimumStatisticsCreationDeltaMillis;
        public int minimumStatisticsCreationTimeDeltaFactor;
        public ThreadFactoryProvider threadFactoryProvider;
        public int loaderThreadCountCpuFactor;
        public StandardCommonMetricsFactory commonMetricsFactory;
        public ExceptionPropagator exceptionPropagator;
        public int hashQualityWarningThreshold;
        public int hashQualityErrorThreshold;
        public int segmentCountOverride;
        
        public Tunable() {
            this.initialHashSize = 64;
            this.hashLoadPercent = 64;
            this.sharpExpirySafetyGapMillis = 27127L;
            this.minimumStatisticsCreationDeltaMillis = 333;
            this.minimumStatisticsCreationTimeDeltaFactor = 123;
            this.threadFactoryProvider = (ThreadFactoryProvider)new DefaultThreadFactoryProvider();
            this.loaderThreadCountCpuFactor = 1;
            this.commonMetricsFactory = new StandardCommonMetricsFactory();
            this.exceptionPropagator = new StandardExceptionPropagator();
            this.hashQualityWarningThreshold = 20;
            this.hashQualityErrorThreshold = 5;
            this.segmentCountOverride = 0;
        }
    }
}
