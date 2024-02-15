//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k;

import java.util.*;
import org.cache2k.processor.*;
import java.util.concurrent.*;
import org.cache2k.jmx.*;

public class AbstractCache<K, V> implements Cache<K, V>
{
    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public V get(final K key) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public CacheEntry<K, V> getEntry(final K key) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void prefetch(final K key) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void prefetchAll(final Iterable<? extends K> keys, final CacheOperationCompletionListener listener) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public V peek(final K key) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public CacheEntry<K, V> peekEntry(final K key) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean containsKey(final K key) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public V computeIfAbsent(final K key, final Callable<V> callable) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean putIfAbsent(final K key, final V value) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public V peekAndReplace(final K key, final V value) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean replace(final K key, final V value) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean replaceIfEquals(final K key, final V oldValue, final V newValue) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public V peekAndRemove(final K key) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean containsAndRemove(final K key) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void remove(final K key) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean removeIfEquals(final K key, final V expectedValue) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void removeAll(final Iterable<? extends K> keys) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public V peekAndPut(final K key, final V value) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void expireAt(final K key, final long millis) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void loadAll(final Iterable<? extends K> keys, final CacheOperationCompletionListener listener) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void reloadAll(final Iterable<? extends K> keys, final CacheOperationCompletionListener listener) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <R> R invoke(final K key, final EntryProcessor<K, V, R> entryProcessor) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <R> Map<K, EntryProcessingResult<R>> invokeAll(final Iterable<? extends K> keys, final EntryProcessor<K, V, R> entryProcessor) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Map<K, V> getAll(final Iterable<? extends K> keys) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Map<K, V> peekAll(final Iterable<? extends K> keys) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void putAll(final Map<? extends K, ? extends V> valueMap) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Iterable<K> keys() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Iterable<CacheEntry<K, V>> entries() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void removeAll() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void clearAndClose() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public CacheManager getCacheManager() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <X> X requestInterface(final Class<X> _type) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ConcurrentMap<K, V> asMap() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public CacheInfoMXBean getStatistics() {
        throw new UnsupportedOperationException();
    }
}
