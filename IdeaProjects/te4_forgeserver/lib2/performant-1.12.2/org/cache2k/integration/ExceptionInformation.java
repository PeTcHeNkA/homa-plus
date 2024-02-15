//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.integration;

public interface ExceptionInformation
{
    Throwable getException();
    
    int getRetryCount();
    
    long getSinceTime();
    
    long getLoadTime();
    
    long getUntil();
}
