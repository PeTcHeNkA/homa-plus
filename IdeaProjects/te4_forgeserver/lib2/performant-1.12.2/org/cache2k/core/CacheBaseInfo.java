//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import java.util.concurrent.*;
import java.util.*;
import org.cache2k.core.util.*;
import org.cache2k.*;
import java.text.*;

class CacheBaseInfo implements InternalCacheInfo
{
    private CommonMetrics metrics;
    private HeapCache heapCache;
    private InternalCache cache;
    private long size;
    private long infoCreatedTime;
    private int infoCreationDeltaMs;
    private long missCnt;
    private long hitCnt;
    private long correctedPutCnt;
    private CollisionInfo collisionInfo;
    private String extraStatistics;
    private IntegrityState integrityState;
    private long totalLoadCnt;
    private int loaderThreadsLimit;
    private long asyncLoadsStarted;
    private long asyncLoadsInFlight;
    private int loaderThreadsMaxActive;
    private long clearedTime;
    private long newEntryCnt;
    private long keyMutationCnt;
    private long removedCnt;
    private long clearRemovedCnt;
    private long clearCnt;
    private long expiredRemoveCnt;
    private long evictedCnt;
    private long maxSize;
    private int evictionRunningCnt;
    private long internalExceptionCnt;
    private long maxWeight;
    private long currentWeight;
    
    public CacheBaseInfo(final HeapCache _heapCache, final InternalCache _userCache, final long now) {
        this.loaderThreadsLimit = -1;
        this.asyncLoadsStarted = -1L;
        this.asyncLoadsInFlight = -1L;
        this.loaderThreadsMaxActive = -1;
        this.infoCreatedTime = now;
        this.cache = _userCache;
        this.heapCache = _heapCache;
        this.metrics = _heapCache.metrics;
        final EvictionMetrics em = _heapCache.eviction.getMetrics();
        this.newEntryCnt = em.getNewEntryCount();
        this.expiredRemoveCnt = em.getExpiredRemovedCount();
        this.evictedCnt = em.getEvictedCount();
        this.maxSize = em.getMaxSize();
        this.maxWeight = em.getMaxWeight();
        this.currentWeight = em.getCurrentWeight();
        this.clearedTime = _heapCache.clearedTime;
        this.keyMutationCnt = _heapCache.keyMutationCnt;
        this.removedCnt = em.getRemovedCount();
        this.clearRemovedCnt = _heapCache.clearRemovedCnt;
        this.clearCnt = _heapCache.clearCnt;
        this.internalExceptionCnt = _heapCache.internalExceptionCnt;
        this.evictionRunningCnt = em.getEvictionRunningCount();
        this.integrityState = _heapCache.getIntegrityState();
        this.collisionInfo = new CollisionInfo();
        _heapCache.hash.calcHashCollisionInfo(this.collisionInfo);
        this.extraStatistics = em.getExtraStatistics();
        if (this.extraStatistics.startsWith(", ")) {
            this.extraStatistics = this.extraStatistics.substring(2);
        }
        this.size = this.heapCache.getLocalSize();
        this.missCnt = this.metrics.getLoadCount() + this.metrics.getReloadCount() + this.metrics.getPeekHitNotFreshCount() + this.metrics.getPeekMissCount();
        this.hitCnt = em.getHitCount();
        this.correctedPutCnt = this.metrics.getPutNewEntryCount() + this.metrics.getPutHitCount() + this.metrics.getPutNoReadHitCount();
        if (_heapCache.loaderExecutor instanceof ExclusiveExecutor) {
            final ThreadPoolExecutor ex = ((ExclusiveExecutor)_heapCache.loaderExecutor).getThreadPoolExecutor();
            this.asyncLoadsInFlight = ex.getActiveCount();
            this.asyncLoadsStarted = ex.getTaskCount();
            this.loaderThreadsLimit = ex.getCorePoolSize();
            this.loaderThreadsMaxActive = ex.getLargestPoolSize();
        }
        this.totalLoadCnt = this.metrics.getLoadCount() + this.metrics.getReloadCount() + this.metrics.getRefreshCount();
    }
    
    String percentString(final double d) {
        final String s = Double.toString(d);
        return ((s.length() > 5) ? s.substring(0, 5) : s) + "%";
    }
    
    public void setInfoCreationDeltaMs(final int _millis) {
        this.infoCreationDeltaMs = _millis;
    }
    
    @Override
    public String getName() {
        return this.heapCache.name;
    }
    
    @Override
    public String getImplementation() {
        return this.cache.getClass().getSimpleName();
    }
    
    @Override
    public long getReloadCount() {
        return this.metrics.getReloadCount();
    }
    
    @Override
    public long getSize() {
        return this.size;
    }
    
    @Override
    public long getHeapCapacity() {
        return this.maxSize;
    }
    
    @Override
    public long getMaximumWeight() {
        return this.maxWeight;
    }
    
    @Override
    public long getCurrentWeight() {
        return this.currentWeight;
    }
    
    @Override
    public long getGetCount() {
        final long _putHit = this.metrics.getPutNoReadHitCount();
        final long _heapHitButNoRead = this.metrics.getHeapHitButNoReadCount();
        return this.hitCnt + this.metrics.getPeekMissCount() + this.metrics.getLoadCount() - _putHit - _heapHitButNoRead;
    }
    
    @Override
    public long getMissCount() {
        return this.missCnt;
    }
    
    @Override
    public long getNewEntryCount() {
        return this.newEntryCnt;
    }
    
    @Override
    public long getHeapHitCount() {
        return this.hitCnt;
    }
    
    @Override
    public long getLoadCount() {
        return this.totalLoadCnt;
    }
    
    @Override
    public long getRefreshCount() {
        return this.metrics.getRefreshCount();
    }
    
    @Override
    public long getInternalExceptionCount() {
        return this.internalExceptionCnt;
    }
    
    @Override
    public long getRefreshFailedCount() {
        return this.metrics.getRefreshFailedCount();
    }
    
    @Override
    public long getSuppressedExceptionCount() {
        return this.metrics.getSuppressedExceptionCount();
    }
    
    @Override
    public long getLoadExceptionCount() {
        return this.metrics.getLoadExceptionCount() + this.metrics.getSuppressedExceptionCount();
    }
    
    @Override
    public long getRefreshedHitCount() {
        return this.metrics.getRefreshedHitCount();
    }
    
    @Override
    public long getExpiredCount() {
        return this.expiredRemoveCnt + this.metrics.getExpiredKeptCount();
    }
    
    @Override
    public long getEvictedCount() {
        return this.evictedCnt;
    }
    
    @Override
    public int getEvictionRunningCount() {
        return this.evictionRunningCnt;
    }
    
    @Override
    public long getRemoveCount() {
        return this.removedCnt;
    }
    
    @Override
    public long getPutCount() {
        return this.correctedPutCnt;
    }
    
    @Override
    public long getGoneSpinCount() {
        return this.metrics.getGoneSpinCount();
    }
    
    @Override
    public long getKeyMutationCount() {
        return this.keyMutationCnt;
    }
    
    @Override
    public long getTimerEventCount() {
        return this.metrics.getTimerEventCount();
    }
    
    @Override
    public double getHitRate() {
        final long cnt = this.getGetCount();
        return (cnt == 0L) ? 0.0 : ((cnt - this.missCnt) * 100.0 / cnt);
    }
    
    @Override
    public String getHitRateString() {
        return this.percentString(this.getHitRate());
    }
    
    @Override
    public int getNoCollisionPercent() {
        if (this.size == 0L) {
            return 100;
        }
        return (int)((this.size - this.collisionInfo.collisionCnt) * 100L / this.size);
    }
    
    @Override
    public int getHashQuality() {
        return hashQuality(this.getNoCollisionPercent(), this.getHashLongestSlotSize());
    }
    
    @Override
    public double getMillisPerLoad() {
        return (this.getLoadCount() == 0L) ? 0.0 : (this.metrics.getLoadMillis() * 1.0 / this.getLoadCount());
    }
    
    @Override
    public long getLoadMillis() {
        return this.metrics.getLoadMillis();
    }
    
    @Override
    public int getHashCollisionCount() {
        return this.collisionInfo.collisionCnt;
    }
    
    @Override
    public int getHashCollisionSlotCount() {
        return this.collisionInfo.collisionSlotCnt;
    }
    
    @Override
    public int getHashLongestSlotSize() {
        return this.collisionInfo.longestCollisionSize;
    }
    
    @Override
    public String getIntegrityDescriptor() {
        return this.integrityState.getStateDescriptor();
    }
    
    @Override
    public long getStartedTime() {
        return this.heapCache.startedTime;
    }
    
    @Override
    public long getClearedTime() {
        return this.clearedTime;
    }
    
    @Override
    public long getInfoCreatedTime() {
        return this.infoCreatedTime;
    }
    
    @Override
    public int getInfoCreationDeltaMs() {
        return this.infoCreationDeltaMs;
    }
    
    @Override
    public Collection<HealthInfoElement> getHealth() {
        final List<HealthInfoElement> l = new ArrayList<HealthInfoElement>();
        if (this.integrityState.getStateFlags() > 0L) {
            l.add(new HealthBean(this.cache, "integrity", "FAILURE", "Integrity check error: " + this.integrityState.getStateFlags()));
        }
        final int _WARNING_THRESHOLD = HeapCache.TUNABLE.hashQualityWarningThreshold;
        final int _ERROR_THRESHOLD = HeapCache.TUNABLE.hashQualityErrorThreshold;
        if (this.getHashQuality() < _ERROR_THRESHOLD) {
            l.add(new HealthBean(this.cache, "hashing", "FAILURE", "hash quality is " + this.getHashQuality() + " (threshold: " + _ERROR_THRESHOLD + ")"));
        }
        else if (this.getHashQuality() < _WARNING_THRESHOLD) {
            l.add(new HealthBean(this.cache, "hashing", "WARNING", "hash quality is " + this.getHashQuality() + " (threshold: " + _WARNING_THRESHOLD + ")"));
        }
        if (this.getKeyMutationCount() > 0L) {
            l.add(new HealthBean(this.cache, "keyMutation", "WARNING", "key mutation detected"));
        }
        if (this.getInternalExceptionCount() > 0L) {
            l.add(new HealthBean(this.cache, "internalException", "WARNING", "internal exception"));
        }
        return l;
    }
    
    @Override
    public long getAsyncLoadsStarted() {
        return this.asyncLoadsStarted;
    }
    
    @Override
    public long getAsyncLoadsInFlight() {
        return this.asyncLoadsInFlight;
    }
    
    @Override
    public int getLoaderThreadsLimit() {
        return this.loaderThreadsLimit;
    }
    
    @Override
    public int getLoaderThreadsMaxActive() {
        return this.loaderThreadsMaxActive;
    }
    
    @Override
    public String getExtraStatistics() {
        return this.extraStatistics;
    }
    
    private static String timestampToString(final long t) {
        if (t == 0L) {
            return "-";
        }
        return Util.formatMillis(t);
    }
    
    @Override
    public long getClearCount() {
        return this.clearCnt;
    }
    
    @Override
    public long getClearedEntriesCount() {
        return this.clearRemovedCnt;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Cache(");
        final CacheManagerImpl cm = (CacheManagerImpl)this.cache.getCacheManager();
        sb.append("name=").append(BaseCache.nameQualifier((Cache)this.cache)).append(", ").append("size=").append(this.getSize()).append(", ");
        if (this.getHeapCapacity() >= 0L) {
            sb.append("capacity=").append((this.getHeapCapacity() != Long.MAX_VALUE) ? Long.valueOf(this.getHeapCapacity()) : "unlimited").append(", ");
        }
        else {
            sb.append("maximumWeight=").append((this.getMaximumWeight() != Long.MAX_VALUE) ? Long.valueOf(this.getMaximumWeight()) : "unlimited").append(", ");
            sb.append("currentWeight=").append(this.getCurrentWeight()).append(", ");
        }
        sb.append("get=").append(this.getGetCount()).append(", ").append("miss=").append(this.getMissCount()).append(", ").append("put=").append(this.getPutCount()).append(", ").append("load=").append(this.getLoadCount()).append(", ").append("reload=").append(this.getReloadCount()).append(", ").append("heapHit=").append(this.getHeapHitCount()).append(", ").append("refresh=").append(this.getRefreshCount()).append(", ").append("refreshFailed=").append(this.getRefreshFailedCount()).append(", ").append("refreshedHit=").append(this.getRefreshedHitCount()).append(", ").append("loadException=").append(this.getLoadExceptionCount()).append(", ").append("suppressedException=").append(this.getSuppressedExceptionCount()).append(", ").append("new=").append(this.getNewEntryCount()).append(", ").append("expire=").append(this.getExpiredCount()).append(", ").append("remove=").append(this.getRemoveCount()).append(", ").append("clear=").append(this.getClearCount()).append(", ").append("removeByClear=").append(this.getClearedEntriesCount()).append(", ").append("evict=").append(this.getEvictedCount()).append(", ").append("timer=").append(this.getTimerEventCount()).append(", ").append("goneSpin=").append(this.getGoneSpinCount()).append(", ").append("hitRate=").append(this.getHitRateString()).append(", ").append("msecs/load=").append(formatMillisPerLoad(this.getMillisPerLoad())).append(", ").append("asyncLoadsStarted=").append(this.asyncLoadsStarted).append(", ").append("asyncLoadsInFlight=").append(this.asyncLoadsInFlight).append(", ").append("loaderThreadsLimit=").append(this.loaderThreadsLimit).append(", ").append("loaderThreadsMaxActive=").append(this.loaderThreadsMaxActive).append(", ").append("created=").append(timestampToString(this.getStartedTime())).append(", ").append("cleared=").append(timestampToString(this.getClearedTime())).append(", ").append("infoCreated=").append(timestampToString(this.getInfoCreatedTime())).append(", ").append("infoCreationDeltaMs=").append(this.getInfoCreationDeltaMs()).append(", ").append("collisions=").append(this.getHashCollisionCount()).append(", ").append("collisionSlots=").append(this.getHashCollisionSlotCount()).append(", ").append("longestSlot=").append(this.getHashLongestSlotSize()).append(", ").append("hashQuality=").append(this.getHashQuality()).append(", ").append("noCollisionPercent=").append(this.getNoCollisionPercent()).append(", ").append("impl=").append(this.getImplementation()).append(", ").append(this.getExtraStatistics()).append(", ").append("evictionRunning=").append(this.getEvictionRunningCount()).append(", ").append("keyMutation=").append(this.getKeyMutationCount()).append(", ").append("internalException=").append(this.getInternalExceptionCount()).append(", ").append("integrityState=").append(this.getIntegrityDescriptor()).append(", ").append("version=").append(cm.getProvider().getVersion()).append(")");
        return sb.toString();
    }
    
    static String formatMillisPerLoad(final double val) {
        if (val < 0.0) {
            return "-";
        }
        final DecimalFormat f = new DecimalFormat("#.###");
        return f.format(val);
    }
    
    static int hashQuality(final int _noCollisionPercent, final int _longestSlot) {
        if (_longestSlot == 0) {
            return 100;
        }
        final double _EXPONENT_CONSTANT = -0.011;
        final int _SLOT_SIZE_MINIMUM = 5;
        final int _correctionForOversizeSlot = (int)((1.0 - Math.exp(-0.011 * Math.max(0, _longestSlot - 5))) * 100.0);
        final int _quality = _noCollisionPercent - _correctionForOversizeSlot;
        return Math.max(0, Math.min(100, _quality));
    }
    
    static class HealthBean implements HealthInfoElement
    {
        String id;
        String message;
        String level;
        InternalCache cache;
        
        public HealthBean(final InternalCache _cache, final String _id, final String _level, final String _message) {
            this.cache = _cache;
            this.id = _id;
            this.level = _level;
            this.message = _message;
        }
        
        @Override
        public InternalCache getCache() {
            return this.cache;
        }
        
        @Override
        public String getId() {
            return this.id;
        }
        
        @Override
        public String getLevel() {
            return this.level;
        }
        
        @Override
        public String getMessage() {
            return this.message;
        }
    }
}
