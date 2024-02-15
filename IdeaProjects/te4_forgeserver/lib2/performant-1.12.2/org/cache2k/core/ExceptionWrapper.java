//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.integration.*;

public class ExceptionWrapper<K> implements ExceptionInformation
{
    private Throwable exception;
    private long loadTime;
    private long until;
    private int count;
    private long since;
    private K key;
    
    public ExceptionWrapper(final Throwable ex) {
        this(0L, ex);
    }
    
    public ExceptionWrapper(final long now, final Throwable ex) {
        this.loadTime = now;
        this.exception = ex;
    }
    
    public ExceptionWrapper(final K _key, final Throwable _exception, final long _loadTime, final Entry e) {
        final Object _oldValue = e.getValueOrException();
        ExceptionInformation _recentExceptionInfo;
        if (_oldValue instanceof ExceptionWrapper) {
            _recentExceptionInfo = (ExceptionInformation)_oldValue;
        }
        else {
            _recentExceptionInfo = e.getSuppressedLoadExceptionInformation();
        }
        this.init(_key, _exception, _loadTime, _recentExceptionInfo);
    }
    
    public ExceptionWrapper(final K _key, final Throwable _exception, final long _loadTime, final ExceptionInformation w) {
        this.init(_key, _exception, _loadTime, w);
    }
    
    private void init(final K _key, final Throwable _exception, final long _loadTime, final ExceptionInformation w) {
        this.exception = _exception;
        this.key = _key;
        this.loadTime = _loadTime;
        if (w != null) {
            this.since = w.getSinceTime();
            this.count = w.getRetryCount() + 1;
        }
        else {
            this.since = this.loadTime;
        }
    }
    
    public void setUntil(final long _until) {
        this.until = _until;
    }
    
    public K getKey() {
        return this.key;
    }
    
    @Override
    public Throwable getException() {
        return this.exception;
    }
    
    @Override
    public long getUntil() {
        return this.until;
    }
    
    @Override
    public long getLoadTime() {
        return this.loadTime;
    }
    
    @Override
    public int getRetryCount() {
        return this.count;
    }
    
    @Override
    public long getSinceTime() {
        return this.since;
    }
    
    @Override
    public String toString() {
        return "ExceptionWrapper{" + this.exception.toString() + "}";
    }
}
