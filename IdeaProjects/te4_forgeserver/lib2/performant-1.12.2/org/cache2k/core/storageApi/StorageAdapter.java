//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.storageApi;

import java.util.concurrent.*;
import java.util.*;
import org.cache2k.core.*;

public abstract class StorageAdapter
{
    public abstract void open();
    
    public abstract Future<Void> cancelTimerJobs();
    
    public abstract Future<Void> shutdown();
    
    public abstract void flush();
    
    public abstract void purge();
    
    public abstract void clear();
    
    public abstract boolean checkStorageStillDisconnectedForClear();
    
    public abstract void disconnectStorageForClear();
    
    public abstract Future<Void> clearAndReconnect();
    
    public abstract void put(final Entry p0, final long p1);
    
    public abstract StorageEntry get(final Object p0);
    
    public abstract boolean remove(final Object p0);
    
    public abstract void evict(final Entry p0);
    
    public abstract void expire(final Entry p0);
    
    public abstract Iterator<Entry> iterateAll();
    
    public abstract long getTotalEntryCount();
    
    public abstract int getAlert();
    
    public abstract void disable(final Throwable p0);
    
    protected static Throwable buildThrowable(final String txt, final Throwable ex) {
        if (ex instanceof Error || ex.getCause() instanceof Error) {
            return (Throwable)new CacheInternalError(txt, ex);
        }
        return (Throwable)new CacheStorageException(txt, ex);
    }
    
    public static void rethrow(final String txt, final Throwable ex) {
        if (ex instanceof Error || ex.getCause() instanceof Error) {
            throw new CacheInternalError(txt, ex);
        }
        throw new CacheStorageException(txt, ex);
    }
    
    public interface Parent
    {
        void resetStorage(final StorageAdapter p0, final StorageAdapter p1);
    }
}
