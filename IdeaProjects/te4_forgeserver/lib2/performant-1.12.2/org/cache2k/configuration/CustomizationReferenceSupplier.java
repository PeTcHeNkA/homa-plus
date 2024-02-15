//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.configuration;

import org.cache2k.*;

public final class CustomizationReferenceSupplier<T> implements CustomizationSupplier<T>
{
    private T object;
    
    public CustomizationReferenceSupplier(final T obj) {
        if (obj == null) {
            throw new NullPointerException("object reference missing");
        }
        this.object = obj;
    }
    
    @Override
    public T supply(final CacheManager ignored) {
        return this.object;
    }
    
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CustomizationReferenceSupplier)) {
            return false;
        }
        final CustomizationReferenceSupplier<?> obj = (CustomizationReferenceSupplier<?>)other;
        return this.object.equals(obj.object);
    }
    
    @Override
    public int hashCode() {
        return this.object.hashCode();
    }
}
