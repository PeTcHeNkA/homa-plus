//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.expiry.*;
import org.cache2k.integration.*;
import org.cache2k.configuration.*;
import org.cache2k.*;
import org.cache2k.core.operation.*;
import org.cache2k.core.util.*;

public abstract class TimingHandler<K, V>
{
    static final TimingHandler ETERNAL;
    static final TimingHandler ETERNAL_IMMEDIATE;
    private static final TimingHandler IMMEDIATE;
    private static final int PURGE_INTERVAL;
    private static final long SAFETY_GAP_MILLIS;
    static final ExpiryPolicy<?, ValueWithExpiryTime> ENTRY_EXPIRY_CALCULATOR_FROM_VALUE;
    
    static boolean realDuration(final long t) {
        return t > 0L && t < Long.MAX_VALUE;
    }
    
    static boolean zeroOrUnspecified(final long t) {
        return t == 0L || t == -1L;
    }
    
    public static <K, V> TimingHandler<K, V> of(final InternalClock _clock, final Cache2kConfiguration<K, V> cfg) {
        if (cfg.getExpireAfterWrite() == 0L && zeroOrUnspecified(cfg.getRetryInterval())) {
            return (TimingHandler<K, V>)TimingHandler.IMMEDIATE;
        }
        if (cfg.getExpiryPolicy() != null || (cfg.getValueType() != null && ValueWithExpiryTime.class.isAssignableFrom(cfg.getValueType().getType())) || cfg.getResiliencePolicy() != null) {
            final Dynamic<K, V> h = new Dynamic<K, V>(_clock, cfg);
            return h;
        }
        if (cfg.getResilienceDuration() > 0L && !cfg.isSuppressExceptions()) {
            throw new IllegalArgumentException("Ambiguous: exceptions suppression is switched off, but resilience duration is specified");
        }
        if (realDuration(cfg.getExpireAfterWrite()) || realDuration(cfg.getRetryInterval()) || realDuration(cfg.getResilienceDuration())) {
            final Static<K, V> h2 = new Static<K, V>(_clock, cfg);
            return h2;
        }
        if (cfg.getExpireAfterWrite() == Long.MAX_VALUE || cfg.getExpireAfterWrite() == -1L) {
            if (zeroOrUnspecified(cfg.getRetryInterval())) {
                return (TimingHandler<K, V>)TimingHandler.ETERNAL_IMMEDIATE;
            }
            if (cfg.getRetryInterval() == Long.MAX_VALUE) {
                return (TimingHandler<K, V>)TimingHandler.ETERNAL;
            }
        }
        throw new IllegalArgumentException("expiry time ambiguous");
    }
    
    public void init(final InternalCache<K, V> c) {
    }
    
    public void reset() {
    }
    
    public void shutdown() {
    }
    
    public void close() {
        this.shutdown();
    }
    
    public abstract long calculateNextRefreshTime(final Entry<K, V> p0, final V p1, final long p2);
    
    public abstract long suppressExceptionUntil(final Entry<K, V> p0, final ExceptionInformation p1);
    
    public abstract long cacheExceptionUntil(final Entry<K, V> p0, final ExceptionInformation p1);
    
    public long stopStartTimer(final long _expiryTime, final Entry<K, V> e) {
        if ((_expiryTime > 0L && _expiryTime < Long.MAX_VALUE) || _expiryTime < 0L) {
            throw new IllegalArgumentException("invalid expiry time, cache is not initialized with expiry: " + Util.formatMillis(_expiryTime));
        }
        return (_expiryTime == 0L) ? 4L : _expiryTime;
    }
    
    public boolean startRefreshProbationTimer(final Entry<K, V> e, final long _nextRefreshTime) {
        return true;
    }
    
    public void cancelExpiryTimer(final Entry<K, V> e) {
    }
    
    public void scheduleFinalTimerForSharpExpiry(final Entry<K, V> e) {
    }
    
    static <K, T> long calcNextRefreshTime(final K _key, final T _newObject, final long now, final Entry _entry, final ExpiryPolicy<K, T> ec, final long _maxLinger, final boolean _sharpExpiryEnabled) {
        if (_maxLinger == 0L) {
            return 0L;
        }
        if (ec != null) {
            final long t = ec.calculateExpiryTime(_key, _newObject, now, (org.cache2k.CacheEntry<K, T>)_entry);
            return limitExpiryToMaxLinger(now, _maxLinger, t, _sharpExpiryEnabled);
        }
        if (_maxLinger < Long.MAX_VALUE) {
            final long t = _maxLinger + now;
            if (t >= 0L) {
                return t;
            }
        }
        return Long.MAX_VALUE;
    }
    
    static long limitExpiryToMaxLinger(final long now, final long _maxLinger, long _requestedExpiryTime, final boolean _sharpExpiryEnabled) {
        if (_sharpExpiryEnabled && _requestedExpiryTime > 1L && _requestedExpiryTime < Long.MAX_VALUE) {
            _requestedExpiryTime = -_requestedExpiryTime;
        }
        return Expiry.mixTimeSpanAndPointInTime(now, _maxLinger, _requestedExpiryTime);
    }
    
    static {
        ETERNAL = new Eternal();
        ETERNAL_IMMEDIATE = new EternalImmediate();
        IMMEDIATE = new Immediate();
        PURGE_INTERVAL = TunableFactory.get(Tunable.class).purgeInterval;
        SAFETY_GAP_MILLIS = HeapCache.TUNABLE.sharpExpirySafetyGapMillis;
        ENTRY_EXPIRY_CALCULATOR_FROM_VALUE = new ExpiryPolicy<Object, ValueWithExpiryTime>() {
            @Override
            public long calculateExpiryTime(final Object _key, final ValueWithExpiryTime _value, final long _loadTime, final CacheEntry<Object, ValueWithExpiryTime> _oldEntry) {
                return _value.getCacheExpiryTime();
            }
        };
    }
    
    abstract static class TimeAgnostic<K, V> extends TimingHandler<K, V>
    {
    }
    
    private static class Eternal<K, V> extends TimeAgnostic<K, V>
    {
        @Override
        public long calculateNextRefreshTime(final Entry<K, V> e, final V v, final long _loadTime) {
            return Long.MAX_VALUE;
        }
        
        @Override
        public long cacheExceptionUntil(final Entry<K, V> e, final ExceptionInformation inf) {
            return Long.MAX_VALUE;
        }
        
        @Override
        public long suppressExceptionUntil(final Entry<K, V> e, final ExceptionInformation inf) {
            return Long.MAX_VALUE;
        }
    }
    
    static class EternalImmediate<K, V> extends TimeAgnostic<K, V>
    {
        @Override
        public long calculateNextRefreshTime(final Entry<K, V> e, final V v, final long _loadTime) {
            return Long.MAX_VALUE;
        }
        
        @Override
        public long cacheExceptionUntil(final Entry<K, V> e, final ExceptionInformation inf) {
            return 0L;
        }
        
        @Override
        public long suppressExceptionUntil(final Entry<K, V> e, final ExceptionInformation inf) {
            return 0L;
        }
    }
    
    static class Immediate<K, V> extends TimeAgnostic<K, V>
    {
        @Override
        public long calculateNextRefreshTime(final Entry<K, V> e, final V v, final long _loadTime) {
            return 0L;
        }
        
        @Override
        public long cacheExceptionUntil(final Entry<K, V> e, final ExceptionInformation inf) {
            return 0L;
        }
        
        @Override
        public long suppressExceptionUntil(final Entry<K, V> e, final ExceptionInformation inf) {
            return 0L;
        }
    }
    
    static class Static<K, V> extends TimingHandler<K, V>
    {
        final InternalClock clock;
        boolean sharpExpiry;
        boolean refreshAhead;
        SimpleTimer[] timer;
        int timerMask;
        long maxLinger;
        InternalCache cache;
        int timerCancelCount;
        int purgeIndex;
        ResiliencePolicy<K, V> resiliencePolicy;
        CustomizationSupplier<ResiliencePolicy<K, V>> resiliencePolicyFactory;
        
        public Static(final InternalClock c, final Cache2kConfiguration<K, V> cc) {
            this.timerCancelCount = 0;
            this.purgeIndex = 0;
            this.clock = c;
            this.configure(cc);
        }
        
        void configure(final Cache2kConfiguration<K, V> c) {
            final long _expiryMillis = c.getExpireAfterWrite();
            if (_expiryMillis == Long.MAX_VALUE || _expiryMillis < 0L) {
                this.maxLinger = Long.MAX_VALUE;
            }
            else {
                this.maxLinger = _expiryMillis;
            }
            final ResiliencePolicy.Context ctx = new ResiliencePolicy.Context() {
                @Override
                public long getExpireAfterWriteMillis() {
                    return c.getExpireAfterWrite();
                }
                
                @Override
                public long getResilienceDurationMillis() {
                    return c.isSuppressExceptions() ? c.getResilienceDuration() : 0L;
                }
                
                @Override
                public long getRetryIntervalMillis() {
                    return c.getRetryInterval();
                }
                
                @Override
                public long getMaxRetryIntervalMillis() {
                    return c.getMaxRetryInterval();
                }
            };
            this.resiliencePolicyFactory = (CustomizationSupplier<ResiliencePolicy<K, V>>)c.getResiliencePolicy();
            if (this.resiliencePolicyFactory == null) {
                this.resiliencePolicy = (ResiliencePolicy<K, V>)new DefaultResiliencePolicy();
            }
            else if (this.resiliencePolicyFactory instanceof CustomizationReferenceSupplier) {
                try {
                    this.resiliencePolicy = (ResiliencePolicy<K, V>)this.resiliencePolicyFactory.supply((CacheManager)null);
                }
                catch (Exception ex) {}
            }
            this.resiliencePolicy.init(ctx);
            this.refreshAhead = c.isRefreshAhead();
            this.sharpExpiry = c.isSharpExpiry();
            int _timerCount = 1;
            if (c.isBoostConcurrency()) {
                final int _ncpu = Runtime.getRuntime().availableProcessors();
                _timerCount = 2 << 31 - Integer.numberOfLeadingZeros(_ncpu);
            }
            this.timer = new SimpleTimer[_timerCount];
            this.timerMask = _timerCount - 1;
        }
        
        @Override
        public synchronized void init(final InternalCache<K, V> c) {
            this.cache = c;
            if (this.resiliencePolicy == null) {
                this.resiliencePolicy = (ResiliencePolicy<K, V>)c.createCustomization((CustomizationSupplier)this.resiliencePolicyFactory);
            }
            this.resiliencePolicyFactory = null;
        }
        
        @Override
        public synchronized void reset() {
            this.shutdown();
            for (int i = 0; i <= this.timerMask; ++i) {
                if (this.timer[i] == null) {
                    this.timer[i] = new SimpleTimer(this.clock, this.cache.getName(), true);
                }
            }
        }
        
        @Override
        public synchronized void shutdown() {
            for (int i = 0; i <= this.timerMask; ++i) {
                final SimpleTimer _timer;
                if ((_timer = this.timer[i]) != null) {
                    _timer.cancel();
                    this.timer[i] = null;
                }
            }
        }
        
        @Override
        public long calculateNextRefreshTime(final Entry<K, V> e, final V v, final long _loadTime) {
            return TimingHandler.calcNextRefreshTime(e.getKey(), v, _loadTime, e, null, this.maxLinger, this.sharpExpiry);
        }
        
        @Override
        public long suppressExceptionUntil(final Entry<K, V> e, final ExceptionInformation inf) {
            return this.resiliencePolicy.suppressExceptionUntil((K)e.getKey(), inf, (org.cache2k.CacheEntry<K, V>)this.cache.returnCacheEntry((ExaminationEntry)e));
        }
        
        @Override
        public long cacheExceptionUntil(final Entry<K, V> e, final ExceptionInformation inf) {
            return this.resiliencePolicy.retryLoadAfter((K)e.getKey(), inf);
        }
        
        long expiredEventuallyStartBackgroundRefresh(final Entry e, final boolean _sharpExpiry) {
            if (this.refreshAhead) {
                e.setTask((SimpleTimerTask)new RefreshTimerTask().to(this.cache, e));
                this.scheduleTask(0L, e);
                return _sharpExpiry ? 5L : 16L;
            }
            return 4L;
        }
        
        @Override
        public long stopStartTimer(long _expiryTime, final Entry e) {
            this.cancelExpiryTimer((Entry<K, V>)e);
            if (_expiryTime == 0L) {
                return 4L;
            }
            if (_expiryTime == -1L) {
                final long nrt = e.getNextRefreshTime();
                if (nrt == 0L) {
                    throw new IllegalArgumentException("neutral expiry not allowed for creation");
                }
                return e.getNextRefreshTime();
            }
            else {
                if (_expiryTime == Long.MAX_VALUE) {
                    return _expiryTime;
                }
                if (_expiryTime == 1L) {
                    return this.expiredEventuallyStartBackgroundRefresh(e, false);
                }
                final long now = this.clock.millis();
                if (Math.abs(_expiryTime) <= now) {
                    return this.expiredEventuallyStartBackgroundRefresh(e, _expiryTime < 0L);
                }
                if (_expiryTime < 0L) {
                    final long _timerTime = -_expiryTime - TimingHandler.SAFETY_GAP_MILLIS;
                    if (_timerTime >= now) {
                        e.setTask((SimpleTimerTask)new ExpireTimerTask().to(this.cache, e));
                        this.scheduleTask(_timerTime, e);
                        _expiryTime = -_expiryTime;
                    }
                    else {
                        this.scheduleFinalExpireWithOptionalRefresh((Entry<K, V>)e, -_expiryTime);
                    }
                }
                else {
                    this.scheduleFinalExpireWithOptionalRefresh((Entry<K, V>)e, _expiryTime);
                }
                return _expiryTime;
            }
        }
        
        @Override
        public boolean startRefreshProbationTimer(final Entry<K, V> e, final long _nextRefreshTime) {
            this.cancelExpiryTimer(e);
            if (_nextRefreshTime == Long.MAX_VALUE) {
                e.setNextRefreshTime(_nextRefreshTime);
                return false;
            }
            if (_nextRefreshTime > 0L && _nextRefreshTime < 32L) {
                e.setNextRefreshTime(4L);
                return true;
            }
            final long _absTime = Math.abs(_nextRefreshTime);
            e.setRefreshProbationNextRefreshTime(_absTime);
            e.setNextRefreshTime(6L);
            e.setTask((SimpleTimerTask)new RefreshExpireTimerTask<K, V>().to((org.cache2k.core.InternalCache<K, V>)this.cache, e));
            this.scheduleTask(_absTime, e);
            return false;
        }
        
        @Override
        public void scheduleFinalTimerForSharpExpiry(final Entry<K, V> e) {
            this.cancelExpiryTimer(e);
            this.scheduleFinalExpireWithOptionalRefresh(e, e.getNextRefreshTime());
        }
        
        void scheduleFinalExpireWithOptionalRefresh(final Entry<K, V> e, final long t) {
            if (this.refreshAhead) {
                e.setTask((SimpleTimerTask)new RefreshTimerTask<K, V>().to((org.cache2k.core.InternalCache<K, V>)this.cache, e));
            }
            else {
                e.setTask((SimpleTimerTask)new ExpireTimerTask<K, V>().to((org.cache2k.core.InternalCache<K, V>)this.cache, e));
            }
            this.scheduleTask(t, e);
        }
        
        void scheduleTask(final long _nextRefreshTime, final Entry e) {
            final SimpleTimer _timer = this.timer[e.hashCode & this.timerMask];
            if (_timer != null) {
                try {
                    _timer.schedule(e.getTask(), _nextRefreshTime);
                }
                catch (IllegalStateException ex) {}
            }
        }
        
        @Override
        public void cancelExpiryTimer(final Entry<K, V> e) {
            final CommonTimerTask tsk = (CommonTimerTask)e.getTask();
            if (tsk != null && tsk.cancel()) {
                tsk.cache = null;
                tsk.entry = null;
                ++this.timerCancelCount;
                if (this.timerCancelCount >= TimingHandler.PURGE_INTERVAL) {
                    synchronized (this.timer) {
                        this.timer[this.purgeIndex].purge();
                        this.purgeIndex = (this.purgeIndex + 1 & this.timerMask);
                        this.timerCancelCount = 0;
                    }
                }
            }
        }
    }
    
    abstract static class CommonTimerTask<K, V> extends SimpleTimerTask
    {
        Entry<K, V> entry;
        InternalCache<K, V> cache;
        
        CommonTimerTask<K, V> to(final InternalCache<K, V> c, final Entry<K, V> e) {
            this.cache = c;
            this.entry = e;
            return this;
        }
        
        public abstract void fire() throws Exception;
        
        @Override
        public final void run() {
            try {
                this.fire();
            }
            catch (CacheClosedException ex2) {}
            catch (Throwable ex) {
                this.cache.logAndCountInternalException("Timer execution exception", ex);
            }
        }
    }
    
    static class RefreshTimerTask<K, V> extends CommonTimerTask<K, V>
    {
        @Override
        public void fire() {
            this.cache.timerEventRefresh((Entry)this.entry);
        }
    }
    
    static class ExpireTimerTask<K, V> extends CommonTimerTask<K, V>
    {
        @Override
        public void fire() {
            this.cache.timerEventExpireEntry((Entry)this.entry);
        }
    }
    
    static class RefreshExpireTimerTask<K, V> extends CommonTimerTask<K, V>
    {
        @Override
        public void fire() {
            this.cache.timerEventProbationTerminated((Entry)this.entry);
        }
    }
    
    static class Dynamic<K, V> extends Static<K, V>
    {
        private ExpiryPolicy<K, V> expiryPolicy;
        private CustomizationSupplier<ExpiryPolicy<K, V>> policyFactory;
        
        public Dynamic(final InternalClock c, final Cache2kConfiguration<K, V> cc) {
            super(c, cc);
        }
        
        @Override
        void configure(final Cache2kConfiguration<K, V> c) {
            super.configure(c);
            this.policyFactory = (CustomizationSupplier<ExpiryPolicy<K, V>>)c.getExpiryPolicy();
            if (this.policyFactory instanceof CustomizationReferenceSupplier) {
                try {
                    this.expiryPolicy = (ExpiryPolicy<K, V>)this.policyFactory.supply((CacheManager)null);
                }
                catch (Exception ex) {}
            }
            if (c.getValueType() != null && ValueWithExpiryTime.class.isAssignableFrom(c.getValueType().getType()) && c.getExpiryPolicy() == null) {
                this.expiryPolicy = (ExpiryPolicy<K, V>)Dynamic.ENTRY_EXPIRY_CALCULATOR_FROM_VALUE;
            }
        }
        
        @Override
        public synchronized void init(final InternalCache<K, V> c) {
            super.init(c);
            if (this.expiryPolicy == null) {
                this.expiryPolicy = (ExpiryPolicy<K, V>)c.createCustomization((CustomizationSupplier)this.policyFactory);
            }
            this.policyFactory = null;
        }
        
        long calcNextRefreshTime(final K _key, final V _newObject, final long now, final Entry _entry) {
            return TimingHandler.calcNextRefreshTime(_key, _newObject, now, _entry, this.expiryPolicy, this.maxLinger, this.sharpExpiry);
        }
        
        @Override
        public long calculateNextRefreshTime(final Entry<K, V> _entry, final V _newValue, final long _loadTime) {
            long t;
            if (_entry.isDataValid() || _entry.isExpired() || _entry.nextRefreshTime == 5L) {
                t = this.calcNextRefreshTime(_entry.getKey(), _newValue, _loadTime, _entry);
            }
            else {
                t = this.calcNextRefreshTime(_entry.getKey(), _newValue, _loadTime, null);
            }
            return t;
        }
        
        @Override
        public synchronized void close() {
            super.shutdown();
            this.cache.closeCustomization((Object)this.expiryPolicy, "expiryPolicy");
        }
    }
    
    public static class Tunable extends TunableConstants
    {
        public int purgeInterval;
        
        public Tunable() {
            this.purgeInterval = 10000;
        }
    }
}
