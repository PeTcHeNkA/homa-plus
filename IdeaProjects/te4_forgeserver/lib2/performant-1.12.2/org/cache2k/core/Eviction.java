//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.core.concurrency.*;

public interface Eviction
{
    boolean submitWithoutEviction(final Entry p0);
    
    void updateWeight(final Entry p0);
    
    void evictEventually(final int p0);
    
    void evictEventually();
    
    long removeAll();
    
    boolean drain();
    
    void start();
    
    void stop();
    
    void close();
    
     <T> T runLocked(final Job<T> p0);
    
    void checkIntegrity(final IntegrityState p0);
    
    EvictionMetrics getMetrics();
    
    boolean isWeigherPresent();
}
