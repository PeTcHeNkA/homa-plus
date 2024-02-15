//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.experimentalApi;

import org.cache2k.*;
import java.util.concurrent.*;
import java.util.*;

public interface AsyncCacheLoader<K, V>
{
    void get(final K p0, final V p1, final CacheEntry<K, V> p2, final Callback<V> p3, final Executor p4);
    
    public interface Callback<V> extends EventListener
    {
        void onLoadSuccess(final V p0);
        
        void onLoadFailure(final Throwable p0);
    }
}
