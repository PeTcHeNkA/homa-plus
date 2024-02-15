//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.concurrency;

import java.util.concurrent.locks.*;

public class OptimisticLockStamped implements OptimisticLock
{
    private final StampedLock lock;
    
    public OptimisticLockStamped() {
        this.lock = new StampedLock();
    }
    
    public long readLock() {
        return this.lock.readLock();
    }
    
    public long writeLock() {
        return this.lock.writeLock();
    }
    
    public long tryOptimisticRead() {
        return this.lock.tryOptimisticRead();
    }
    
    public boolean validate(final long stamp) {
        return this.lock.validate(stamp);
    }
    
    public void unlockRead(final long stamp) {
        this.lock.unlockRead(stamp);
    }
    
    public void unlockWrite(final long stamp) {
        this.lock.unlockWrite(stamp);
    }
}
