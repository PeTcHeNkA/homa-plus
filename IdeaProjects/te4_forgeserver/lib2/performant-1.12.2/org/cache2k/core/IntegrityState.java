//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import java.util.*;

public class IntegrityState
{
    List<String> failingTests;
    long state;
    long bitNr;
    int stringsHashCode;
    String groupPrefix;
    
    public IntegrityState() {
        this.failingTests = new ArrayList<String>();
        this.state = 0L;
        this.bitNr = 0L;
        this.stringsHashCode = 0;
        this.groupPrefix = "";
    }
    
    IntegrityState check(final boolean f) {
        this.check(null, f);
        return this;
    }
    
    protected IntegrityState check(String _check, final String _note, final boolean f) {
        if (_check == null || _check.length() == 0) {
            _check = "test#" + this.bitNr;
        }
        this.stringsHashCode = this.stringsHashCode * 31 + _check.hashCode();
        if (!f) {
            if (_note != null) {
                this.failingTests.add(this.groupPrefix + '\"' + _check + "\" => " + _note);
            }
            else {
                this.failingTests.add(this.groupPrefix + '\"' + _check + '\"');
            }
            this.state |= 1 << (int)this.bitNr;
        }
        ++this.bitNr;
        return this;
    }
    
    public IntegrityState group(final String _group) {
        this.groupPrefix = _group + ": ";
        return this;
    }
    
    public IntegrityState check(final String _check, final boolean f) {
        this.check(_check, null, f);
        return this;
    }
    
    public IntegrityState checkEquals(final String _check, final int v1, final int v2) {
        if (v1 == v2) {
            this.check(_check, null, true);
        }
        else {
            this.check(_check, v1 + "==" + v2, false);
        }
        return this;
    }
    
    public IntegrityState checkEquals(final String _check, final long v1, final long v2) {
        if (v1 == v2) {
            this.check(_check, null, true);
        }
        else {
            this.check(_check, v1 + "==" + v2, false);
        }
        return this;
    }
    
    public IntegrityState checkLessOrEquals(final String _check, final int v1, final int v2) {
        if (v1 <= v2) {
            this.check(_check, null, true);
        }
        else {
            this.check(_check, v1 + "<=" + v2, false);
        }
        return this;
    }
    
    public IntegrityState checkLess(final String _check, final int v1, final int v2) {
        if (v1 < v2) {
            this.check(_check, null, true);
        }
        else {
            this.check(_check, v1 + "<" + v2, false);
        }
        return this;
    }
    
    public IntegrityState checkGreaterOrEquals(final String _check, final int v1, final int v2) {
        if (v1 >= v2) {
            this.check(_check, null, true);
        }
        else {
            this.check(_check, v1 + ">=" + v2, false);
        }
        return this;
    }
    
    public IntegrityState checkGreater(final String _check, final int v1, final int v2) {
        if (v1 > v2) {
            this.check(_check, null, true);
        }
        else {
            this.check(_check, v1 + ">" + v2, false);
        }
        return this;
    }
    
    public String getStateDescriptor() {
        return Long.toHexString(this.state) + '.' + this.bitNr + '.' + Integer.toHexString(this.stringsHashCode);
    }
    
    public long getStateFlags() {
        return this.state;
    }
    
    public String getFailingChecks() {
        return this.failingTests.toString();
    }
    
    public void throwIfNeeded() {
        if (this.state > 0L) {
            throw new IllegalStateException("Integrity test failed: " + this.failingTests.toString());
        }
    }
}
