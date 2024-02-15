//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.integration;

public abstract class LoadDetail<V>
{
    private Object value;
    
    public LoadDetail(final Object valueOrWrapper) {
        this.value = valueOrWrapper;
    }
    
    public V getValue() {
        if (this.value instanceof LoadDetail) {
            return (V)((LoadDetail)this.value).getValue();
        }
        return (V)this.value;
    }
    
    public LoadDetail<V> getNextInChain() {
        if (this.value instanceof LoadDetail) {
            return (LoadDetail)this.value;
        }
        return null;
    }
}
