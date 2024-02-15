//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k;

public interface IntKeyValueStore<V> extends AdvancedKeyValueSource<Integer, V>, IntKeyValueSource<V>
{
    void put(final int p0, final V p1);
    
    void remove(final int p0);
}
