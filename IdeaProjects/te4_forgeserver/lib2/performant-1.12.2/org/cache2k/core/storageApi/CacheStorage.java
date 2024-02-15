//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.storageApi;

import java.util.concurrent.*;

public interface CacheStorage
{
    StorageEntry get(final Object p0) throws Exception;
    
    void put(final StorageEntry p0) throws Exception;
    
    boolean remove(final Object p0) throws Exception;
    
    boolean contains(final Object p0) throws Exception;
    
    void clear() throws Exception;
    
    void close() throws Exception;
    
    void visit(final VisitContext p0, final EntryFilter p1, final EntryVisitor p2) throws Exception;
    
    int getEntryCount();
    
    public interface EntryVisitor
    {
        void visit(final StorageEntry p0) throws Exception;
    }
    
    public interface VisitContext extends MultiThreadedContext
    {
        boolean needMetaData();
        
        boolean needValue();
    }
    
    public interface MultiThreadedContext
    {
        ExecutorService getExecutorService();
        
        void awaitTermination() throws InterruptedException;
        
        boolean shouldStop();
        
        void abortOnException(final Throwable p0);
    }
    
    public interface EntryFilter
    {
        boolean shouldInclude(final Object p0);
    }
}
