//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k;

public interface IntCache<V> extends Cache<Integer, V>, IntKeyValueStore<V>
{
    V peek(final int p0);
    
    boolean containsKey(final int p0);
}
