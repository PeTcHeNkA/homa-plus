//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.util;

public final class ClockDefaultImpl implements InternalClock
{
    public static final ClockDefaultImpl INSTANCE;
    
    private ClockDefaultImpl() {
    }
    
    @Override
    public boolean isJobSchedulable() {
        return false;
    }
    
    @Override
    public TimeReachedJob createJob(final TimeReachedEvent ev) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void schedule(final TimeReachedJob j, final long _millis) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void disableJob(final TimeReachedJob j) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public long millis() {
        return System.currentTimeMillis();
    }
    
    @Override
    public void sleep(final long _millis) throws InterruptedException {
        Thread.sleep(_millis);
    }
    
    static {
        INSTANCE = new ClockDefaultImpl();
    }
}
