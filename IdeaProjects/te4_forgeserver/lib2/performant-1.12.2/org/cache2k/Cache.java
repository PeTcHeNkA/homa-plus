//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k;

import java.io.*;
import java.util.*;
import org.cache2k.processor.*;
import java.util.concurrent.*;
import org.cache2k.jmx.*;

public interface Cache<K, V> extends KeyValueStore<K, V>, Closeable
{
    String getName();
    
    V get(final K p0);
    
    CacheEntry<K, V> getEntry(final K p0);
    
    void prefetch(final K p0);
    
    void prefetchAll(final Iterable<? extends K> p0, final CacheOperationCompletionListener p1);
    
    V peek(final K p0);
    
    CacheEntry<K, V> peekEntry(final K p0);
    
    boolean containsKey(final K p0);
    
    void put(final K p0, final V p1);
    
    V computeIfAbsent(final K p0, final Callable<V> p1);
    
    boolean putIfAbsent(final K p0, final V p1);
    
    V peekAndReplace(final K p0, final V p1);
    
    boolean replace(final K p0, final V p1);
    
    boolean replaceIfEquals(final K p0, final V p1, final V p2);
    
    V peekAndRemove(final K p0);
    
    boolean containsAndRemove(final K p0);
    
    void remove(final K p0);
    
    boolean removeIfEquals(final K p0, final V p1);
    
    void removeAll(final Iterable<? extends K> p0);
    
    V peekAndPut(final K p0, final V p1);
    
    void expireAt(final K p0, final long p1);
    
    void loadAll(final Iterable<? extends K> p0, final CacheOperationCompletionListener p1);
    
    void reloadAll(final Iterable<? extends K> p0, final CacheOperationCompletionListener p1);
    
     <R> R invoke(final K p0, final EntryProcessor<K, V, R> p1);
    
     <R> Map<K, EntryProcessingResult<R>> invokeAll(final Iterable<? extends K> p0, final EntryProcessor<K, V, R> p1);
    
    Map<K, V> getAll(final Iterable<? extends K> p0);
    
    Map<K, V> peekAll(final Iterable<? extends K> p0);
    
    void putAll(final Map<? extends K, ? extends V> p0);
    
    Iterable<K> keys();
    
    Iterable<CacheEntry<K, V>> entries();
    
    void removeAll();
    
    void clear();
    
    void clearAndClose();
    
    void close();
    
    CacheManager getCacheManager();
    
    boolean isClosed();
    
    String toString();
    
     <X> X requestInterface(final Class<X> p0);
    
    ConcurrentMap<K, V> asMap();
    
    CacheInfoMXBean getStatistics();
}
