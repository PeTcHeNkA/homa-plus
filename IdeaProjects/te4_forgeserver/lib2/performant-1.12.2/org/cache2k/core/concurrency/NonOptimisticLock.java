//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.concurrency;

import java.util.concurrent.locks.*;

public class NonOptimisticLock implements OptimisticLock
{
    private static final int DUMMY = 4711;
    private Sync sync;
    
    public NonOptimisticLock() {
        this.sync = new Sync();
    }
    
    @Override
    public long readLock() {
        this.sync.acquire(4711);
        return 4711L;
    }
    
    @Override
    public long writeLock() {
        this.sync.acquire(4711);
        return 4711L;
    }
    
    @Override
    public long tryOptimisticRead() {
        return (this.sync.getStamp() != 1) ? 1 : 0;
    }
    
    @Override
    public boolean validate(final long stamp) {
        return false;
    }
    
    @Override
    public void unlockRead(final long stamp) {
        this.sync.release(4711);
    }
    
    @Override
    public void unlockWrite(final long stamp) {
        this.sync.release(4711);
    }
    
    private static final class Sync extends AbstractQueuedSynchronizer
    {
        private static final int UNLOCKED = 0;
        private static final int LOCKED = 1;
        
        @Override
        protected boolean tryAcquire(final int acquires) {
            final int _state = this.getState();
            if (_state == 0 && this.compareAndSetState(_state, 1)) {
                this.setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }
        
        @Override
        protected boolean tryRelease(final int releases) {
            if (Thread.currentThread() != this.getExclusiveOwnerThread()) {
                throw new IllegalMonitorStateException();
            }
            this.setExclusiveOwnerThread(null);
            this.setState(0);
            return true;
        }
        
        public boolean isHeldExclusively() {
            return this.getState() == 1 && this.getExclusiveOwnerThread() == Thread.currentThread();
        }
        
        private int getStamp() {
            return this.getState();
        }
    }
}
