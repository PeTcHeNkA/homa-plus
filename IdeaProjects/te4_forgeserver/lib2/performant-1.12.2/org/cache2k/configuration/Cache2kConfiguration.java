//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.configuration;

import java.util.concurrent.*;
import org.cache2k.expiry.*;
import org.cache2k.integration.*;
import org.cache2k.*;
import org.cache2k.event.*;
import java.util.*;

public class Cache2kConfiguration<K, V> implements ConfigurationBean, ConfigurationWithSections
{
    public static final long EXPIRY_NOT_ETERNAL = 9223372036854775806L;
    private boolean storeByReference;
    private String name;
    private CacheType<K> keyType;
    private CacheType<V> valueType;
    private long entryCapacity;
    private boolean strictEviction;
    private boolean refreshAhead;
    private long expireAfterWrite;
    private long retryInterval;
    private long maxRetryInterval;
    private long resilienceDuration;
    private long maximumWeight;
    private boolean keepDataAfterExpired;
    private boolean sharpExpiry;
    private boolean suppressExceptions;
    private int loaderThreadCount;
    private boolean permitNullValues;
    private boolean disableStatistics;
    private boolean recordRefreshedTime;
    private boolean externalConfigurationPresent;
    private boolean boostConcurrency;
    private boolean enableJmx;
    private CustomizationSupplier<Executor> loaderExecutor;
    private CustomizationSupplier<Executor> prefetchExecutor;
    private CustomizationSupplier<Executor> asyncListenerExecutor;
    private CustomizationSupplier<ExpiryPolicy<K, V>> expiryPolicy;
    private CustomizationSupplier<ResiliencePolicy<K, V>> resiliencePolicy;
    private CustomizationSupplier<? extends FunctionalCacheLoader<K, V>> loader;
    private CustomizationSupplier<CacheWriter<K, V>> writer;
    private CustomizationSupplier<AdvancedCacheLoader<K, V>> advancedLoader;
    private CustomizationSupplier<AsyncCacheLoader<K, V>> asyncLoader;
    private CustomizationSupplier<ExceptionPropagator<K>> exceptionPropagator;
    private CustomizationSupplier<TimeReference> timeReference;
    private CustomizationSupplier<Weigher> weigher;
    private CustomizationCollection<CacheEntryOperationListener<K, V>> listeners;
    private CustomizationCollection<CacheEntryOperationListener<K, V>> asyncListeners;
    private CustomizationCollection<CacheClosedListener> closedListeners;
    private ConfigurationSectionContainer sections;
    
    public Cache2kConfiguration() {
        this.entryCapacity = -1L;
        this.strictEviction = false;
        this.refreshAhead = false;
        this.expireAfterWrite = -1L;
        this.retryInterval = -1L;
        this.maxRetryInterval = -1L;
        this.resilienceDuration = -1L;
        this.maximumWeight = -1L;
        this.keepDataAfterExpired = false;
        this.sharpExpiry = false;
        this.suppressExceptions = true;
        this.permitNullValues = false;
        this.disableStatistics = false;
        this.recordRefreshedTime = false;
        this.externalConfigurationPresent = false;
        this.boostConcurrency = false;
        this.enableJmx = false;
    }
    
    public static <K, V> Cache2kConfiguration<K, V> of(final Class<K> keyType, final Class<V> valueType) {
        final Cache2kConfiguration<K, V> c = new Cache2kConfiguration<K, V>();
        c.setKeyType(keyType);
        c.setValueType(valueType);
        return c;
    }
    
    public static <K, V> Cache2kConfiguration<K, V> of(final Class<K> keyType, final CacheType<V> valueType) {
        final Cache2kConfiguration<K, V> c = new Cache2kConfiguration<K, V>();
        c.setKeyType(keyType);
        c.setValueType(valueType);
        return c;
    }
    
    public static <K, V> Cache2kConfiguration<K, V> of(final CacheType<K> keyType, final Class<V> valueType) {
        final Cache2kConfiguration<K, V> c = new Cache2kConfiguration<K, V>();
        c.setKeyType(keyType);
        c.setValueType(valueType);
        return c;
    }
    
    public static <K, V> Cache2kConfiguration<K, V> of(final CacheType<K> keyType, final CacheType<V> valueType) {
        final Cache2kConfiguration<K, V> c = new Cache2kConfiguration<K, V>();
        c.setKeyType(keyType);
        c.setValueType(valueType);
        return c;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public long getEntryCapacity() {
        return this.entryCapacity;
    }
    
    public void setEntryCapacity(final long v) {
        this.entryCapacity = v;
    }
    
    public boolean isRefreshAhead() {
        return this.refreshAhead;
    }
    
    public void setRefreshAhead(final boolean v) {
        this.refreshAhead = v;
    }
    
    public CacheType<K> getKeyType() {
        return this.keyType;
    }
    
    private void checkNull(final Object v) {
        if (v == null) {
            throw new NullPointerException("null value not allowed");
        }
    }
    
    public void setKeyType(final Class<K> v) {
        this.checkNull(v);
        this.setKeyType(CacheTypeCapture.of(v));
    }
    
    public void setKeyType(final CacheType<K> v) {
        this.checkNull(v);
        if (v.isArray()) {
            throw new IllegalArgumentException("Arrays are not supported for keys");
        }
        this.keyType = v.getBeanRepresentation();
    }
    
    public CacheType<V> getValueType() {
        return this.valueType;
    }
    
    public void setValueType(final Class<V> v) {
        this.checkNull(v);
        this.setValueType(CacheTypeCapture.of(v));
    }
    
    public void setValueType(final CacheType<V> v) {
        this.checkNull(v);
        if (v.isArray()) {
            throw new IllegalArgumentException("Arrays are not supported for values");
        }
        this.valueType = v.getBeanRepresentation();
    }
    
    public boolean isEternal() {
        return this.expireAfterWrite == -1L || this.expireAfterWrite == Long.MAX_VALUE;
    }
    
    public void setEternal(final boolean v) {
        if (v) {
            this.setExpireAfterWrite(Long.MAX_VALUE);
        }
        else {
            this.setExpireAfterWrite(9223372036854775806L);
        }
    }
    
    public long getExpireAfterWrite() {
        return this.expireAfterWrite;
    }
    
    public void setExpireAfterWrite(final long millis) {
        if (millis == this.expireAfterWrite) {
            return;
        }
        if (this.expireAfterWrite != -1L) {
            if (millis == Long.MAX_VALUE) {
                throw new IllegalArgumentException("eternal disabled or expiry was set, refusing to reset back to eternal");
            }
            if (this.expireAfterWrite == Long.MAX_VALUE) {
                throw new IllegalArgumentException("eternal enabled explicitly, refusing to enable expiry");
            }
        }
        this.expireAfterWrite = millis;
    }
    
    public long getRetryInterval() {
        return this.retryInterval;
    }
    
    public void setRetryInterval(final long millis) {
        this.retryInterval = millis;
    }
    
    public long getMaxRetryInterval() {
        return this.maxRetryInterval;
    }
    
    public void setMaxRetryInterval(final long millis) {
        if (this.entryCapacity >= 0L) {}
        this.maxRetryInterval = millis;
    }
    
    public long getResilienceDuration() {
        return this.resilienceDuration;
    }
    
    public void setResilienceDuration(final long millis) {
        this.resilienceDuration = millis;
    }
    
    public boolean isKeepDataAfterExpired() {
        return this.keepDataAfterExpired;
    }
    
    public long getMaximumWeight() {
        return this.maximumWeight;
    }
    
    public void setMaximumWeight(final long v) {
        if (this.entryCapacity >= 0L) {
            throw new IllegalArgumentException("entryCapacity already set, setting maximumWeight is illegal");
        }
        this.maximumWeight = v;
    }
    
    public void setKeepDataAfterExpired(final boolean v) {
        this.keepDataAfterExpired = v;
    }
    
    public boolean isSharpExpiry() {
        return this.sharpExpiry;
    }
    
    public void setSharpExpiry(final boolean v) {
        this.sharpExpiry = v;
    }
    
    public boolean isSuppressExceptions() {
        return this.suppressExceptions;
    }
    
    public void setSuppressExceptions(final boolean v) {
        this.suppressExceptions = v;
    }
    
    public boolean isExternalConfigurationPresent() {
        return this.externalConfigurationPresent;
    }
    
    public void setExternalConfigurationPresent(final boolean v) {
        this.externalConfigurationPresent = v;
    }
    
    @Override
    public ConfigurationSectionContainer getSections() {
        if (this.sections == null) {
            this.sections = new ConfigurationSectionContainer();
        }
        return this.sections;
    }
    
    public void setSections(final Collection<ConfigurationSection> c) {
        this.getSections().addAll(c);
    }
    
    public CustomizationSupplier<? extends FunctionalCacheLoader<K, V>> getLoader() {
        return this.loader;
    }
    
    public void setLoader(final CustomizationSupplier<? extends FunctionalCacheLoader<K, V>> v) {
        this.loader = v;
    }
    
    public CustomizationSupplier<AdvancedCacheLoader<K, V>> getAdvancedLoader() {
        return this.advancedLoader;
    }
    
    public void setAdvancedLoader(final CustomizationSupplier<AdvancedCacheLoader<K, V>> v) {
        this.advancedLoader = v;
    }
    
    public CustomizationSupplier<AsyncCacheLoader<K, V>> getAsyncLoader() {
        return this.asyncLoader;
    }
    
    public void setAsyncLoader(final CustomizationSupplier<AsyncCacheLoader<K, V>> v) {
        this.asyncLoader = v;
    }
    
    public int getLoaderThreadCount() {
        return this.loaderThreadCount;
    }
    
    public void setLoaderThreadCount(final int v) {
        this.loaderThreadCount = v;
    }
    
    public CustomizationSupplier<ExpiryPolicy<K, V>> getExpiryPolicy() {
        return this.expiryPolicy;
    }
    
    public void setExpiryPolicy(final CustomizationSupplier<ExpiryPolicy<K, V>> v) {
        this.expiryPolicy = v;
    }
    
    public CustomizationSupplier<CacheWriter<K, V>> getWriter() {
        return this.writer;
    }
    
    public void setWriter(final CustomizationSupplier<CacheWriter<K, V>> v) {
        this.writer = v;
    }
    
    public boolean isStoreByReference() {
        return this.storeByReference;
    }
    
    public void setStoreByReference(final boolean v) {
        this.storeByReference = v;
    }
    
    public CustomizationSupplier<ExceptionPropagator<K>> getExceptionPropagator() {
        return this.exceptionPropagator;
    }
    
    public void setExceptionPropagator(final CustomizationSupplier<ExceptionPropagator<K>> v) {
        this.exceptionPropagator = v;
    }
    
    public CustomizationCollection<CacheEntryOperationListener<K, V>> getListeners() {
        if (this.listeners == null) {
            this.listeners = new DefaultCustomizationCollection<CacheEntryOperationListener<K, V>>();
        }
        return this.listeners;
    }
    
    public boolean hasListeners() {
        return this.listeners != null && !this.listeners.isEmpty();
    }
    
    public void setListeners(final Collection<CustomizationSupplier<CacheEntryOperationListener<K, V>>> c) {
        this.getListeners().addAll(c);
    }
    
    public CustomizationCollection<CacheEntryOperationListener<K, V>> getAsyncListeners() {
        if (this.asyncListeners == null) {
            this.asyncListeners = new DefaultCustomizationCollection<CacheEntryOperationListener<K, V>>();
        }
        return this.asyncListeners;
    }
    
    public boolean hasAsyncListeners() {
        return this.asyncListeners != null && !this.asyncListeners.isEmpty();
    }
    
    public void setAsyncListeners(final Collection<CustomizationSupplier<CacheEntryOperationListener<K, V>>> c) {
        this.getAsyncListeners().addAll(c);
    }
    
    public CustomizationCollection<CacheClosedListener> getCacheClosedListeners() {
        if (this.closedListeners == null) {
            this.closedListeners = new DefaultCustomizationCollection<CacheClosedListener>();
        }
        return this.closedListeners;
    }
    
    public boolean hasCacheClosedListeners() {
        return this.closedListeners != null && !this.closedListeners.isEmpty();
    }
    
    public void setCacheClosedListeners(final Collection<CustomizationSupplier<CacheClosedListener>> c) {
        this.getCacheClosedListeners().addAll(c);
    }
    
    public CustomizationSupplier<ResiliencePolicy<K, V>> getResiliencePolicy() {
        return this.resiliencePolicy;
    }
    
    public void setResiliencePolicy(final CustomizationSupplier<ResiliencePolicy<K, V>> v) {
        this.resiliencePolicy = v;
    }
    
    public boolean isStrictEviction() {
        return this.strictEviction;
    }
    
    public void setStrictEviction(final boolean v) {
        this.strictEviction = v;
    }
    
    public boolean isPermitNullValues() {
        return this.permitNullValues;
    }
    
    public void setPermitNullValues(final boolean v) {
        this.permitNullValues = v;
    }
    
    public boolean isDisableStatistics() {
        return this.disableStatistics;
    }
    
    public void setDisableStatistics(final boolean v) {
        this.disableStatistics = v;
    }
    
    public CustomizationSupplier<Executor> getLoaderExecutor() {
        return this.loaderExecutor;
    }
    
    @Deprecated
    public boolean isDisableLastModificationTime() {
        return false;
    }
    
    @Deprecated
    public void setDisableLastModificationTime(final boolean v) {
    }
    
    public boolean isRecordRefreshedTime() {
        return this.recordRefreshedTime;
    }
    
    public void setRecordRefreshedTime(final boolean v) {
        this.recordRefreshedTime = v;
    }
    
    public void setLoaderExecutor(final CustomizationSupplier<Executor> v) {
        this.loaderExecutor = v;
    }
    
    public CustomizationSupplier<Executor> getPrefetchExecutor() {
        return this.prefetchExecutor;
    }
    
    public void setPrefetchExecutor(final CustomizationSupplier<Executor> v) {
        this.prefetchExecutor = v;
    }
    
    public CustomizationSupplier<Executor> getAsyncListenerExecutor() {
        return this.asyncListenerExecutor;
    }
    
    public void setAsyncListenerExecutor(final CustomizationSupplier<Executor> v) {
        this.asyncListenerExecutor = v;
    }
    
    public CustomizationSupplier<TimeReference> getTimeReference() {
        return this.timeReference;
    }
    
    public void setTimeReference(final CustomizationSupplier<TimeReference> v) {
        this.timeReference = v;
    }
    
    public CustomizationSupplier<Weigher> getWeigher() {
        return this.weigher;
    }
    
    public void setWeigher(final CustomizationSupplier<Weigher> v) {
        if (this.entryCapacity >= 0L) {
            throw new IllegalArgumentException("entryCapacity already set, specifying a weigher is illegal");
        }
        this.weigher = v;
    }
    
    public boolean isBoostConcurrency() {
        return this.boostConcurrency;
    }
    
    public void setBoostConcurrency(final boolean v) {
        this.boostConcurrency = v;
    }
    
    public boolean isEnableJmx() {
        return this.enableJmx;
    }
    
    public void setEnableJmx(final boolean v) {
        this.enableJmx = v;
    }
}
