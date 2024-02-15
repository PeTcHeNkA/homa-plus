//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import java.util.concurrent.*;
import org.cache2k.core.storageApi.*;
import java.util.*;
import org.cache2k.processor.*;
import org.cache2k.core.operation.*;
import org.cache2k.configuration.*;
import java.io.*;
import org.cache2k.*;
import org.cache2k.jmx.*;

public abstract class BaseCache<K, V> implements InternalCache<K, V>
{
    public static String nameQualifier(final Cache cache) {
        final StringBuilder sb = new StringBuilder();
        sb.append('\'').append(cache.getName()).append('\'');
        if (!cache.getCacheManager().isDefaultManager()) {
            sb.append(", manager='").append(cache.getCacheManager().getName()).append('\'');
        }
        return sb.toString();
    }
    
    protected abstract Iterator<CacheEntry<K, V>> iterator();
    
    public Iterable<K> keys() {
        return new Iterable<K>() {
            @Override
            public Iterator<K> iterator() {
                final Iterator<CacheEntry<K, V>> it = BaseCache.this.iterator();
                return new Iterator<K>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    
                    @Override
                    public K next() {
                        return (K)it.next().getKey();
                    }
                    
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
    
    public Iterable<CacheEntry<K, V>> entries() {
        return new Iterable<CacheEntry<K, V>>() {
            @Override
            public Iterator<CacheEntry<K, V>> iterator() {
                return BaseCache.this.iterator();
            }
        };
    }
    
    public void removeAll() {
        this.removeAll(this.keys());
    }
    
    public void removeAll(final Iterable<? extends K> _keys) {
        for (final K k : _keys) {
            this.remove(k);
        }
    }
    
    public <X> X requestInterface(final Class<X> _type) {
        if (_type.equals(ConcurrentMap.class) || _type.equals(Map.class)) {
            return (X)new ConcurrentMapWrapper((InternalCache<K, V>)this);
        }
        if (_type.isAssignableFrom(this.getClass())) {
            return (X)this;
        }
        return null;
    }
    
    public ConcurrentMap<K, V> asMap() {
        return new ConcurrentMapWrapper<K, V>(this);
    }
    
    @Override
    public StorageAdapter getStorage() {
        return null;
    }
    
    public <R> Map<K, EntryProcessingResult<R>> invokeAll(final Iterable<? extends K> keys, final EntryProcessor<K, V, R> entryProcessor) {
        final Map<K, EntryProcessingResult<R>> m = new HashMap<K, EntryProcessingResult<R>>();
        for (final K k : keys) {
            try {
                final R _result = (R)this.invoke(k, (EntryProcessor)entryProcessor);
                if (_result == null) {
                    continue;
                }
                m.put(k, new EntryProcessingResult<R>() {
                    @Override
                    public R getResult() {
                        return _result;
                    }
                    
                    @Override
                    public Throwable getException() {
                        return null;
                    }
                });
            }
            catch (EntryProcessingException t) {
                final Throwable _cause = t.getCause();
                m.put(k, new EntryProcessingResult<R>() {
                    @Override
                    public R getResult() {
                        throw new EntryProcessingException(_cause);
                    }
                    
                    @Override
                    public Throwable getException() {
                        return _cause;
                    }
                });
            }
        }
        return m;
    }
    
    protected <R> R execute(final K key, final Entry<K, V> e, final Semantic<K, V, R> op) {
        final EntryAction<K, V, R> _action = this.createEntryAction(key, e, op);
        return this.execute(op, _action);
    }
    
    protected abstract <R> EntryAction<K, V, R> createEntryAction(final K p0, final Entry<K, V> p1, final Semantic<K, V, R> p2);
    
    protected <R> R execute(final Semantic<K, V, R> op, final EntryAction<K, V, R> _action) {
        op.start(_action);
        final RuntimeException t = _action.exceptionToPropagate;
        if (t != null) {
            t.fillInStackTrace();
            throw t;
        }
        return _action.result;
    }
    
    protected <R> R execute(final K key, final Semantic<K, V, R> op) {
        return this.execute(key, null, op);
    }
    
    @Override
    public <T> T createCustomization(final CustomizationSupplier<T> f) {
        if (f == null) {
            return null;
        }
        try {
            return (T)f.supply(this.getCacheManager());
        }
        catch (Exception ex) {
            throw new CustomizationException("Initialization of customization failed", ex);
        }
    }
    
    @Override
    public void closeCustomization(final Object _customization, final String _customizationName) {
        if (_customization instanceof Closeable) {
            try {
                ((Closeable)_customization).close();
            }
            catch (Exception e) {
                final String _message = _customizationName + ".close() exception (" + nameQualifier((Cache)this) + ")";
                throw new CacheException(_message, (Throwable)e);
            }
        }
    }
    
    public void clearAndClose() {
        this.close();
    }
    
    @Override
    public String toString() {
        try {
            final InternalCacheInfo fo = this.getLatestInfo();
            return fo.toString();
        }
        catch (CacheClosedException ex) {
            return "Cache(name=" + nameQualifier((Cache)this) + ", closed=true)";
        }
    }
    
    public CacheInfoMXBean getStatistics() {
        return new CacheMXBeanImpl(this);
    }
}
