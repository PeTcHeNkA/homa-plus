//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.jcache;

import javax.cache.configuration.*;
import org.cache2k.configuration.*;
import org.cache2k.*;

public final class ExtendedMutableConfiguration<K, V> extends MutableConfiguration<K, V> implements ExtendedConfiguration<K, V>
{
    private Cache2kConfiguration<K, V> cache2kConfiguration;
    
    public static <K, V> ExtendedMutableConfiguration<K, V> of(final Cache2kBuilder<K, V> builder) {
        return of((org.cache2k.configuration.Cache2kConfiguration<K, V>)builder.toConfiguration());
    }
    
    public static <K, V> ExtendedMutableConfiguration<K, V> of(final Cache2kConfiguration<K, V> configuration) {
        final ExtendedMutableConfiguration<K, V> cfg = new ExtendedMutableConfiguration<K, V>();
        cfg.cache2kConfiguration = configuration;
        return cfg;
    }
    
    public Cache2kConfiguration<K, V> getCache2kConfiguration() {
        return this.cache2kConfiguration;
    }
    
    public void setCache2kConfiguration(final Cache2kConfiguration<K, V> _cache2kConfiguration) {
        this.cache2kConfiguration = _cache2kConfiguration;
    }
}
