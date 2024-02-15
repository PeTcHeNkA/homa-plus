//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import java.util.*;

public interface InternalCacheInfo
{
    String getName();
    
    String getImplementation();
    
    long getSize();
    
    long getHeapCapacity();
    
    long getMaximumWeight();
    
    long getCurrentWeight();
    
    long getHeapHitCount();
    
    long getGetCount();
    
    long getMissCount();
    
    long getNewEntryCount();
    
    long getLoadCount();
    
    long getReloadCount();
    
    long getRefreshCount();
    
    long getInternalExceptionCount();
    
    long getRefreshFailedCount();
    
    long getSuppressedExceptionCount();
    
    long getLoadExceptionCount();
    
    long getRefreshedHitCount();
    
    long getExpiredCount();
    
    long getEvictedCount();
    
    int getEvictionRunningCount();
    
    long getRemoveCount();
    
    long getPutCount();
    
    long getClearedEntriesCount();
    
    long getClearCount();
    
    long getKeyMutationCount();
    
    long getTimerEventCount();
    
    double getHitRate();
    
    String getHitRateString();
    
    int getNoCollisionPercent();
    
    int getHashQuality();
    
    int getHashCollisionCount();
    
    int getHashCollisionSlotCount();
    
    int getHashLongestSlotSize();
    
    double getMillisPerLoad();
    
    long getLoadMillis();
    
    String getIntegrityDescriptor();
    
    long getGoneSpinCount();
    
    long getStartedTime();
    
    long getClearedTime();
    
    long getInfoCreatedTime();
    
    int getInfoCreationDeltaMs();
    
    Collection<HealthInfoElement> getHealth();
    
    String getExtraStatistics();
    
    long getAsyncLoadsStarted();
    
    long getAsyncLoadsInFlight();
    
    int getLoaderThreadsLimit();
    
    int getLoaderThreadsMaxActive();
}
