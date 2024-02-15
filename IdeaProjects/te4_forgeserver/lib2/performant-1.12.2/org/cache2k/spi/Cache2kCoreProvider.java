//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.spi;

import org.cache2k.configuration.*;
import org.cache2k.*;

public interface Cache2kCoreProvider
{
    void setDefaultManagerName(final ClassLoader p0, final String p1);
    
    String getDefaultManagerName(final ClassLoader p0);
    
    CacheManager getManager(final ClassLoader p0, final String p1);
    
    ClassLoader getDefaultClassLoader();
    
    void close();
    
    void close(final ClassLoader p0);
    
    void close(final ClassLoader p0, final String p1);
    
     <K, V> Cache<K, V> createCache(final CacheManager p0, final Cache2kConfiguration<K, V> p1);
    
    Cache2kConfiguration getDefaultConfiguration(final CacheManager p0);
}
