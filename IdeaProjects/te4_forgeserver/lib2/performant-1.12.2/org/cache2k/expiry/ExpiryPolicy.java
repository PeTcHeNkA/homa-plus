//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.expiry;

import org.cache2k.*;

public interface ExpiryPolicy<K, V> extends ExpiryTimeValues
{
    long calculateExpiryTime(final K p0, final V p1, final long p2, final CacheEntry<K, V> p3);
}
