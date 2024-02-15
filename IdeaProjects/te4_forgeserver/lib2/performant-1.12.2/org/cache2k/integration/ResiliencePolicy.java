//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.integration;

import org.cache2k.expiry.*;
import org.cache2k.*;

public abstract class ResiliencePolicy<K, V> implements ExpiryTimeValues
{
    public void init(final Context context) {
    }
    
    public abstract long suppressExceptionUntil(final K p0, final ExceptionInformation p1, final CacheEntry<K, V> p2);
    
    public abstract long retryLoadAfter(final K p0, final ExceptionInformation p1);
    
    public interface Context
    {
        long getExpireAfterWriteMillis();
        
        long getResilienceDurationMillis();
        
        long getRetryIntervalMillis();
        
        long getMaxRetryIntervalMillis();
    }
}
