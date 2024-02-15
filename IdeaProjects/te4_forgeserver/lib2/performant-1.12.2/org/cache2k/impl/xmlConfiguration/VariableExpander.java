//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.xmlConfiguration;

public interface VariableExpander
{
    void expand(final ParsedConfiguration p0);
    
    public static class NeedsExpansion extends RuntimeException
    {
    }
    
    public interface ValueAccessor
    {
        String get(final ExpanderContext p0, final String p1);
    }
    
    public interface ExpanderContext
    {
        ParsedConfiguration getTopLevelConfiguration();
        
        ParsedConfiguration getCurrentConfiguration();
    }
}
