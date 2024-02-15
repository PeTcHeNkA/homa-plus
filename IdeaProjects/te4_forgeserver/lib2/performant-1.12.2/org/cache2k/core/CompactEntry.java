//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

class CompactEntry<K, V>
{
    protected static final InitialValueInEntryNeverReturned INITIAL_VALUE;
    protected volatile long nextRefreshTime;
    private final K key;
    private volatile V valueOrException;
    public final int hashCode;
    public Entry<K, V> another;
    public long hitCnt;
    
    public CompactEntry(final K _key, final int _hashCode) {
        this.valueOrException = (V)CompactEntry.INITIAL_VALUE;
        this.key = _key;
        this.hashCode = _hashCode;
    }
    
    public void setValueOrException(final V _valueOrException) {
        this.valueOrException = _valueOrException;
    }
    
    public Throwable getException() {
        final V v = this.valueOrException;
        if (v instanceof ExceptionWrapper) {
            return ((ExceptionWrapper)v).getException();
        }
        return null;
    }
    
    public boolean equalsValue(final V v) {
        final V ve = this.valueOrException;
        if (ve == null) {
            return v == ve;
        }
        return ve.equals(v);
    }
    
    @Deprecated
    public V getValue() {
        return this.valueOrException;
    }
    
    public V getValueOrException() {
        return this.valueOrException;
    }
    
    public K getKey() {
        if (this.key == null) {
            return (K)Integer.valueOf(this.hashCode);
        }
        return this.key;
    }
    
    public K getKeyObj() {
        return this.key;
    }
    
    static {
        INITIAL_VALUE = new InitialValueInEntryNeverReturned();
    }
    
    private static class InitialValueInEntryNeverReturned
    {
    }
}
