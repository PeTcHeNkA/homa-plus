//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.event;

import org.cache2k.*;

public interface CacheEntryExpiredListener<K, V> extends CacheEntryOperationListener<K, V>
{
    void onEntryExpired(final Cache<K, V> p0, final CacheEntry<K, V> p1);
}
