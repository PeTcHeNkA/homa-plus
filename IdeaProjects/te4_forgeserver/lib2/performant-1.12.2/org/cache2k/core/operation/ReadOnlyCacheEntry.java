//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.operation;

import org.cache2k.*;
import org.cache2k.core.*;

public class ReadOnlyCacheEntry<K, V> extends AbstractCacheEntry<K, V> implements ResultEntry<K, V>
{
    private K key;
    private V valueOrException;
    private long refreshTime;
    
    public static <K, V> ReadOnlyCacheEntry<K, V> of(final CacheEntry<K, V> entry) {
        if (entry instanceof ReadOnlyCacheEntry) {
            return (ReadOnlyCacheEntry<K, V>)(ReadOnlyCacheEntry)entry;
        }
        return new ReadOnlyCacheEntry<K, V>((org.cache2k.core.Entry<K, V>)entry);
    }
    
    public ReadOnlyCacheEntry(final Entry<K, V> entry) {
        this.setValues(entry);
    }
    
    private void setValues(final Entry<K, V> entry) {
        this.setValues(entry.getKey(), entry.getValueOrException(), entry.getRefreshTime());
    }
    
    private void setValues(final K _key, final V _valueOrException, final long _refreshTime) {
        this.key = _key;
        this.refreshTime = _refreshTime;
        this.valueOrException = _valueOrException;
    }
    
    public Throwable getException() {
        if (this.valueOrException instanceof ExceptionWrapper) {
            return ((ExceptionWrapper)this.valueOrException).getException();
        }
        return null;
    }
    
    public K getKey() {
        return this.key;
    }
    
    public long getRefreshTime() {
        return this.refreshTime;
    }
    
    public V getValue() {
        if (this.valueOrException instanceof ExceptionWrapper) {
            return null;
        }
        return this.valueOrException;
    }
    
    public V getValueOrException() {
        return this.valueOrException;
    }
}
