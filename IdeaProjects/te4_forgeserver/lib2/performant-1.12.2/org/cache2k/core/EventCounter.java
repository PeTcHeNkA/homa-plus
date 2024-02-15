//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

public interface EventCounter
{
    void loadStart();
    
    void load();
    
    void load(final int p0);
    
    long getLoadCount();
    
    void write();
    
    void write(final int p0);
    
    long getWriteCount();
}
