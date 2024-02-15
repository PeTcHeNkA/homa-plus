//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.configuration;

import java.util.*;

public final class DefaultCustomizationCollection<T> extends AbstractCollection<CustomizationSupplier<T>> implements CustomizationCollection<T>
{
    private Collection<CustomizationSupplier<T>> list;
    
    public DefaultCustomizationCollection() {
        this.list = new ArrayList<CustomizationSupplier<T>>();
    }
    
    @Override
    public int size() {
        return this.list.size();
    }
    
    @Override
    public Iterator<CustomizationSupplier<T>> iterator() {
        return this.list.iterator();
    }
    
    @Override
    public boolean add(final CustomizationSupplier<T> entry) {
        if (this.list.contains(entry)) {
            throw new IllegalArgumentException("duplicate entry");
        }
        return this.list.add(entry);
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + this.list.toString();
    }
}
