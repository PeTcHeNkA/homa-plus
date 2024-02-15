package org.cache2k.core;

import org.cache2k.core.spi.*;
import org.cache2k.core.util.*;
import java.util.*;
import org.cache2k.configuration.*;
import org.cache2k.*;
import org.cache2k.spi.*;


public class Cache2kCoreProviderImpl implements Cache2kCoreProvider {
    public static final CacheConfigurationProvider CACHE_CONFIGURATION_PROVIDER;
    private final Object lock = new Object();
    private volatile Map<ClassLoader, String> loader2defaultName;
    private volatile Map<ClassLoader, Map<String, CacheManager>> loader2name2manager;
    private String version;
    
    public Cache2kCoreProviderImpl() {
        this.loader2defaultName = Collections.emptyMap();
        this.loader2name2manager = Collections.emptyMap();
        this.extractVersionAndGreet();
        this.registerExtensions();
    }
    
    private void extractVersionAndGreet() {
        this.version = Cache2kVersion.getVersion();
        System.out.println("Cache2k version: " + this.version);
    }
    
    private void registerExtensions() {
        final Iterator<Cache2kExtensionProvider> it = ServiceLoader.load(Cache2kExtensionProvider.class, CacheManager.class.getClassLoader()).iterator();
        while (it.hasNext()) {
            try {
                it.next().registerCache2kExtension();
            }
            catch (ServiceConfigurationError ex) {
                Log.getLog(CacheManager.class.getName()).debug("Error loading cache2k extension", ex);
            }
        }
    }
    
    public Object getLockObject() {
        return this.lock;
    }
    
    @Override
    public void setDefaultManagerName(final ClassLoader cl, final String s) {
        synchronized (this.getLockObject()) {
            if (this.loader2defaultName.containsKey(cl)) {
                throw new IllegalStateException("a CacheManager was already created");
            }
            this.replaceDefaultName(cl, s);
        }
    }
    
    @Override
    public String getDefaultManagerName(final ClassLoader cl) {
        String n = this.loader2defaultName.get(cl);
        if (n != null) {
            return n;
        }
        synchronized (this.getLockObject()) {
            n = Cache2kCoreProviderImpl.CACHE_CONFIGURATION_PROVIDER.getDefaultManagerName(cl);
            if (n == null) {
                n = "default";
            }
            this.replaceDefaultName(cl, n);
        }
        return n;
    }
    
    private void replaceDefaultName(final ClassLoader cl, final String s) {
        final Map<ClassLoader, String> _copy = new WeakHashMap<ClassLoader, String>(this.loader2defaultName);
        _copy.put(cl, s);
        this.loader2defaultName = _copy;
    }
    
    @Override
    public ClassLoader getDefaultClassLoader() {
        return this.getClass().getClassLoader();
    }
    
    @Override
    public CacheManager getManager(final ClassLoader cl, final String _name) {
        CacheManagerImpl.checkName(_name);
        if (cl == null) {
            throw new NullPointerException("classloader is null");
        }
        return this.getManager(cl, _name, this.getDefaultManagerName(cl).equals(_name));
    }
    
    public CacheManager getManager(final ClassLoader cl, final String _name, final boolean _default) {
        Map<String, CacheManager> _loader2managers = this.loader2name2manager.get(cl);
        if (_loader2managers != null) {
            final CacheManager mgr = _loader2managers.get(_name);
            if (mgr != null) {
                return mgr;
            }
        }
        CacheManager mgr;
        synchronized (this.getLockObject()) {
            _loader2managers = this.loader2name2manager.get(cl);
            if (_loader2managers != null) {
                mgr = _loader2managers.get(_name);
                if (mgr != null) {
                    return mgr;
                }
            }
            if (_loader2managers != null) {
                _loader2managers = new HashMap<String, CacheManager>(_loader2managers);
            }
            else {
                _loader2managers = new HashMap<String, CacheManager>();
            }
            mgr = new CacheManagerImpl(this, cl, _name, _default);
            _loader2managers.put(_name, mgr);
            final Map<ClassLoader, Map<String, CacheManager>> _copy = new WeakHashMap<ClassLoader, Map<String, CacheManager>>(this.loader2name2manager);
            _copy.put(cl, _loader2managers);
            this.loader2name2manager = _copy;
        }
        return mgr;
    }
    
    void removeManager(final CacheManager cm) {
        synchronized (this.getLockObject()) {
            Map<String, CacheManager> _name2managers = this.loader2name2manager.get(cm.getClassLoader());
            _name2managers = new HashMap<String, CacheManager>(_name2managers);
            final Object _removed = _name2managers.remove(cm.getName());
            final Map<ClassLoader, Map<String, CacheManager>> _copy = new WeakHashMap<ClassLoader, Map<String, CacheManager>>(this.loader2name2manager);
            _copy.put(cm.getClassLoader(), _name2managers);
            this.loader2name2manager = _copy;
            if (cm.isDefaultManager()) {
                final Map<ClassLoader, String> _defaultNameCopy = new WeakHashMap<ClassLoader, String>(this.loader2defaultName);
                _defaultNameCopy.remove(cm.getClassLoader());
                this.loader2defaultName = _defaultNameCopy;
            }
        }
    }
    
    @Override
    public void close(final ClassLoader l) {
        for (final CacheManager cm : this.loader2name2manager.get(l).values()) {
            cm.close();
        }
    }
    
    @Override
    public void close() {
        for (final ClassLoader cl : this.loader2name2manager.keySet()) {
            this.close(cl);
        }
    }
    
    @Override
    public void close(ClassLoader cl, final String managerName) {
        if (cl == null) {
            cl = this.getDefaultClassLoader();
        }
        final Map<String, CacheManager> map = this.loader2name2manager.get(cl);
        if (map == null) {
            return;
        }
        final CacheManager cm = map.get(managerName);
        if (cm == null) {
            return;
        }
        cm.close();
    }
    
    @Override
    public <K, V> Cache<K, V> createCache(final CacheManager m, final Cache2kConfiguration<K, V> cfg) {
        return new InternalCache2kBuilder<K, V>(cfg, m).build();
    }
    
    public String getVersion() {
        return this.version;
    }
    
    @Override
    public Cache2kConfiguration getDefaultConfiguration(final CacheManager mgr) {
        return Cache2kCoreProviderImpl.CACHE_CONFIGURATION_PROVIDER.getDefaultConfiguration(mgr);
    }
    
    static {
        CACHE_CONFIGURATION_PROVIDER = SingleProviderResolver.resolve(CacheConfigurationProvider.class, DummyConfigurationProvider.class);
    }
}
