//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k;

import java.util.*;

public interface KeyValueStore<K, V> extends AdvancedKeyValueSource<K, V>
{
    void put(final K p0, final V p1);
    
    void putAll(final Map<? extends K, ? extends V> p0);
    
    void remove(final K p0);
    
    void removeAll(final Iterable<? extends K> p0);
}
