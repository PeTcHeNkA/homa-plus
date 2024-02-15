package org.cache2k.core;
import org.cache2k.core.spi.*;
import org.cache2k.*;
import org.cache2k.configuration.*;
import java.util.*;


public class DummyConfigurationProvider implements CacheConfigurationProvider {
    @Override
    public String getDefaultManagerName(final ClassLoader classLoader) {
        return null;
    }


    @Override
    public Cache2kConfiguration getDefaultConfiguration(final CacheManager mgr) {
        return new Cache2kConfiguration();
    }


    @Override
    public <K, V> void augmentConfiguration(final CacheManager mgr, final Cache2kConfiguration<K, V> cfg) {
    }


    @Override
    public Iterable<String> getConfiguredCacheNames(final CacheManager mgr) {
        return Collections.emptyList();
    }
}
