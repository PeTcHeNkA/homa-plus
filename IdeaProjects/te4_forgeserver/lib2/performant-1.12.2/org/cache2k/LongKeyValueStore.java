//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k;

public interface LongKeyValueStore<V> extends AdvancedKeyValueSource<Long, V>, LongKeyValueSource<V>
{
    void put(final long p0, final V p1);
    
    void remove(final long p0);
}
