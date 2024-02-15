//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.spi.*;
import org.cache2k.core.spi.*;
import org.cache2k.core.util.*;
import java.lang.reflect.*;
import org.cache2k.configuration.*;
import java.util.*;
import java.util.concurrent.*;
import org.cache2k.*;

public class CacheManagerImpl extends CacheManager
{
    public static final Cache2kCoreProvider PROVIDER;
    private static final Iterable<CacheLifeCycleListener> cacheLifeCycleListeners;
    private static final Iterable<CacheManagerLifeCycleListener> cacheManagerLifeCycleListeners;
    private final Object lock;
//    private Log log;
    private final String name;
    private Map<String, InternalCache> cacheNames;
    private final Properties properties;
    private final ClassLoader classLoader;
    private boolean defaultManager;
    private Cache2kCoreProviderImpl provider;
    private boolean closing;
    
    private static <S> Iterable<S> constructAllServiceImplementations(final Class<S> _service) {
        final ClassLoader cl = CacheManagerImpl.class.getClassLoader();
        final ArrayList<S> li = new ArrayList<S>();
        final Iterator<S> it = ServiceLoader.load(_service, cl).iterator();
        while (it.hasNext()) {
            try {
                li.add(it.next());
            }
            catch (ServiceConfigurationError ex) {
                Log.getLog(CacheManager.class.getName()).debug("Error loading service '" + _service + "'", ex);
            }
        }
        final S[] a = (S[])Array.newInstance(_service, li.size());
        li.toArray(a);
        return new Iterable<S>() {
            @Override
            public Iterator<S> iterator() {
                return new Iterator<S>() {
                    private int pos = 0;
                    
                    @Override
                    public boolean hasNext() {
                        return this.pos < a.length;
                    }
                    
                    @Override
                    public S next() {
                        return a[this.pos++];
                    }
                    
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
    
    public CacheManagerImpl(final Cache2kCoreProviderImpl _provider, final ClassLoader cl, final String _name, final boolean _default) {
        this.lock = new Object();
        this.cacheNames = new HashMap<String, InternalCache>();
        this.properties = new Properties();
        this.provider = _provider;
        this.defaultManager = _default;
        this.classLoader = cl;
        this.name = _name;
        //this.log = Log.getLog(CacheManager.class.getName() + '.' + this.name);
        for (final CacheManagerLifeCycleListener lc : CacheManagerImpl.cacheManagerLifeCycleListeners) {
            lc.managerCreated(this);
        }
        this.logPhase("open");
    }
    
    public static Iterable<CacheLifeCycleListener> getCacheLifeCycleListeners() {
        return CacheManagerImpl.cacheLifeCycleListeners;
    }
    
    public void sendCreatedEvent(final Cache c, final Cache2kConfiguration _configuration) {
        for (final CacheLifeCycleListener e : CacheManagerImpl.cacheLifeCycleListeners) {
            e.cacheCreated(c, _configuration);
        }
    }
    
    private void sendDestroyedEvent(final Cache c) {
        for (final CacheLifeCycleListener e : CacheManagerImpl.cacheLifeCycleListeners) {
            e.cacheDestroyed(c);
        }
    }
    
    public static void checkName(final String s) {
        for (final char c : s.toCharArray()) {
            if (c != '.' && c != '-' && c != '~' && c != ',' && c != '@' && c != ' ' && c != '(' && c != ')' && c != '+' && c != '!' && c != '\'' && c != '%') {
                if (c != '#') {
                    if (c < ' ' || c >= '\u007f' || !Character.isJavaIdentifierPart(c)) {
                        throw new IllegalArgumentException("Cache name contains illegal character: '" + c + "', name=\"" + s + "\"");
                    }
                }
            }
        }
    }
    
    public String newCache(final InternalCache c, final String _requestedName) {
        synchronized (this.lock) {
            this.checkClosed();
            final String _name = _requestedName;
            if (this.cacheNames.containsKey(_name)) {
                throw new IllegalStateException("Cache already created: '" + _requestedName + "'");
            }
            checkName(_name);
            this.cacheNames.put(_name, c);
            return _name;
        }
    }
    
    public void cacheDestroyed(final Cache c) {
        synchronized (this.lock) {
            this.cacheNames.remove(c.getName());
            this.sendDestroyedEvent(c);
        }
    }
    
    public boolean isDefaultManager() {
        return this.defaultManager;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Iterable<Cache> getActiveCaches() {
        return this.cachesCopy();
    }
    
    private Iterable<Cache> cachesCopy() {
        final Set<Cache> _caches = new HashSet<Cache>();
        synchronized (this.lock) {
            if (!this.isClosed()) {
                for (final Cache c : this.cacheNames.values()) {
                    if (!c.isClosed()) {
                        _caches.add(c);
                    }
                }
            }
        }
        return _caches;
    }
    
    private Collection<String> getActiveCacheNames() {
        final Set<String> _caches = new HashSet<String>();
        synchronized (this.lock) {
            if (!this.isClosed()) {
                for (final Cache c : this.cacheNames.values()) {
                    if (!c.isClosed()) {
                        _caches.add(c.getName());
                    }
                }
            }
        }
        return _caches;
    }
    
    public Iterable<String> getConfiguredCacheNames() {
        return Cache2kCoreProviderImpl.CACHE_CONFIGURATION_PROVIDER.getConfiguredCacheNames(this);
    }
    
    public <K, V> Cache<K, V> getCache(final String name) {
        synchronized (this.lock) {
            final Cache c = (Cache)this.cacheNames.get(name);
            return (Cache<K, V>)((c != null && c.isClosed()) ? null : c);
        }
    }
    
    public <K, V> Cache<K, V> createCache(final Cache2kConfiguration<K, V> cfg) {
        return (Cache<K, V>)Cache2kBuilder.of((Cache2kConfiguration)cfg).manager((CacheManager)this).build();
    }
    
    public void clear() {
        for (final Cache c : this.cachesCopy()) {
            try {
                c.clear();
            }
            catch (CacheClosedException ex) {}
        }
    }
    
    public void close() {
        if (this.isDefaultManager() && this.getClass().getClassLoader() == this.classLoader) {
            System.out.println("Closing default CacheManager");
        }
        final Iterable<Cache> _caches;
        synchronized (this.lock) {
            if (this.closing) {
                return;
            }
            _caches = this.cachesCopy();
            this.closing = true;
        }
        this.logPhase("close");
        final List<Throwable> _suppressedExceptions = new ArrayList<>();
        for (final Cache c : _caches) {
            ((InternalCache)c).cancelTimerJobs();
        }
        for (final Cache c : _caches) {
            try {
                c.close();
            }
            catch (Throwable t) {
                _suppressedExceptions.add(t);
            }
        }
        try {
            for (final CacheManagerLifeCycleListener lc : CacheManagerImpl.cacheManagerLifeCycleListeners) {
                lc.managerDestroyed(this);
            }
        }
        catch (Throwable t2) {
            _suppressedExceptions.add(t2);
        }
        ((Cache2kCoreProviderImpl)CacheManagerImpl.PROVIDER).removeManager((CacheManager)this);
        synchronized (this.lock) {
            for (final Cache c2 : this.cacheNames.values()) {
                System.err.println("unable to close cache: " + c2.getName());
            }
        }
        eventuallyThrowException(_suppressedExceptions);
        this.cacheNames = null;
    }
    
    static void eventuallyThrowException(final List<Throwable> _suppressedExceptions) {
        if (_suppressedExceptions.isEmpty()) {
            return;
        }
        Throwable _error = null;
        for (final Throwable t : _suppressedExceptions) {
            if (t instanceof Error) {
                _error = t;
                break;
            }
            if (t instanceof ExecutionException && t.getCause() instanceof Error) {
                _error = t.getCause();
                break;
            }
        }
        String _text = "Exception(s) during shutdown";
        if (_suppressedExceptions.size() > 1) {
            _text = " (" + (_suppressedExceptions.size() - 1) + " more suppressed exceptions)";
        }
        if (_error != null) {
            throw new CacheInternalError(_text, _error);
        }
        throw new CacheException(_text, (Throwable)_suppressedExceptions.get(0));
    }
    
    public Properties getProperties() {
        return this.properties;
    }
    
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
    
    public String getVersion() {
        return this.provider.getVersion();
    }
    
    public Cache2kCoreProviderImpl getProvider() {
        return this.provider;
    }
    
    public boolean isClosed() {
        return this.closing;
    }
    
    public Object getLockObject() {
        return this.lock;
    }
    
    private void checkClosed() {
        if (this.closing) {
            throw new IllegalStateException("CacheManager already closed");
        }
    }
    
    private void logPhase(final String _phase) {
    }
    
    private String getManagerId() {
        return "name='" + this.name + "', objectId=" + Integer.toString(System.identityHashCode(this), 36) + ", classloaderId=" + Integer.toString(System.identityHashCode(this.classLoader), 36) + ", default=" + this.defaultManager;
    }
    
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CacheManager(");
        sb.append(this.getManagerId());
        if (this.isClosed()) {
            sb.append(", closed=true");
        }
        else {
            sb.append(", activeCaches=");
            sb.append(this.getActiveCacheNames());
        }
        return sb.append(')').toString();
    }
    
    static {
        PROVIDER = CacheManager.PROVIDER;
        cacheLifeCycleListeners = constructAllServiceImplementations(CacheLifeCycleListener.class);
        cacheManagerLifeCycleListeners = constructAllServiceImplementations(CacheManagerLifeCycleListener.class);
    }
}
