//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.integration;

import java.util.*;
import java.util.concurrent.*;

public interface AsyncCacheLoader<K, V>
{
    void load(final K p0, final Context<K, V> p1, final Callback<K, V> p2) throws Exception;
    
    public interface Callback<K, V> extends EventListener
    {
        void onLoadSuccess(final V p0);
        
        void onLoadFailure(final Throwable p0);
    }
    
    public interface Context<K, V>
    {
        K getKey();
        
        Executor getLoaderExecutor();
        
        V getCachedValue();
        
        Throwable getCachedException();
        
        long getCurrentTime();
    }
}
