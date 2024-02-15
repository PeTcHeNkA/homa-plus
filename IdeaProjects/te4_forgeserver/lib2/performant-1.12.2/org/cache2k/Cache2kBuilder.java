//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k;

import java.lang.reflect.*;
import org.cache2k.event.*;
import org.cache2k.expiry.*;
import org.cache2k.integration.*;
import org.cache2k.configuration.*;
import java.util.concurrent.*;

public class Cache2kBuilder<K, V>
{
    private static final String MSG_NO_TYPES = "Use Cache2kBuilder.forUnknownTypes(), to construct a builder with no key and value types";
    private CacheType<K> keyType;
    private CacheType<V> valueType;
    private Cache2kConfiguration<K, V> config;
    private CacheManager manager;
    
    public static Cache2kBuilder forUnknownTypes() {
        return new Cache2kBuilder(null, null);
    }
    
    public static <K, T> Cache2kBuilder<K, T> of(final Class<K> _keyType, final Class<T> _valueType) {
        return new Cache2kBuilder<K, T>(CacheTypeCapture.of(_keyType), CacheTypeCapture.of(_valueType));
    }
    
    public static <K, T> Cache2kBuilder<K, T> of(final Cache2kConfiguration<K, T> c) {
        final Cache2kBuilder<K, T> cb = new Cache2kBuilder<K, T>(c);
        return cb;
    }
    
    private Cache2kBuilder(final Cache2kConfiguration<K, V> cfg) {
        this.config = null;
        this.manager = null;
        this.withConfig(cfg);
    }
    
    public Cache2kBuilder() {
        this.config = null;
        this.manager = null;
        final Type t = this.getClass().getGenericSuperclass();
        if (!(t instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Use Cache2kBuilder.forUnknownTypes(), to construct a builder with no key and value types");
        }
        final Type[] _types = ((ParameterizedType)t).getActualTypeArguments();
        this.keyType = CacheTypeCapture.of(_types[0]).getBeanRepresentation();
        this.valueType = CacheTypeCapture.of(_types[1]).getBeanRepresentation();
        if (Object.class.equals(this.keyType.getType()) && Object.class.equals(this.valueType.getType())) {
            throw new IllegalArgumentException("Use Cache2kBuilder.forUnknownTypes(), to construct a builder with no key and value types");
        }
    }
    
    private Cache2kBuilder(final CacheType<K> _keyType, final CacheType<V> _valueType) {
        this.config = null;
        this.manager = null;
        this.keyType = _keyType;
        this.valueType = _valueType;
    }
    
    private void withConfig(final Cache2kConfiguration<K, V> cfg) {
        this.config = cfg;
    }
    
    private Cache2kConfiguration<K, V> config() {
        if (this.config == null) {
            if (this.manager == null) {
                this.manager = CacheManager.getInstance();
            }
            this.config = (Cache2kConfiguration<K, V>)CacheManager.PROVIDER.getDefaultConfiguration(this.manager);
            if (this.keyType != null) {
                this.config.setKeyType(this.keyType);
            }
            if (this.valueType != null) {
                this.config.setValueType(this.valueType);
            }
        }
        return this.config;
    }
    
    public final Cache2kBuilder<K, V> manager(final CacheManager manager) {
        if (this.manager != null) {
            throw new IllegalStateException("manager() must be first operation on builder.");
        }
        this.manager = manager;
        return this;
    }
    
    public final <K2> Cache2kBuilder<K2, V> keyType(final Class<K2> t) {
        final Cache2kBuilder<K2, V> me = (Cache2kBuilder<K2, V>)this;
        me.config().setKeyType(t);
        return me;
    }
    
    public final <T2> Cache2kBuilder<K, T2> valueType(final Class<T2> t) {
        final Cache2kBuilder<K, T2> me = (Cache2kBuilder<K, T2>)this;
        me.config().setValueType(t);
        return me;
    }
    
    public final <K2> Cache2kBuilder<K2, V> keyType(final CacheType<K2> t) {
        final Cache2kBuilder<K2, V> me = (Cache2kBuilder<K2, V>)this;
        me.config().setKeyType(t);
        return me;
    }
    
    public final <T2> Cache2kBuilder<K, T2> valueType(final CacheType<T2> t) {
        final Cache2kBuilder<K, T2> me = (Cache2kBuilder<K, T2>)this;
        me.config().setValueType(t);
        return me;
    }
    
    public final Cache2kBuilder<K, V> name(final String _uniqueName, final Class<?> _class, final String _fieldName) {
        if (_fieldName == null) {
            throw new NullPointerException();
        }
        if (_uniqueName == null) {
            return this.name(_class, _fieldName);
        }
        this.config().setName(_uniqueName + '~' + _class.getName() + "." + _fieldName);
        return this;
    }
    
    public final Cache2kBuilder<K, V> name(final Class<?> _class, final String _fieldName) {
        if (_fieldName == null) {
            throw new NullPointerException();
        }
        this.config().setName(_class.getName() + "." + _fieldName);
        return this;
    }
    
    public final Cache2kBuilder<K, V> name(final Class<?> _class) {
        this.config().setName(_class.getName());
        return this;
    }
    
    public final Cache2kBuilder<K, V> name(final String v) {
        this.config().setName(v);
        return this;
    }
    
    public final Cache2kBuilder<K, V> keepDataAfterExpired(final boolean v) {
        this.config().setKeepDataAfterExpired(v);
        return this;
    }
    
    public final Cache2kBuilder<K, V> entryCapacity(final long v) {
        this.config().setEntryCapacity(v);
        return this;
    }
    
    public final Cache2kBuilder<K, V> eternal(final boolean v) {
        this.config().setEternal(v);
        return this;
    }
    
    public final Cache2kBuilder<K, V> suppressExceptions(final boolean v) {
        this.config().setSuppressExceptions(v);
        return this;
    }
    
    public final Cache2kBuilder<K, V> expireAfterWrite(final long v, final TimeUnit u) {
        this.config().setExpireAfterWrite(u.toMillis(v));
        return this;
    }
    
    public final Cache2kBuilder<K, V> exceptionPropagator(final ExceptionPropagator<K> ep) {
        this.config().setExceptionPropagator(wrapCustomizationInstance(ep));
        return this;
    }
    
    private static <T> CustomizationReferenceSupplier<T> wrapCustomizationInstance(final T obj) {
        if (obj == null) {
            return null;
        }
        return new CustomizationReferenceSupplier<T>(obj);
    }
    
    public final Cache2kBuilder<K, V> loader(final FunctionalCacheLoader<K, V> l) {
        this.config().setLoader(wrapCustomizationInstance(l));
        return this;
    }
    
    public final Cache2kBuilder<K, V> loader(final CacheLoader<K, V> l) {
        this.config().setLoader(wrapCustomizationInstance(l));
        return this;
    }
    
    public final Cache2kBuilder<K, V> loader(final AdvancedCacheLoader<K, V> l) {
        this.config().setAdvancedLoader(wrapCustomizationInstance(l));
        return this;
    }
    
    public final Cache2kBuilder<K, V> loader(final AsyncCacheLoader<K, V> l) {
        this.config().setAsyncLoader(wrapCustomizationInstance(l));
        return this;
    }
    
    public final Cache2kBuilder<K, V> wrappingLoader(final AdvancedCacheLoader<K, LoadDetail<V>> l) {
        throw new UnsupportedOperationException();
        //this.config().setAdvancedLoader(wrapCustomizationInstance(l));
        //return this;
    }
    
    public final Cache2kBuilder<K, V> wrappingLoader(final FunctionalCacheLoader<K, LoadDetail<V>> l) {
        throw new UnsupportedOperationException();
//        this.config().setAdvancedLoader(wrapCustomizationInstance(l));
//        return this;
    }
    
    public final Cache2kBuilder<K, V> wrappingLoader(final CacheLoader<K, LoadDetail<V>> l) {
        throw new UnsupportedOperationException();
//        this.config().setAdvancedLoader(wrapCustomizationInstance(l));
//        return this;
    }
    
    public final Cache2kBuilder<K, V> writer(final CacheWriter<K, V> w) {
        this.config().setWriter(wrapCustomizationInstance(w));
        return this;
    }
    
    public final Cache2kBuilder<K, V> addCacheClosedListener(final CacheClosedListener listener) {
        this.config().getCacheClosedListeners().add((CustomizationSupplier<CacheClosedListener>)wrapCustomizationInstance(listener));
        return this;
    }
    
    public final Cache2kBuilder<K, V> addListener(final CacheEntryOperationListener<K, V> listener) {
        this.config().getListeners().add((CustomizationSupplier<CacheEntryOperationListener<K, V>>)wrapCustomizationInstance(listener));
        return this;
    }
    
    public final Cache2kBuilder<K, V> addAsyncListener(final CacheEntryOperationListener<K, V> listener) {
        this.config().getAsyncListeners().add((CustomizationSupplier<CacheEntryOperationListener<K, V>>)wrapCustomizationInstance(listener));
        return this;
    }
    
    public final Cache2kBuilder<K, V> expiryPolicy(final ExpiryPolicy<K, V> c) {
        this.config().setExpiryPolicy(wrapCustomizationInstance(c));
        return this;
    }
    
    public final Cache2kBuilder<K, V> refreshAhead(final boolean f) {
        this.config().setRefreshAhead(f);
        return this;
    }
    
    public final Cache2kBuilder<K, V> sharpExpiry(final boolean f) {
        this.config().setSharpExpiry(f);
        return this;
    }
    
    public final Cache2kBuilder<K, V> loaderThreadCount(final int v) {
        this.config().setLoaderThreadCount(v);
        return this;
    }
    
    public final Cache2kBuilder<K, V> storeByReference(final boolean v) {
        this.config().setStoreByReference(v);
        return this;
    }
    
    public final Cache2kBuilder<K, V> retryInterval(final long v, final TimeUnit u) {
        this.config().setRetryInterval(u.toMillis(v));
        return this;
    }
    
    public final Cache2kBuilder<K, V> maxRetryInterval(final long v, final TimeUnit u) {
        this.config().setMaxRetryInterval(u.toMillis(v));
        return this;
    }
    
    public final Cache2kBuilder<K, V> resilienceDuration(final long v, final TimeUnit u) {
        this.config().setResilienceDuration(u.toMillis(v));
        return this;
    }
    
    public final Cache2kBuilder<K, V> resiliencePolicy(final ResiliencePolicy<K, V> v) {
        this.config().setResiliencePolicy(wrapCustomizationInstance(v));
        return this;
    }
    
    public final Cache2kBuilder<K, V> with(final ConfigurationSectionBuilder<? extends ConfigurationSection>... sectionBuilders) {
        for (final ConfigurationSectionBuilder<? extends ConfigurationSection> b : sectionBuilders) {
            this.config().getSections().add((ConfigurationSection)b.buildConfigurationSection());
        }
        return this;
    }
    
    public final Cache2kBuilder<K, V> strictEviction(final boolean flag) {
        this.config().setStrictEviction(flag);
        return this;
    }
    
    public final Cache2kBuilder<K, V> permitNullValues(final boolean flag) {
        this.config().setPermitNullValues(flag);
        return this;
    }
    
    public final Cache2kBuilder<K, V> disableStatistics(final boolean flag) {
        this.config().setDisableStatistics(flag);
        return this;
    }
    
    @Deprecated
    public final Cache2kBuilder<K, V> disableLastModificationTime(final boolean flag) {
        return this;
    }
    
    public final Cache2kBuilder<K, V> recordRefreshedTime(final boolean flag) {
        this.config().setRecordRefreshedTime(flag);
        return this;
    }
    
    public final Cache2kBuilder<K, V> boostConcurrency(final boolean f) {
        this.config().setBoostConcurrency(f);
        return this;
    }
    
    public final Cache2kBuilder<K, V> enableJmx(final boolean f) {
        this.config().setEnableJmx(f);
        return this;
    }
    
    public final Cache2kBuilder<K, V> loaderExecutor(final Executor v) {
        this.config().setLoaderExecutor(new CustomizationReferenceSupplier<Executor>(v));
        return this;
    }
    
    public final Cache2kBuilder<K, V> prefetchExecutor(final Executor v) {
        this.config().setPrefetchExecutor(new CustomizationReferenceSupplier<Executor>(v));
        return this;
    }
    
    public final Cache2kBuilder<K, V> asyncListenerExecutor(final Executor v) {
        this.config().setAsyncListenerExecutor(new CustomizationReferenceSupplier<Executor>(v));
        return this;
    }
    
    public final Cache2kBuilder<K, V> timeReference(final TimeReference v) {
        this.config().setTimeReference(new CustomizationReferenceSupplier<TimeReference>(v));
        return this;
    }
    
    public final Cache2kBuilder<K, V> weigher(final Weigher<K, V> v) {
        this.config().setWeigher(new CustomizationReferenceSupplier<Weigher>(v));
        return this;
    }
    
    public final Cache2kBuilder<K, V> maximumWeight(final long v) {
        this.config().setMaximumWeight(v);
        return this;
    }
    
    public final Cache2kConfiguration<K, V> toConfiguration() {
        return this.config();
    }
    
    public final CacheManager getManager() {
        return this.manager;
    }
    
    public final Cache<K, V> build() {
        return CacheManager.PROVIDER.createCache(this.manager, this.config());
    }
    
    public final IntCache<V> buildForIntKey() {
        final Cache2kConfiguration<K, V> cfg = this.config();
        if (cfg.getKeyType().getType() != Integer.class) {
            throw new IllegalArgumentException("Integer key type expected, was: " + cfg.getKeyType());
        }
        return (IntCache<V>)(IntCache)CacheManager.PROVIDER.createCache(this.manager, this.config());
    }
    
    public final LongCache<V> buildForLongKey() {
        final Cache2kConfiguration<K, V> cfg = this.config();
        if (cfg.getKeyType().getType() != Long.class) {
            throw new IllegalArgumentException("Long key type expected, was: " + cfg.getKeyType());
        }
        return (LongCache<V>)(LongCache)CacheManager.PROVIDER.createCache(this.manager, this.config());
    }
}
