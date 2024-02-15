//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k;

import java.util.*;
import org.cache2k.processor.*;
import java.util.concurrent.*;
import org.cache2k.jmx.*;

public abstract class ForwardingCache<K, V> implements Cache<K, V>
{
    protected abstract Cache<K, V> delegate();
    
    public String getName() {
        return this.delegate().getName();
    }
    
    public V get(final K key) {
        return (V)this.delegate().get((K) key);
    }
    
    public CacheEntry<K, V> getEntry(final K key) {
        return this.delegate().getEntry((K) key);
    }
    
    public void prefetch(final K key) {
        this.delegate().prefetch((K) key);
    }
    
    public void prefetchAll(final Iterable<? extends K> keys, final CacheOperationCompletionListener listener) {
        this.delegate().prefetchAll((Iterable)keys, listener);
    }
    
    public V peek(final K key) {
        return this.delegate().peek(key);
    }
    
    public CacheEntry<K, V> peekEntry(final K key) {
        return this.delegate().peekEntry(key);
    }
    
    public boolean containsKey(final K key) {
        return this.delegate().containsKey(key);
    }
    
    public void put(final K key, final V value) {
        this.delegate().put(key, value);
    }
    
    public V computeIfAbsent(final K key, final Callable<V> callable) {
        return (V)this.delegate().computeIfAbsent(key, callable);
    }
    
    public boolean putIfAbsent(final K key, final V value) {
        return this.delegate().putIfAbsent(key, value);
    }
    
    public V peekAndReplace(final K key, final V value) {
        return (V)this.delegate().peekAndReplace(key, value);
    }
    
    public boolean replace(final K key, final V value) {
        return this.delegate().replace(key, value);
    }
    
    public boolean replaceIfEquals(final K key, final V oldValue, final V newValue) {
        return this.delegate().replaceIfEquals(key, oldValue, newValue);
    }
    
    public V peekAndRemove(final K key) {
        return (V)this.delegate().peekAndRemove(key);
    }
    
    public boolean containsAndRemove(final K key) {
        return this.delegate().containsAndRemove(key);
    }
    
    public void remove(final K key) {
        this.delegate().remove(key);
    }
    
    public boolean removeIfEquals(final K key, final V expectedValue) {
        return this.delegate().removeIfEquals(key, expectedValue);
    }
    
    public void removeAll(final Iterable<? extends K> keys) {
        this.delegate().removeAll((Iterable)keys);
    }
    
    public V peekAndPut(final K key, final V value) {
        return (V)this.delegate().peekAndPut(key, value);
    }
    
    public void expireAt(final K key, final long millis) {
        this.delegate().expireAt(key, millis);
    }
    
    public void loadAll(final Iterable<? extends K> keys, final CacheOperationCompletionListener listener) {
        this.delegate().loadAll(keys, listener);
    }
    
    public void reloadAll(final Iterable<? extends K> keys, final CacheOperationCompletionListener listener) {
        this.delegate().reloadAll(keys, listener);
    }
    
    public <R> R invoke(final K key, final EntryProcessor<K, V, R> entryProcessor) {
        return this.delegate().invoke(key, entryProcessor);
    }
    
    public <R> Map<K, EntryProcessingResult<R>> invokeAll(final Iterable<? extends K> keys, final EntryProcessor<K, V, R> entryProcessor) {
        return this.delegate().invokeAll(keys, entryProcessor);
    }
    
    public Map<K, V> getAll(final Iterable<? extends K> keys) {
        return (Map<K, V>)this.delegate().getAll(keys);
    }
    
    public Map<K, V> peekAll(final Iterable<? extends K> keys) {
        return (Map<K, V>)this.delegate().peekAll(keys);
    }
    
    public void putAll(final Map<? extends K, ? extends V> valueMap) {
        this.delegate().putAll(valueMap);
    }
    
    public Iterable<K> keys() {
        return this.delegate().keys();
    }
    
    public Iterable<CacheEntry<K, V>> entries() {
        return this.delegate().entries();
    }
    
    public void removeAll() {
        this.delegate().removeAll();
    }
    
    public void clear() {
        this.delegate().clear();
    }
    
    public void clearAndClose() {
        this.delegate().clearAndClose();
    }
    
    public void close() {
        this.delegate().close();
    }
    
    public CacheManager getCacheManager() {
        return this.delegate().getCacheManager();
    }
    
    public boolean isClosed() {
        return this.delegate().isClosed();
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "!" + this.delegate().toString();
    }
    
    public <X> X requestInterface(final Class<X> _type) {
        return (X)this.delegate().requestInterface((Class)_type);
    }
    
    public ConcurrentMap<K, V> asMap() {
        return (ConcurrentMap<K, V>)this.delegate().asMap();
    }
    
    public CacheInfoMXBean getStatistics() {
        return this.delegate().getStatistics();
    }
}
