//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.jmx;

import java.util.*;

public interface CacheInfoMXBean
{
    String getKeyType();
    
    String getValueType();
    
    long getSize();
    
    long getEntryCapacity();
    
    long getMaximumWeight();
    
    long getCurrentWeight();
    
    long getInsertCount();
    
    long getGetCount();
    
    long getMissCount();
    
    long getLoadCount();
    
    long getRefreshCount();
    
    long getRefreshFailedCount();
    
    long getRefreshedHitCount();
    
    long getExpiredCount();
    
    long getEvictedCount();
    
    long getPutCount();
    
    long getRemoveCount();
    
    long getClearedEntriesCount();
    
    long getClearCount();
    
    long getKeyMutationCount();
    
    long getLoadExceptionCount();
    
    long getSuppressedLoadExceptionCount();
    
    double getHitRate();
    
    int getHashQuality();
    
    double getMillisPerLoad();
    
    long getTotalLoadMillis();
    
    String getImplementation();
    
    Date getCreatedTime();
    
    Date getClearedTime();
    
    Date getInfoCreatedTime();
    
    int getInfoCreatedDeltaMillis();
    
    int getAlert();
    
    String getEvictionStatistics();
    
    String getIntegrityDescriptor();
    
    boolean isLoaderPresent();
    
    boolean isWeigherPresent();
}
