//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.util;

import org.cache2k.*;

public interface InternalClock extends TimeReference
{
    long millis();
    
    void sleep(final long p0) throws InterruptedException;
    
    boolean isJobSchedulable();
    
    TimeReachedJob createJob(final TimeReachedEvent p0);
    
    void schedule(final TimeReachedJob p0, final long p1);
    
    void disableJob(final TimeReachedJob p0);
    
    public interface TimeReachedJob
    {
    }
    
    public interface TimeReachedEvent
    {
        void timeIsReached(final long p0);
    }
}
