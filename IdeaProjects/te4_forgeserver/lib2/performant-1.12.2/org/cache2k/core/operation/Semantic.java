//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.operation;

public interface Semantic<K, V, R>
{
    void start(final Progress<K, V, R> p0);
    
    void examine(final Progress<K, V, R> p0, final ExaminationEntry<K, V> p1);
    
    void update(final Progress<K, V, R> p0, final ExaminationEntry<K, V> p1);
    
    void loaded(final Progress<K, V, R> p0, final ExaminationEntry<K, V> p1);
    
    public abstract static class Base<K, V, R> implements Semantic<K, V, R>
    {
        @Override
        public void loaded(final Progress<K, V, R> c, final ExaminationEntry<K, V> e) {
            c.result((R) e.getValueOrException());
        }
    }
    
    public abstract static class Update<K, V, R> extends Base<K, V, R>
    {
        @Override
        public final void start(final Progress<K, V, R> c) {
            c.wantMutation();
        }
        
        @Override
        public final void examine(final Progress<K, V, R> c, final ExaminationEntry<K, V> e) {
        }
        
        @Override
        public abstract void update(final Progress<K, V, R> p0, final ExaminationEntry<K, V> p1);
    }
    
    public abstract static class UpdateExisting<K, V, R> extends Base<K, V, R>
    {
        @Override
        public final void start(final Progress<K, V, R> c) {
            c.wantData();
        }
        
        @Override
        public final void examine(final Progress<K, V, R> c, final ExaminationEntry<K, V> e) {
            c.wantMutation();
        }
        
        @Override
        public abstract void update(final Progress<K, V, R> p0, final ExaminationEntry<K, V> p1);
    }
    
    public abstract static class MightUpdateExisting<K, V, R> extends Base<K, V, R>
    {
        @Override
        public final void start(final Progress<K, V, R> c) {
            c.wantData();
        }
    }
    
    public abstract static class Read<K, V, R> extends Base<K, V, R>
    {
        @Override
        public final void start(final Progress<K, V, R> c) {
            c.wantData();
        }
        
        @Override
        public final void update(final Progress<K, V, R> c, final ExaminationEntry<K, V> e) {
        }
    }
}
