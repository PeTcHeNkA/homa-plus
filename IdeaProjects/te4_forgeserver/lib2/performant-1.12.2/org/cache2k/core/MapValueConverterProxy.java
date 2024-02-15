//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import java.util.*;

public abstract class MapValueConverterProxy<K, V, S> implements Map<K, V>
{
    private Map<K, S> map;
    
    public MapValueConverterProxy(final Map<K, S> _map) {
        this.map = _map;
    }
    
    protected abstract V convert(final S p0);
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int size() {
        return this.map.size();
    }
    
    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }
    
    @Override
    public boolean containsKey(final Object key) {
        return this.map.containsKey(key);
    }
    
    @Override
    public boolean containsValue(final Object value) {
        return this.values().contains(value);
    }
    
    @Override
    public V get(final Object key) {
        final S v = this.map.get(key);
        if (v == null) {
            return null;
        }
        return this.convert(v);
    }
    
    @Override
    public V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public V remove(final Object key) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }
    
    @Override
    public Collection<V> values() {
        return new AbstractCollection<V>() {
            @Override
            public Iterator<V> iterator() {
                final Iterator<S> it = MapValueConverterProxy.this.map.values().iterator();
                return new Iterator<V>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    
                    @Override
                    public V next() {
                        return MapValueConverterProxy.this.convert(it.next());
                    }
                    
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
            
            @Override
            public int size() {
                return MapValueConverterProxy.this.map.size();
            }
        };
    }
    
    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                final Iterator<Entry<K, S>> it = MapValueConverterProxy.this.map.entrySet().iterator();
                return new Iterator<Entry<K, V>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    
                    @Override
                    public Entry<K, V> next() {
                        final Entry<K, S> e = it.next();
                        return new Entry<K, V>() {
                            @Override
                            public K getKey() {
                                return e.getKey();
                            }
                            
                            @Override
                            public V getValue() {
                                return MapValueConverterProxy.this.convert(e.getValue());
                            }
                            
                            @Override
                            public V setValue(final V value) {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                    
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
            
            @Override
            public int size() {
                return MapValueConverterProxy.this.map.size();
            }
        };
    }
}
