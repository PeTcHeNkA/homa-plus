//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.jmx.*;
import java.util.*;

public class CacheMXBeanImpl implements CacheMXBean
{
    private InternalCache cache;
    
    @Override
    public String getKeyType() {
        return this.cache.getKeyType().getTypeName();
    }
    
    @Override
    public String getValueType() {
        return this.cache.getValueType().getTypeName();
    }
    
    public CacheMXBeanImpl(final InternalCache cache) {
        this.cache = cache;
    }
    
    private InternalCacheInfo getInfo() {
        return this.cache.getInfo();
    }
    
    @Override
    public long getSize() {
        return this.getInfo().getSize();
    }
    
    @Override
    public long getEntryCapacity() {
        return this.getInfo().getHeapCapacity();
    }
    
    @Override
    public long getMaximumWeight() {
        return this.getInfo().getMaximumWeight();
    }
    
    @Override
    public long getCurrentWeight() {
        return this.getInfo().getCurrentWeight();
    }
    
    @Override
    public long getMissCount() {
        return this.getInfo().getMissCount();
    }
    
    @Override
    public long getInsertCount() {
        return this.getInfo().getNewEntryCount();
    }
    
    @Override
    public long getLoadCount() {
        return this.getInfo().getLoadCount();
    }
    
    @Override
    public long getRefreshCount() {
        return this.getInfo().getRefreshCount();
    }
    
    @Override
    public long getRefreshFailedCount() {
        return this.getInfo().getRefreshFailedCount();
    }
    
    @Override
    public long getRefreshedHitCount() {
        return this.getInfo().getRefreshedHitCount();
    }
    
    @Override
    public long getExpiredCount() {
        return this.getInfo().getExpiredCount();
    }
    
    @Override
    public long getEvictedCount() {
        return this.getInfo().getEvictedCount();
    }
    
    @Override
    public long getKeyMutationCount() {
        return this.getInfo().getKeyMutationCount();
    }
    
    @Override
    public long getLoadExceptionCount() {
        return this.getInfo().getLoadExceptionCount();
    }
    
    @Override
    public long getSuppressedLoadExceptionCount() {
        return this.getInfo().getSuppressedExceptionCount();
    }
    
    @Override
    public long getGetCount() {
        return this.getInfo().getGetCount();
    }
    
    @Override
    public long getPutCount() {
        return this.getInfo().getPutCount();
    }
    
    @Override
    public long getClearCount() {
        return this.getInfo().getClearCount();
    }
    
    @Override
    public long getRemoveCount() {
        return this.getInfo().getRemoveCount();
    }
    
    @Override
    public long getClearedEntriesCount() {
        return this.getInfo().getClearedEntriesCount();
    }
    
    @Override
    public double getHitRate() {
        return this.getInfo().getHitRate();
    }
    
    @Override
    public int getHashQuality() {
        return this.getInfo().getHashQuality();
    }
    
    @Override
    public double getMillisPerLoad() {
        return this.getInfo().getMillisPerLoad();
    }
    
    @Override
    public long getTotalLoadMillis() {
        return this.getInfo().getLoadMillis();
    }
    
    @Override
    public String getIntegrityDescriptor() {
        return this.getInfo().getIntegrityDescriptor();
    }
    
    @Override
    public Date getCreatedTime() {
        return new Date(this.getInfo().getStartedTime());
    }
    
    @Override
    public Date getClearedTime() {
        return this.optionalDate(this.getInfo().getClearedTime());
    }
    
    @Override
    public Date getInfoCreatedTime() {
        return new Date(this.getInfo().getInfoCreatedTime());
    }
    
    Date optionalDate(final long _millis) {
        return (_millis == 0L) ? null : new Date(_millis);
    }
    
    @Override
    public int getInfoCreatedDeltaMillis() {
        return this.getInfo().getInfoCreationDeltaMs();
    }
    
    @Override
    public String getImplementation() {
        return this.getInfo().getImplementation();
    }
    
    @Override
    public void clear() {
        this.cache.clear();
    }
    
    @Override
    public int getAlert() {
        final Iterator<HealthInfoElement> it = this.getInfo().getHealth().iterator();
        if (!it.hasNext()) {
            return 0;
        }
        final HealthInfoElement hi = it.next();
        if ("FAILURE".equals(hi.getLevel())) {
            return 2;
        }
        return 1;
    }
    
    @Override
    public String getEvictionStatistics() {
        return this.getInfo().getExtraStatistics();
    }
    
    @Override
    public boolean isLoaderPresent() {
        return this.cache.isLoaderPresent();
    }
    
    @Override
    public boolean isWeigherPresent() {
        return this.cache.isWeigherPresent();
    }
}
