//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.configuration;

import org.cache2k.*;

public final class CustomizationSupplierByClassName<T> implements CustomizationSupplier<T>, ValidatingConfigurationBean
{
    private String className;
    
    public CustomizationSupplierByClassName() {
    }
    
    public CustomizationSupplierByClassName(final String className) {
        if (className == null) {
            throw new NullPointerException("className");
        }
        this.className = className;
    }
    
    public String getClassName() {
        return this.className;
    }
    
    public void setClassName(final String v) {
        this.className = v;
    }
    
    public void validate() {
        if (this.className == null) {
            throw new IllegalArgumentException("className not set");
        }
    }
    
    public T supply(final CacheManager manager) throws Exception {
        return (T)manager.getClassLoader().loadClass(this.className).newInstance();
    }
    
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CustomizationSupplierByClassName)) {
            return false;
        }
        final CustomizationSupplierByClassName<?> _that = (CustomizationSupplierByClassName<?>)other;
        return this.className.equals(_that.className);
    }
    
    @Override
    public int hashCode() {
        return this.className.hashCode();
    }
}
