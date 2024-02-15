//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.util;

import java.io.*;
import java.util.*;

public class Cache2kVersion
{
    private static String version;
    
    static void parse(final InputStream in) {
        if (in != null) {
            try {
                final Properties p = new Properties();
                p.load(in);
                in.close();
                final String s = p.getProperty("version");
                if (isDefined(s)) {
                    Cache2kVersion.version = s;
                }
            }
            catch (Exception e) {
                Log.getLog(Cache2kVersion.class).warn("error parsing version properties", e);
            }
        }
    }
    
    static boolean isDefined(final String s) {
        return s != null && s.length() > 0 && !s.startsWith("$");
    }
    
    public static String getVersion() {
        return Cache2kVersion.version;
    }
    
    static {
        Cache2kVersion.version = "<unknown>";
        final InputStream in = Cache2kVersion.class.getResourceAsStream("/org.cache2k.impl.version.txt");
        parse(in);
    }
}
