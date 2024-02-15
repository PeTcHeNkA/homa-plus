//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import java.util.concurrent.*;
import org.cache2k.processor.*;
import org.cache2k.*;
import java.util.*;

public class ConcurrentMapWrapper<K, V> implements ConcurrentMap<K, V>
{
    private boolean permitNull;
    private Cache<K, V> cache;
    private Class<?> keyType;
    private Class<?> valueType;

    public ConcurrentMapWrapper(final InternalCache<K, V> cache) {
        this.cache = (org.cache2k.Cache<K, V>)cache;
        this.permitNull = cache.isNullValuePermitted();
        this.keyType = (Class<?>)cache.getKeyType().getType();
        this.valueType = (Class<?>)cache.getValueType().getType();
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        return (V)this.cache.invoke(key, (EntryProcessor) e -> {
            if (!e.exists()) {
                e.setValue(value);
                return null;
            }
            return e.getValue();
        });
    }


    @Override
    public boolean remove(final Object key, final Object value) {
        return this.keyType.isAssignableFrom(key.getClass()) &&
                this.valueType.isAssignableFrom(value.getClass())
                && this.cache.removeIfEquals((K) key, (V) value);
    }


    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        return this.cache.replaceIfEquals(key, oldValue, newValue);
    }


    @Override
    public V replace(final K key, final V value) {
        final EntryProcessor<K, V, V> p = e -> {
            if (e.exists()) {
                final V result = e.getValue();
                e.setValue(value);
                return result;
            }
            return null;
        };
        return (V)this.cache.invoke(key, (EntryProcessor)p);
    }


    @Override
    public int size() {
        return ((InternalCache<?, ?>)this.cache).getTotalEntryCount();
    }


    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }


    @Override
    public boolean containsKey(final Object key) {
        return this.keyType.isAssignableFrom(key.getClass()) && this.cache.containsKey((K) key);
    }


    @Override
    public boolean containsValue(final Object value) {
        if (value == null) {
            if (!this.permitNull) {
                throw new NullPointerException();
            }
            for (final CacheEntry<K, V> e : this.cache.entries()) {
                if (e.getValue() == null) {
                    return true;
                }
            }
        }
        else {
            for (final CacheEntry<K, V> e : this.cache.entries()) {
                if (value.equals(e.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public V get(final Object key) {
        if (this.keyType.isAssignableFrom(key.getClass())) {
            return (V)this.cache.peek((K) key);
        }
        return null;
    }


    @Override
    public V put(final K key, final V value) {
        final EntryProcessor<K, V, V> p = new EntryProcessor<K, V, V>() {
            @Override
            public V process(final MutableCacheEntry<K, V> e) {
                final V result = e.getValue();
                e.setValue(value);
                return result;
            }
        };
        return (V)this.cache.invoke(key, (EntryProcessor)p);
    }

    @Override
    public V remove(final Object key) {
        if (!this.keyType.isAssignableFrom(key.getClass())) {
            return null;
        }
        final EntryProcessor<K, V, V> p = e -> {
            final V result = e.getValue();
            e.remove();
            return result;
        };
        return (V)this.cache.invoke((K) key, (EntryProcessor)p);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        this.cache.putAll((Map)m);
    }

    @Override
    public void clear() {
        this.cache.clear();
    }

    @Override
    public Set<K> keySet() {
        return new AbstractSet<K>() {
            @Override
            public Iterator<K> iterator() {
                final Iterator<CacheEntry<K, V>> it = ConcurrentMapWrapper.this.cache.entries().iterator();
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
                        it.remove();
                    }
                };
            }

            @Override
            public boolean contains(final Object o) {
                return ConcurrentMapWrapper.this.containsKey(o);
            }

            @Override
            public int size() {
                return ConcurrentMapWrapper.this.size();
            }
        };
    }

    @Override
    public Collection<V> values() {
        return new AbstractSet<V>() {
            @Override
            public Iterator<V> iterator() {
                final Iterator<CacheEntry<K, V>> it = ConcurrentMapWrapper.this.cache.entries().iterator();
                return new Iterator<V>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public V next() {
                        return (V)it.next().getValue();
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }

            @Override
            public int size() {
                return ConcurrentMapWrapper.this.size();
            }
        };
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K, V>>() {
            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                final Iterator<CacheEntry<K, V>> it = ConcurrentMapWrapper.this.cache.entries().iterator();
                return new Iterator<Map.Entry<K, V>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public Map.Entry<K, V> next() {
                        final CacheEntry<K, V> e = (CacheEntry<K, V>)it.next();
                        return new Map.Entry<K, V>() {
                            @Override
                            public K getKey() {
                                return (K)e.getKey();
                            }

                            @Override
                            public V getValue() {
                                return (V)e.getValue();
                            }

                            @Override
                            public V setValue(final V value) {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }

            @Override
            public int size() {
                return ConcurrentMapWrapper.this.size();
            }
        };
    }


    @Override
    public boolean equals(final Object o) {
        return o instanceof ConcurrentMapWrapper && this.cache.equals(((ConcurrentMapWrapper)o).cache);
    }


    @Override
    public int hashCode() {
        return this.cache.hashCode();
    }
}
