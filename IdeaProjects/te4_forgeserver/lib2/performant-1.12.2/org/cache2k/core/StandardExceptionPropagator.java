//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.integration.*;
import java.sql.*;

public final class StandardExceptionPropagator implements ExceptionPropagator
{
    @Override
    public RuntimeException propagateException(final Object key, final ExceptionInformation exceptionInformation) {
        final long _expiry = exceptionInformation.getUntil();
        String txt = "";
        if (_expiry > 0L) {
            if (_expiry == Long.MAX_VALUE) {
                txt = "expiry=ETERNAL, cause: ";
            }
            else {
                txt = "expiry=" + this.formatMillis(_expiry) + ", cause: ";
            }
        }
        return (RuntimeException)new CacheLoaderException(txt + exceptionInformation.getException(), exceptionInformation.getException());
    }
    
    private String formatMillis(final long t) {
        return new Timestamp(t).toString();
    }
}
