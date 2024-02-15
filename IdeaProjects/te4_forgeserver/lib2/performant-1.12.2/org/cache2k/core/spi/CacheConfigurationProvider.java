//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.spi;

import org.cache2k.*;
import org.cache2k.configuration.*;

public interface CacheConfigurationProvider
{
    String getDefaultManagerName(final ClassLoader p0);
    
    Cache2kConfiguration getDefaultConfiguration(final CacheManager p0);
    
     <K, V> void augmentConfiguration(final CacheManager p0, final Cache2kConfiguration<K, V> p1);
    
    Iterable<String> getConfiguredCacheNames(final CacheManager p0);
}
