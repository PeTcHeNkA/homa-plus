//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.configuration;

public interface CacheType<T>
{
    public static final String DESCRIPTOR_TO_STRING_PREFIX = "CacheTypeDescriptor#";
    
    Class<T> getType();
    
    boolean hasTypeArguments();
    
    boolean isArray();
    
    CacheType<?> getComponentType();
    
    CacheType<?>[] getTypeArguments();
    
    String getTypeName();
    
    CacheType<T> getBeanRepresentation();
}
