//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.concurrency;

public interface OptimisticLock
{
    long writeLock();
    
    long readLock();
    
    long tryOptimisticRead();
    
    boolean validate(final long p0);
    
    void unlockRead(final long p0);
    
    void unlockWrite(final long p0);
}
