//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

public interface EvictionMetrics
{
    long getNewEntryCount();
    
    long getHitCount();
    
    long getRemovedCount();
    
    long getExpiredRemovedCount();
    
    long getVirginRemovedCount();
    
    long getEvictedCount();
    
    int getEvictionRunningCount();
    
    long getSize();
    
    long getMaxSize();
    
    long getMaxWeight();
    
    long getCurrentWeight();
    
    String getExtraStatistics();
}
