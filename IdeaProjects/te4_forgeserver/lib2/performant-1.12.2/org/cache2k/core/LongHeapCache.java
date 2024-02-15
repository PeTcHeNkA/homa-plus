//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.*;

public class LongHeapCache<V> extends HeapCache<Long, V> implements LongCache<V>
{
    public V peek(final long key) {
        return (V)super.peek(key);
    }
    
    public boolean containsKey(final long key) {
        return super.containsKey(key);
    }
    
    public void put(final long key, final V value) {
        super.put(key, value);
    }
    
    public V get(final long key) {
        return (V)super.get(key);
    }
    
    public void remove(final long key) {
        super.remove(key);
    }
}
