//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.integration;

public class Loaders
{
    public static <V> LoadDetail<V> wrapRefreshedTime(final V value, final long refreshedTimeInMillis) {
        return new RefreshedTimeWrapper<V>(value, refreshedTimeInMillis);
    }
    
    public static <V> LoadDetail<V> wrapRefreshedTime(final LoadDetail<V> value, final long refreshedTimeInMillis) {
        return new RefreshedTimeWrapper<V>(value, refreshedTimeInMillis);
    }
}
