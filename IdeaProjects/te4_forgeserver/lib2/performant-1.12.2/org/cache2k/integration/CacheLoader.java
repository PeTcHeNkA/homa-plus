//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.integration;

import java.util.concurrent.*;
import java.util.*;

public abstract class CacheLoader<K, V> implements FunctionalCacheLoader<K, V>
{
    @Override
    public abstract V load(final K p0) throws Exception;
    
    public Map<K, V> loadAll(final Iterable<? extends K> keys, final Executor executor) throws Exception {
        throw new UnsupportedOperationException();
    }
}
