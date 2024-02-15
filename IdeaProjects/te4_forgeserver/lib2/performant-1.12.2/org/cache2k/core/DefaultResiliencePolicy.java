//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import java.util.*;
import org.cache2k.integration.*;
import org.cache2k.*;

public class DefaultResiliencePolicy<K, V> extends ResiliencePolicy<K, V>
{
    static final Random SHARED_RANDOM;
    static final int RETRY_PERCENT_OF_RESILIENCE_DURATION = 10;
    static final int MIN_RETRY_INTERVAL = 1000;
    private double multiplier;
    private double randomization;
    private long resilienceDuration;
    private long maxRetryInterval;
    private long retryInterval;
    
    public DefaultResiliencePolicy() {
        this.multiplier = 1.5;
        this.randomization = 0.5;
    }
    
    public DefaultResiliencePolicy(final double _multiplier, final double _randomization) {
        this.multiplier = 1.5;
        this.randomization = 0.5;
        this.multiplier = _multiplier;
        this.randomization = _randomization;
    }
    
    public double getMultiplier() {
        return this.multiplier;
    }
    
    public void setMultiplier(final double _multiplier) {
        this.multiplier = _multiplier;
    }
    
    public double getRandomization() {
        return this.randomization;
    }
    
    public void setRandomization(final double _randomization) {
        this.randomization = _randomization;
    }
    
    public long getResilienceDuration() {
        return this.resilienceDuration;
    }
    
    public long getMaxRetryInterval() {
        return this.maxRetryInterval;
    }
    
    public long getRetryInterval() {
        return this.retryInterval;
    }
    
    @Override
    public void init(final Context ctx) {
        this.resilienceDuration = ctx.getResilienceDurationMillis();
        this.maxRetryInterval = ctx.getMaxRetryIntervalMillis();
        this.retryInterval = ctx.getRetryIntervalMillis();
        if (this.resilienceDuration == -1L) {
            if (ctx.getExpireAfterWriteMillis() == Long.MAX_VALUE) {
                this.resilienceDuration = 0L;
            }
            else {
                this.resilienceDuration = ctx.getExpireAfterWriteMillis();
            }
        }
        else if (this.maxRetryInterval == -1L) {
            this.maxRetryInterval = this.resilienceDuration;
        }
        if (this.maxRetryInterval == -1L && this.retryInterval == -1L) {
            this.maxRetryInterval = this.resilienceDuration;
        }
        if (this.retryInterval == -1L) {
            this.retryInterval = this.resilienceDuration * 10L / 100L;
            this.retryInterval = Math.min(this.retryInterval, this.maxRetryInterval);
            this.retryInterval = Math.max(1000L, this.retryInterval);
        }
        if (this.retryInterval > this.maxRetryInterval) {
            this.maxRetryInterval = this.retryInterval;
        }
        if (this.maxRetryInterval > this.resilienceDuration && this.resilienceDuration != 0L) {
            this.resilienceDuration = this.maxRetryInterval;
        }
    }
    
    @Override
    public long suppressExceptionUntil(final K key, final ExceptionInformation exceptionInformation, final CacheEntry<K, V> cachedContent) {
        if (this.resilienceDuration == 0L) {
            return 0L;
        }
        if (this.resilienceDuration == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        final long _maxSuppressUntil = exceptionInformation.getSinceTime() + this.resilienceDuration;
        final long _deltaMs = this.calculateRetryDelta(exceptionInformation);
        return Math.min(exceptionInformation.getLoadTime() + _deltaMs, _maxSuppressUntil);
    }
    
    private long calculateRetryDelta(final ExceptionInformation exceptionInformation) {
        long _delta = (long)(this.retryInterval * Math.pow(this.multiplier, exceptionInformation.getRetryCount()));
        _delta -= (long)(DefaultResiliencePolicy.SHARED_RANDOM.nextDouble() * this.randomization * _delta);
        return Math.min(_delta, this.maxRetryInterval);
    }
    
    @Override
    public long retryLoadAfter(final K key, final ExceptionInformation exceptionInformation) {
        if (this.retryInterval == 0L) {
            return 0L;
        }
        if (this.retryInterval == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return exceptionInformation.getLoadTime() + this.calculateRetryDelta(exceptionInformation);
    }
    
    static {
        SHARED_RANDOM = new Random();
    }
}
