//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.util;

import java.util.concurrent.atomic.*;

public abstract class SimpleTimerTask implements Runnable
{
    private volatile int state;
    static final AtomicIntegerFieldUpdater<SimpleTimerTask> stateUpdater;
    static final int VIRGIN = 0;
    static final int SCHEDULED = 1;
    static final int EXECUTED = 2;
    static final int CANCELLED = 3;
    volatile long executionTime;
    
    protected SimpleTimerTask() {
        this.state = 0;
    }
    
    @Override
    public abstract void run();
    
    public boolean cancel() {
        return SimpleTimerTask.stateUpdater.compareAndSet(this, 1, 3);
    }
    
    public boolean execute() {
        return SimpleTimerTask.stateUpdater.compareAndSet(this, 1, 2);
    }
    
    public boolean schedule() {
        return SimpleTimerTask.stateUpdater.compareAndSet(this, 0, 1);
    }
    
    public boolean isCancelled() {
        return this.state == 3;
    }
    
    public boolean isScheduled() {
        return this.state == 1;
    }
    
    public long scheduledExecutionTime() {
        return this.executionTime;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{state=" + this.state + ", executionTime=" + this.executionTime + '}';
    }
    
    static {
        stateUpdater = AtomicIntegerFieldUpdater.newUpdater(SimpleTimerTask.class, "state");
    }
}
