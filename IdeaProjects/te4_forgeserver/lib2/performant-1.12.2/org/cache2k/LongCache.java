//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k;

public interface LongCache<V> extends Cache<Long, V>, LongKeyValueStore<V>
{
    V peek(final long p0);
    
    boolean containsKey(final long p0);
}
