//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.operation;

import org.cache2k.processor.*;
import org.cache2k.core.*;
import org.cache2k.integration.*;

class MutableEntryOnProgress<K, V> implements MutableCacheEntry<K, V>
{
    private ExaminationEntry<K, V> entry;
    private Progress<K, V, ?> progress;
    private boolean originalExists;
    private boolean mutate;
    private boolean remove;
    private boolean exists;
    private V value;
    private boolean readThrough;
    private boolean customExpiry;
    private long expiryTime;
    private long refreshTime;
    
    public MutableEntryOnProgress(final Progress<K, V, ?> _progress, final ExaminationEntry<K, V> _entry, final boolean _readThrough) {
        this.originalExists = false;
        this.mutate = false;
        this.remove = false;
        this.exists = false;
        this.value = null;
        this.customExpiry = false;
        this.expiryTime = -1L;
        this.refreshTime = this.expiryTime;
        this.readThrough = _readThrough;
        this.entry = _entry;
        this.progress = _progress;
        if (_progress.isPresentOrMiss()) {
            this.value = (V)this.entry.getValueOrException();
            final boolean b = true;
            this.exists = b;
            this.originalExists = b;
        }
    }
    
    @Override
    public long getCurrentTime() {
        return this.progress.getCurrentTime();
    }
    
    @Override
    public boolean exists() {
        return this.exists;
    }
    
    @Override
    public MutableCacheEntry<K, V> setValue(final V v) {
        this.mutate = true;
        this.exists = true;
        this.remove = false;
        this.value = v;
        return this;
    }
    
    @Override
    public MutableCacheEntry<K, V> setException(final Throwable ex) {
        this.mutate = true;
        this.exists = true;
        this.remove = false;
        this.value = (V)new ExceptionWrapper(this.progress.getCurrentTime(), ex);
        return this;
    }
    
    @Override
    public MutableCacheEntry<K, V> setExpiryTime(final long t) {
        this.customExpiry = true;
        this.expiryTime = t;
        return this;
    }
    
    @Override
    public MutableCacheEntry<K, V> remove() {
        if (this.mutate && !this.originalExists) {
            this.mutate = false;
        }
        else {
            final boolean b = true;
            this.remove = b;
            this.mutate = b;
        }
        this.exists = false;
        this.value = null;
        return this;
    }
    
    public K getKey() {
        return (K)this.entry.getKey();
    }
    
    @Override
    public V getValue() {
        if (!this.exists && !this.mutate && this.readThrough) {
            throw new Operations.NeedsLoadRestartException();
        }
        if (this.value instanceof ExceptionWrapper) {
            throw this.progress.propagateException((K)this.entry.getKey(), (ExceptionInformation)this.value);
        }
        return this.value;
    }
    
    @Override
    public V getOldValue() {
        if (!this.originalExists || this.entry instanceof LoadedEntry) {
            return null;
        }
        final V _value = (V)this.entry.getValueOrException();
        if (_value instanceof ExceptionWrapper) {
            throw this.progress.propagateException((K)this.entry.getKey(), (ExceptionInformation)_value);
        }
        return _value;
    }
    
    @Override
    public boolean wasExisting() {
        return this.originalExists && !(this.entry instanceof LoadedEntry);
    }
    
    public Throwable getException() {
        if (!this.exists && !this.mutate && this.readThrough) {
            throw new Operations.NeedsLoadRestartException();
        }
        if (this.value instanceof ExceptionWrapper) {
            return ((ExceptionWrapper)this.value).getException();
        }
        return null;
    }
    
    @Override
    public long getRefreshedTime() {
        if (this.refreshTime != -1L) {
            return this.refreshTime;
        }
        return this.originalExists ? this.entry.getRefreshTime() : 0L;
    }
    
    @Override
    public MutableCacheEntry<K, V> setRefreshedTime(final long t) {
        this.refreshTime = t;
        return this;
    }
    
    public long getLastModification() {
        throw new UnsupportedOperationException();
    }
    
    public void sendMutationCommandIfNeeded() {
        if (!this.mutate) {
            if (this.customExpiry) {
                this.progress.expire(this.expiryTime);
            }
            return;
        }
        if (this.remove) {
            this.progress.remove();
            return;
        }
        if (this.customExpiry || this.refreshTime != -1L) {
            this.progress.putAndSetExpiry(this.value, this.expiryTime, this.refreshTime);
            return;
        }
        this.progress.put(this.value);
    }
}
