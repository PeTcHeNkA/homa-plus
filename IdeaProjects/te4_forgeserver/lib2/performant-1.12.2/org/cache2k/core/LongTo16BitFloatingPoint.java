//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

public class LongTo16BitFloatingPoint
{
    private static final int FRACTION_BITS = 10;
    
    public static int fromLong(final long v) {
        if (v <= 0L) {
            if (v == 0L) {
                return 0;
            }
            throw new IllegalArgumentException("weight must be positive");
        }
        else {
            final int exp = 54 - Long.numberOfLeadingZeros(v);
            if (exp < 0) {
                final int r = (int)v;
                return (int)v;
            }
            return exp << 10 | (int)(v >> exp);
        }
    }
    
    public static long toLong(final int v) {
        final int exp = v >> 10;
        return (v & 0x3FF) << exp;
    }
}
