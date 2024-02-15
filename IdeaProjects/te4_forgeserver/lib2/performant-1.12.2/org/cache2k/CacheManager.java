package org.cache2k;

import java.io.*;
import org.cache2k.configuration.*;
import java.util.*;
import org.cache2k.core.Cache2kCoreProviderImpl;
import org.cache2k.spi.*;


public abstract class CacheManager implements Closeable {
    public static final String STANDARD_DEFAULT_MANAGER_NAME = "default";
    protected static final Cache2kCoreProvider PROVIDER;
    
    public static String getDefaultName() {
        return CacheManager.PROVIDER.getDefaultManagerName(CacheManager.PROVIDER.getDefaultClassLoader());
    }
    
    public static void setDefaultName(final String managerName) {
        CacheManager.PROVIDER.setDefaultManagerName(CacheManager.PROVIDER.getDefaultClassLoader(), managerName);
    }
    
    public static CacheManager getInstance() {
        final ClassLoader _defaultClassLoader = CacheManager.PROVIDER.getDefaultClassLoader();
        return CacheManager.PROVIDER.getManager(_defaultClassLoader, CacheManager.PROVIDER.getDefaultManagerName(_defaultClassLoader));
    }
    
    public static CacheManager getInstance(final ClassLoader cl) {
        return CacheManager.PROVIDER.getManager(cl, CacheManager.PROVIDER.getDefaultManagerName(cl));
    }
    
    public static CacheManager getInstance(final String managerName) {
        return CacheManager.PROVIDER.getManager(CacheManager.PROVIDER.getDefaultClassLoader(), managerName);
    }
    
    public static CacheManager getInstance(final ClassLoader cl, final String managerName) {
        return CacheManager.PROVIDER.getManager(cl, managerName);
    }
    
    public static void closeAll() {
        CacheManager.PROVIDER.close();
    }
    
    public static void closeAll(final ClassLoader cl) {
        CacheManager.PROVIDER.close(cl);
    }
    
    public static void close(final ClassLoader cl, final String name) {
        CacheManager.PROVIDER.close(cl, name);
    }
    
    public abstract boolean isDefaultManager();
    
    public abstract String getName();
    
    public abstract Iterable<Cache> getActiveCaches();
    
    public abstract Iterable<String> getConfiguredCacheNames();
    
    public abstract <K, V> Cache<K, V> getCache(final String p0);
    
    public abstract <K, V> Cache<K, V> createCache(final Cache2kConfiguration<K, V> p0);
    
    public abstract void clear();
    
    @Override
    public abstract void close();
    
    public abstract boolean isClosed();
    
    public abstract Properties getProperties();
    
    public abstract ClassLoader getClassLoader();
    
    static {
        PROVIDER = new Cache2kCoreProviderImpl();
        //PROVIDER = SingleProviderResolver.resolveMandatory(Cache2kCoreProvider.class);
    }
}
