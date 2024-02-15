//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.expiry;

public class Expiry implements ExpiryTimeValues
{
    public static long toSharpTime(final long millis) {
        if (millis == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        if (millis < 0L) {
            return millis;
        }
        return -millis;
    }
    
    public static long earliestTime(final long loadTime, final long candidate1, final long candidate2) {
        if (candidate1 >= loadTime && (candidate1 < candidate2 || candidate2 < loadTime)) {
            return candidate1;
        }
        if (candidate2 >= loadTime) {
            return candidate2;
        }
        return Long.MAX_VALUE;
    }
    
    public static long mixTimeSpanAndPointInTime(final long loadTime, final long refreshAfter, final long pointInTime) {
        long _refreshTime = loadTime + refreshAfter;
        if (_refreshTime < 0L) {
            _refreshTime = Long.MAX_VALUE;
        }
        if (pointInTime == Long.MAX_VALUE) {
            return _refreshTime;
        }
        if (pointInTime > _refreshTime) {
            return _refreshTime;
        }
        final long _absPointInTime = Math.abs(pointInTime);
        if (_absPointInTime <= _refreshTime) {
            return pointInTime;
        }
        final long _pointInTimeMinusDelta = _absPointInTime - refreshAfter;
        if (_pointInTimeMinusDelta < _refreshTime) {
            return _pointInTimeMinusDelta;
        }
        return _refreshTime;
    }
}
