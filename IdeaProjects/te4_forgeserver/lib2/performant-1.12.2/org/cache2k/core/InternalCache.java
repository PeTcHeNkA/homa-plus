//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.core.storageApi.*;
import org.cache2k.configuration.*;
import org.cache2k.core.util.*;
import org.cache2k.core.operation.*;
import org.cache2k.*;

public interface InternalCache<K, V> extends Cache<K, V>, CanCheckIntegrity
{
    CommonMetrics getCommonMetrics();
    
    Log getLog();
    
    StorageAdapter getStorage();
    
    CacheType getKeyType();
    
    CacheType getValueType();
    
    void cancelTimerJobs();
    
    void timerEventRefresh(final Entry<K, V> p0);
    
    void timerEventExpireEntry(final Entry<K, V> p0);
    
    void timerEventProbationTerminated(final Entry<K, V> p0);
    
    void expireOrScheduleFinalExpireEvent(final Entry<K, V> p0);
    
    InternalCacheInfo getInfo();
    
    InternalCacheInfo getLatestInfo();
    
    String getEntryState(final K p0);
    
    int getTotalEntryCount();
    
    void logAndCountInternalException(final String p0, final Throwable p1);
    
    boolean isNullValuePermitted();
    
     <T> T createCustomization(final CustomizationSupplier<T> p0);
    
    void closeCustomization(final Object p0, final String p1);
    
    InternalClock getClock();
    
    CacheEntry<K, V> returnCacheEntry(final ExaminationEntry<K, V> p0);
    
    boolean isWeigherPresent();
    
    boolean isLoaderPresent();
}
