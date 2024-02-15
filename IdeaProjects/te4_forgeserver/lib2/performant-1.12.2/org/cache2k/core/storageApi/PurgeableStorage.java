//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.storageApi;

public interface PurgeableStorage
{
    PurgeResult purge(final PurgeContext p0, final long p1, final long p2) throws Exception;
    
    public interface PurgeAction
    {
        StorageEntry checkAndPurge(final Object p0);
    }
    
    public interface PurgeResult
    {
        long getBytesFreed();
        
        int getEntriesPurged();
        
        int getEntriesScanned();
    }
    
    public interface PurgeContext extends CacheStorage.MultiThreadedContext
    {
        void lockAndRun(final Object p0, final PurgeAction p1);
    }
}
