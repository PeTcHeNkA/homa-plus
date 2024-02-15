//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.processor;

import org.cache2k.*;

public interface MutableCacheEntry<K, V> extends CacheEntry<K, V>
{
    V getValue();
    
    boolean exists();
    
    V getOldValue();
    
    boolean wasExisting();
    
    long getCurrentTime();
    
    MutableCacheEntry<K, V> setValue(final V p0);
    
    MutableCacheEntry<K, V> remove();
    
    MutableCacheEntry<K, V> setException(final Throwable p0);
    
    MutableCacheEntry<K, V> setExpiryTime(final long p0);
    
    long getRefreshedTime();
    
    MutableCacheEntry<K, V> setRefreshedTime(final long p0);
}
