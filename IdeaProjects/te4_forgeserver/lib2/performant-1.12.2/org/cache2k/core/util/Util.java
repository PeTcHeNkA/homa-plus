//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.util;

import java.sql.*;
import org.cache2k.*;

public class Util
{
    public static final String NAME_SEPARATOR = ":";
    
    public static String formatMillis(final long _millis) {
        return new Timestamp(_millis).toString();
    }
    
    public static String compactFullName(final CacheManager mgr, final String _cacheName) {
        if (!"default".equals(mgr.getName())) {
            return mgr.getName() + ":" + _cacheName;
        }
        return _cacheName;
    }
}
