//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k;

import java.util.*;

public interface AdvancedKeyValueSource<K, V> extends KeyValueSource<K, V>
{
    Map<K, V> getAll(final Iterable<? extends K> p0);
    
    void prefetch(final K p0);
    
    void prefetchAll(final Iterable<? extends K> p0, final CacheOperationCompletionListener p1);
}
