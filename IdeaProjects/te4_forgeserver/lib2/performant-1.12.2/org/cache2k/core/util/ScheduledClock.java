//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.util;

public interface ScheduledClock
{
    boolean isJobSchedulable();
    
    Job createJob(final Runnable p0);
    
    void schedule(final Job p0, final long p1);
    
    public interface Job
    {
    }
}
