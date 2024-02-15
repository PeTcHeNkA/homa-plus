//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import java.util.concurrent.atomic.*;
import org.cache2k.core.util.*;
import org.cache2k.integration.*;
import org.cache2k.configuration.*;
import org.cache2k.event.*;
import java.util.*;
import org.cache2k.*;
import java.util.concurrent.*;
import java.io.*;
import org.cache2k.core.operation.*;
import org.cache2k.core.event.*;

public class InternalCache2kBuilder<K, V>
{
    private static final AtomicLong DERIVED_NAME_COUNTER;
    private static final ThreadPoolExecutor DEFAULT_ASYNC_LISTENER_EXECUTOR;
    private CacheManagerImpl manager;
    private Cache2kConfiguration<K, V> config;
    
    public InternalCache2kBuilder(final Cache2kConfiguration<K, V> _config, final CacheManager _manager) {
        this.config = _config;
        this.manager = (CacheManagerImpl)((_manager == null) ? CacheManager.getInstance() : _manager);
    }
    
    private static boolean isBuilderClass(final String _className) {
        return Cache2kBuilder.class.getName().equals(_className);
    }
    
    private static String deriveNameFromStackTrace() {
        boolean _builderSeen = false;
        final Exception ex = new Exception();
        for (final StackTraceElement e : ex.getStackTrace()) {
            if (_builderSeen && !isBuilderClass(e.getClassName())) {
                String _methodName = e.getMethodName();
                if (_methodName.equals("<init>")) {
                    _methodName = "INIT";
                }
                if (_methodName.equals("<clinit>")) {
                    _methodName = "CLINIT";
                }
                return "_" + e.getClassName() + "." + _methodName + "-" + e.getLineNumber() + "-" + Long.toString(InternalCache2kBuilder.DERIVED_NAME_COUNTER.incrementAndGet(), 36);
            }
            _builderSeen = isBuilderClass(e.getClassName());
        }
        throw new IllegalArgumentException("name missing and automatic generation failed");
    }
    
    private void configureViaSettersDirect(final HeapCache<K, V> c) {
        if (this.config.getLoader() != null) {
            final Object obj = c.createCustomization(this.config.getLoader());
            if (obj instanceof CacheLoader) {
                final CacheLoader<K, V> _loader = (CacheLoader<K, V>)obj;
                c.setAdvancedLoader((AdvancedCacheLoader)new AdvancedCacheLoader<K, V>() {
                    @Override
                    public V load(final K key, final long currentTime, final CacheEntry<K, V> currentEntry) throws Exception {
                        return _loader.load(key);
                    }
                });
            }
            else {
                final FunctionalCacheLoader<K, V> _loader2 = (FunctionalCacheLoader<K, V>)obj;
                c.setAdvancedLoader((AdvancedCacheLoader)new AdvancedCacheLoader<K, V>() {
                    @Override
                    public V load(final K key, final long currentTime, final CacheEntry<K, V> currentEntry) throws Exception {
                        return _loader2.load(key);
                    }
                });
            }
        }
        if (this.config.getAdvancedLoader() != null) {
            final AdvancedCacheLoader<K, V> _loader3 = (AdvancedCacheLoader<K, V>)c.createCustomization(this.config.getAdvancedLoader());
            final AdvancedCacheLoader<K, V> _wrappedLoader = new WrappedAdvancedCacheLoader<K, V>(c, _loader3);
            c.setAdvancedLoader((AdvancedCacheLoader)_wrappedLoader);
        }
        if (this.config.getExceptionPropagator() != null) {
            c.setExceptionPropagator((ExceptionPropagator)c.createCustomization(this.config.getExceptionPropagator()));
        }
        c.setCacheConfig((Cache2kConfiguration)this.config);
    }
    
    private HeapCache<K, V> constructImplementationAndFillParameters(final Class<?> cls) {
        if (!HeapCache.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException("Specified impl not a cache" + cls.getName());
        }
        try {
            return (HeapCache<K, V>)cls.newInstance();
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Not able to instantiate cache implementation", e);
        }
    }
    
    public Cache<K, V> build() {
        Cache2kCoreProviderImpl.CACHE_CONFIGURATION_PROVIDER.augmentConfiguration((CacheManager)this.manager, this.config);
        return this.buildAsIs();
    }
    
    public Cache<K, V> buildAsIs() {
        if (this.config.getValueType() == null) {
            this.config.setValueType((Class)Object.class);
        }
        if (this.config.getKeyType() == null) {
            this.config.setKeyType((Class)Object.class);
        }
        if (this.config.getName() == null) {
            this.config.setName(deriveNameFromStackTrace());
        }
        this.checkConfiguration();
        Class<?> _implClass = HeapCache.class;
        final Class<?> _keyType = (Class<?>)this.config.getKeyType().getType();
        if (_keyType == Integer.class) {
            _implClass = IntHeapCache.class;
        }
        else if (_keyType == Long.class) {
            _implClass = LongHeapCache.class;
        }
        InternalCache<K, V> _cache = (InternalCache<K, V>)this.constructImplementationAndFillParameters(_implClass);
        InternalClock _timeReference = (InternalClock)_cache.createCustomization(this.config.getTimeReference());
        if (_timeReference == null) {
            _timeReference = ClockDefaultImpl.INSTANCE;
        }
        final HeapCache bc = (HeapCache)_cache;
        bc.setCacheManager(this.manager);
        if (this.config.hasCacheClosedListeners()) {
            bc.setCacheClosedListeners((Collection)this.config.getCacheClosedListeners());
        }
        this.configureViaSettersDirect((org.cache2k.core.HeapCache<K, V>)bc);
        bc.setClock(_timeReference);
        if (this.config.isRefreshAhead() && this.config.getAsyncLoader() == null && this.config.getLoader() == null && this.config.getAdvancedLoader() == null) {
            throw new IllegalArgumentException("refresh ahead enabled, but no loader defined");
        }
        final boolean _wrap = this.config.getWeigher() != null || this.config.hasListeners() || this.config.hasAsyncListeners() || this.config.getWriter() != null || this.config.getAsyncLoader() != null;
        WiredCache<K, V> wc = null;
        if (_wrap) {
            if (_keyType == Integer.class) {
                wc = (WiredCache<K, V>)new IntWiredCache();
            }
            else if (_keyType == Long.class) {
                wc = (WiredCache<K, V>)new LongWiredCache();
            }
            else {
                wc = new WiredCache<K, V>();
            }
            wc.heapCache = (org.cache2k.core.HeapCache<K, V>)bc;
            _cache = (InternalCache<K, V>)wc;
        }
        final String _name = this.manager.newCache((InternalCache)_cache, bc.getName());
        bc.setName(_name);
        if (_wrap) {
            wc.loader = (AdvancedCacheLoader<K, V>)bc.loader;
            wc.writer = (CacheWriter<K, V>)bc.createCustomization(this.config.getWriter());
            wc.asyncLoader = (AsyncCacheLoader<K, V>)bc.createCustomization(this.config.getAsyncLoader());
            final List<CacheEntryCreatedListener<K, V>> _syncCreatedListeners = new ArrayList<CacheEntryCreatedListener<K, V>>();
            final List<CacheEntryUpdatedListener<K, V>> _syncUpdatedListeners = new ArrayList<CacheEntryUpdatedListener<K, V>>();
            final List<CacheEntryRemovedListener<K, V>> _syncRemovedListeners = new ArrayList<CacheEntryRemovedListener<K, V>>();
            final List<CacheEntryExpiredListener<K, V>> _syncExpiredListeners = new ArrayList<CacheEntryExpiredListener<K, V>>();
            final List<CacheEntryEvictedListener<K, V>> _syncEvictedListeners = new ArrayList<CacheEntryEvictedListener<K, V>>();
            final List<CacheEntryExpiredListener<K, V>> _expiredListeners = new ArrayList<CacheEntryExpiredListener<K, V>>();
            if (this.config.hasListeners()) {
                for (final CustomizationSupplier<CacheEntryOperationListener<K, V>> f : this.config.getListeners()) {
                    final CacheEntryOperationListener<K, V> el = (CacheEntryOperationListener<K, V>)bc.createCustomization((CustomizationSupplier)f);
                    if (el instanceof CacheEntryCreatedListener) {
                        _syncCreatedListeners.add((CacheEntryCreatedListener)el);
                    }
                    if (el instanceof CacheEntryUpdatedListener) {
                        _syncUpdatedListeners.add((CacheEntryUpdatedListener)el);
                    }
                    if (el instanceof CacheEntryRemovedListener) {
                        _syncRemovedListeners.add((CacheEntryRemovedListener)el);
                    }
                    if (el instanceof CacheEntryExpiredListener) {
                        _expiredListeners.add((CacheEntryExpiredListener)el);
                    }
                    if (el instanceof CacheEntryEvictedListener) {
                        _syncEvictedListeners.add((CacheEntryEvictedListener)el);
                    }
                }
            }
            if (this.config.hasAsyncListeners() || !_expiredListeners.isEmpty()) {
                Executor _executor = InternalCache2kBuilder.DEFAULT_ASYNC_LISTENER_EXECUTOR;
                if (this.config.getAsyncListenerExecutor() != null) {
                    _executor = (Executor)_cache.createCustomization(this.config.getAsyncListenerExecutor());
                }
                final AsyncDispatcher<K> _asyncDispatcher = (AsyncDispatcher<K>)new AsyncDispatcher((InternalCache)wc, _executor);
                final List<CacheEntryCreatedListener<K, V>> cll = new ArrayList<CacheEntryCreatedListener<K, V>>();
                final List<CacheEntryUpdatedListener<K, V>> ull = new ArrayList<CacheEntryUpdatedListener<K, V>>();
                final List<CacheEntryRemovedListener<K, V>> rll = new ArrayList<CacheEntryRemovedListener<K, V>>();
                final List<CacheEntryExpiredListener<K, V>> ell = new ArrayList<CacheEntryExpiredListener<K, V>>();
                final List<CacheEntryEvictedListener<K, V>> evl = new ArrayList<CacheEntryEvictedListener<K, V>>();
                for (final CustomizationSupplier<CacheEntryOperationListener<K, V>> f2 : this.config.getAsyncListeners()) {
                    final CacheEntryOperationListener<K, V> el2 = (CacheEntryOperationListener<K, V>)bc.createCustomization((CustomizationSupplier)f2);
                    if (el2 instanceof CacheEntryCreatedListener) {
                        cll.add((CacheEntryCreatedListener)el2);
                    }
                    if (el2 instanceof CacheEntryUpdatedListener) {
                        ull.add((CacheEntryUpdatedListener)el2);
                    }
                    if (el2 instanceof CacheEntryRemovedListener) {
                        rll.add((CacheEntryRemovedListener)el2);
                    }
                    if (el2 instanceof CacheEntryExpiredListener) {
                        ell.add((CacheEntryExpiredListener)el2);
                    }
                    if (el2 instanceof CacheEntryEvictedListener) {
                        evl.add((CacheEntryEvictedListener)el2);
                    }
                }
                for (final CacheEntryCreatedListener l : cll) {
                    _syncCreatedListeners.add((CacheEntryCreatedListener<K, V>) new AsyncCreatedListener<K, Object>(_asyncDispatcher, l));
                }
                for (final CacheEntryUpdatedListener i : ull) {
                    _syncUpdatedListeners.add((CacheEntryUpdatedListener<K, V>) new AsyncUpdatedListener<K, Object>(_asyncDispatcher, i));
                }
                for (final CacheEntryRemovedListener j : rll) {
                    _syncRemovedListeners.add((CacheEntryRemovedListener<K, V>)new AsyncRemovedListener<K, Object>(_asyncDispatcher, j));
                }
                for (final CacheEntryExpiredListener k : ell) {
                    _syncExpiredListeners.add((CacheEntryExpiredListener<K, V>)new AsyncExpiredListener<K, Object>(_asyncDispatcher, k));
                }
                for (final CacheEntryExpiredListener k : _expiredListeners) {
                    _syncExpiredListeners.add((CacheEntryExpiredListener<K, V>)new AsyncExpiredListener<K, Object>(_asyncDispatcher, k));
                }
                for (final CacheEntryEvictedListener m : evl) {
                    _syncEvictedListeners.add((CacheEntryEvictedListener<K, V>)new AsyncEvictedListener<K, Object>(_asyncDispatcher, m));
                }
            }
            if (!_syncCreatedListeners.isEmpty()) {
                wc.syncEntryCreatedListeners = (CacheEntryCreatedListener<K, V>[])_syncCreatedListeners.toArray(new CacheEntryCreatedListener[0]);
            }
            if (!_syncUpdatedListeners.isEmpty()) {
                wc.syncEntryUpdatedListeners = (CacheEntryUpdatedListener<K, V>[])_syncUpdatedListeners.toArray(new CacheEntryUpdatedListener[0]);
            }
            if (!_syncRemovedListeners.isEmpty()) {
                wc.syncEntryRemovedListeners = (CacheEntryRemovedListener<K, V>[])_syncRemovedListeners.toArray(new CacheEntryRemovedListener[0]);
            }
            if (!_syncExpiredListeners.isEmpty()) {
                wc.syncEntryExpiredListeners = (CacheEntryExpiredListener<K, V>[])_syncExpiredListeners.toArray(new CacheEntryExpiredListener[0]);
            }
            if (!_syncEvictedListeners.isEmpty()) {
                wc.syncEntryEvictedListeners = (CacheEntryEvictedListener<K, V>[])_syncEvictedListeners.toArray(new CacheEntryEvictedListener[0]);
            }
            bc.eviction = this.constructEviction(bc, (HeapCacheListener)wc, this.config);
            final TimingHandler rh = TimingHandler.of(_timeReference, this.config);
            bc.setTiming(rh);
            wc.init();
        }
        else {
            final TimingHandler rh2 = TimingHandler.of(_timeReference, this.config);
            bc.setTiming(rh2);
            bc.eviction = this.constructEviction(bc, HeapCacheListener.NO_OPERATION, this.config);
            bc.init();
        }
        this.manager.sendCreatedEvent((Cache)_cache, (Cache2kConfiguration)this.config);
        return (Cache<K, V>)_cache;
    }
    
    private Eviction constructEviction(final HeapCache hc, final HeapCacheListener l, final Cache2kConfiguration config) {
        final boolean _strictEviction = config.isStrictEviction();
        final int _availableProcessors = Runtime.getRuntime().availableProcessors();
        final boolean _boostConcurrency = config.isBoostConcurrency();
        final long _maximumWeight = config.getMaximumWeight();
        long _entryCapacity = config.getEntryCapacity();
        if (_entryCapacity < 0L && _maximumWeight < 0L) {
            _entryCapacity = 2000L;
        }
        final int _segmentCountOverride = HeapCache.TUNABLE.segmentCountOverride;
        final int _segmentCount = determineSegmentCount(_strictEviction, _availableProcessors, _boostConcurrency, _entryCapacity, _segmentCountOverride);
        final Eviction[] _segments = new Eviction[_segmentCount];
        final long _maxSize = determineMaxSize(_entryCapacity, _segmentCount);
        final long _maxWeight = determineMaxWeight(_maximumWeight, _segmentCount);
        final Weigher _weigher = (Weigher)hc.createCustomization(config.getWeigher());
        for (int i = 0; i < _segments.length; ++i) {
            final Eviction ev = (Eviction)new ClockProPlusEviction(hc, l, _maxSize, _weigher, _maxWeight, _strictEviction);
            _segments[i] = ev;
        }
        if (_segmentCount == 1) {
            return _segments[0];
        }
        return (Eviction)new SegmentedEviction(_segments);
    }
    
    static long determineMaxSize(final long _entryCapacity, final int _segmentCount) {
        if (_entryCapacity < 0L) {
            return -1L;
        }
        long _maxSize = _entryCapacity / _segmentCount;
        if (_entryCapacity == Long.MAX_VALUE) {
            _maxSize = Long.MAX_VALUE;
        }
        else if (_entryCapacity % _segmentCount > 0L) {
            ++_maxSize;
        }
        return _maxSize;
    }
    
    static long determineMaxWeight(final long _maximumWeight, final int _segmentCount) {
        if (_maximumWeight < 0L) {
            return -1L;
        }
        long _maxWeight = _maximumWeight / _segmentCount;
        if (_maximumWeight == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        if (_maximumWeight % _segmentCount > 0L) {
            ++_maxWeight;
        }
        return _maxWeight;
    }
    
    static int determineSegmentCount(final boolean _strictEviction, final int _availableProcessors, final boolean _boostConcurrency, final long _entryCapacity, final int _segmentCountOverride) {
        int _segmentCount = 1;
        if (_availableProcessors > 1) {
            _segmentCount = 2;
            if (_boostConcurrency) {
                _segmentCount = 2 << 31 - Integer.numberOfLeadingZeros(_availableProcessors);
            }
        }
        if (_segmentCountOverride > 0) {
            _segmentCount = 1 << 32 - Integer.numberOfLeadingZeros(_segmentCountOverride - 1);
        }
        else {
            final int _maxSegments = _availableProcessors * 2;
            _segmentCount = Math.min(_segmentCount, _maxSegments);
        }
        if (_entryCapacity >= 0L && _entryCapacity < 1000L) {
            _segmentCount = 1;
        }
        if (_strictEviction) {
            _segmentCount = 1;
        }
        return _segmentCount;
    }
    
    private void checkConfiguration() {
        if (this.config.getExpireAfterWrite() == 9223372036854775806L && this.config.getExpiryPolicy() == null) {
            throw new IllegalArgumentException("not eternal is set, but expire value is missing");
        }
    }
    
    static {
        DERIVED_NAME_COUNTER = new AtomicLong(System.currentTimeMillis() % 1234L);
        DEFAULT_ASYNC_LISTENER_EXECUTOR = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(), 21L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), HeapCache.TUNABLE.threadFactoryProvider.newThreadFactory("cache2k-listener"), new ThreadPoolExecutor.AbortPolicy());
    }
    
    private static class WrappedAdvancedCacheLoader<K, V> extends AdvancedCacheLoader<K, V> implements Closeable
    {
        HeapCache<K, V> heapCache;
        private final AdvancedCacheLoader<K, V> forward;
        
        public WrappedAdvancedCacheLoader(final HeapCache<K, V> _heapCache, final AdvancedCacheLoader<K, V> _forward) {
            this.heapCache = _heapCache;
            this.forward = _forward;
        }
        
        @Override
        public void close() throws IOException {
            if (this.forward instanceof Closeable) {
                ((Closeable)this.forward).close();
            }
        }
        
        @Override
        public V load(final K key, final long currentTime, final CacheEntry<K, V> currentEntry) throws Exception {
            if (currentEntry == null) {
                return this.forward.load(key, currentTime, null);
            }
            return this.forward.load(key, currentTime, (org.cache2k.CacheEntry<K, V>)this.heapCache.returnCacheEntry((ExaminationEntry)currentEntry));
        }
    }
    
    static class AsyncCreatedListener<K, V> implements CacheEntryCreatedListener<K, V>
    {
        AsyncDispatcher<K> dispatcher;
        CacheEntryCreatedListener<K, V> listener;
        
        public AsyncCreatedListener(final AsyncDispatcher<K> _dispatcher, final CacheEntryCreatedListener<K, V> _listener) {
            this.dispatcher = _dispatcher;
            this.listener = _listener;
        }
        
        @Override
        public void onEntryCreated(final Cache<K, V> c, final CacheEntry<K, V> e) {
            this.dispatcher.queue((AsyncEvent)new AsyncEvent<K>() {
                public K getKey() {
                    return (K)e.getKey();
                }
                
                public void execute() {
                    AsyncCreatedListener.this.listener.onEntryCreated(c, e);
                }
            });
        }
    }
    
    private static class AsyncUpdatedListener<K, V> implements CacheEntryUpdatedListener<K, V>
    {
        AsyncDispatcher<K> dispatcher;
        CacheEntryUpdatedListener<K, V> listener;
        
        public AsyncUpdatedListener(final AsyncDispatcher<K> _dispatcher, final CacheEntryUpdatedListener<K, V> _listener) {
            this.dispatcher = _dispatcher;
            this.listener = _listener;
        }
        
        @Override
        public void onEntryUpdated(final Cache<K, V> cache, final CacheEntry<K, V> currentEntry, final CacheEntry<K, V> entryWithNewData) {
            this.dispatcher.queue((AsyncEvent)new AsyncEvent<K>() {
                public K getKey() {
                    return (K)currentEntry.getKey();
                }
                
                public void execute() {
                    AsyncUpdatedListener.this.listener.onEntryUpdated(cache, currentEntry, entryWithNewData);
                }
            });
        }
    }
    
    private static class AsyncRemovedListener<K, V> implements CacheEntryRemovedListener<K, V>
    {
        AsyncDispatcher<K> dispatcher;
        CacheEntryRemovedListener<K, V> listener;
        
        public AsyncRemovedListener(final AsyncDispatcher<K> _dispatcher, final CacheEntryRemovedListener<K, V> _listener) {
            this.dispatcher = _dispatcher;
            this.listener = _listener;
        }
        
        @Override
        public void onEntryRemoved(final Cache<K, V> c, final CacheEntry<K, V> e) {
            this.dispatcher.queue((AsyncEvent)new AsyncEvent<K>() {
                public K getKey() {
                    return (K)e.getKey();
                }
                
                public void execute() {
                    AsyncRemovedListener.this.listener.onEntryRemoved(c, e);
                }
            });
        }
    }
    
    private static class AsyncExpiredListener<K, V> implements CacheEntryExpiredListener<K, V>
    {
        AsyncDispatcher<K> dispatcher;
        CacheEntryExpiredListener<K, V> listener;
        
        public AsyncExpiredListener(final AsyncDispatcher<K> _dispatcher, final CacheEntryExpiredListener<K, V> _listener) {
            this.dispatcher = _dispatcher;
            this.listener = _listener;
        }
        
        @Override
        public void onEntryExpired(final Cache<K, V> c, final CacheEntry<K, V> e) {
            this.dispatcher.queue((AsyncEvent)new AsyncEvent<K>() {
                public K getKey() {
                    return (K)e.getKey();
                }
                
                public void execute() {
                    AsyncExpiredListener.this.listener.onEntryExpired(c, e);
                }
            });
        }
    }
    
    private static class AsyncEvictedListener<K, V> implements CacheEntryEvictedListener<K, V>
    {
        AsyncDispatcher<K> dispatcher;
        CacheEntryEvictedListener<K, V> listener;
        
        public AsyncEvictedListener(final AsyncDispatcher<K> _dispatcher, final CacheEntryEvictedListener<K, V> _listener) {
            this.dispatcher = _dispatcher;
            this.listener = _listener;
        }
        
        @Override
        public void onEntryEvicted(final Cache<K, V> c, final CacheEntry<K, V> e) {
            this.dispatcher.queue((AsyncEvent)new AsyncEvent<K>() {
                public K getKey() {
                    return (K)e.getKey();
                }
                
                public void execute() {
                    AsyncEvictedListener.this.listener.onEntryEvicted(c, e);
                }
            });
        }
    }
}
