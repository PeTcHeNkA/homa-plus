//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.xmlConfiguration;

public interface ConfigurationTokenizer extends SourceLocation
{
    String getSource();
    
    int getLineNumber();
    
    Item next() throws Exception;
    
    public interface End extends Item
    {
    }
    
    public interface Item extends SourceLocation
    {
        String getSource();
        
        int getLineNumber();
    }
    
    public interface Unnest extends Item
    {
    }
    
    public interface Nest extends Item
    {
        String getSectionName();
    }
    
    public interface Property extends Item
    {
        String getName();
        
        String getValue();
        
        void setValue(final String p0);
        
        boolean isExpanded();
        
        void setExpanded(final boolean p0);
    }
}
