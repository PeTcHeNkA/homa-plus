//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

public interface HeapCacheListener<K, V>
{
    public static final HeapCacheListener NO_OPERATION = new NoOperation();
    
    void onEvictionFromHeap(final Entry<K, V> p0);
    
    public static final class NoOperation<K, V> implements HeapCacheListener<K, V>
    {
        @Override
        public void onEvictionFromHeap(final Entry<K, V> e) {
        }
    }
}
