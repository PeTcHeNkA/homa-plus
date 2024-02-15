//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.processor;

public interface EntryProcessor<K, V, R>
{
    R process(final MutableCacheEntry<K, V> p0) throws Exception;
}
