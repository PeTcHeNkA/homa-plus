//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.xmlConfiguration;

import java.util.*;
import org.cache2k.configuration.*;

public class StandardPropertyParser implements PropertyParser
{
    private Map<Class<?>, ValueConverter> type2parser;
    private static final Map<String, Long> UNIT2LONG;
    
    public StandardPropertyParser() {
        this.type2parser = new HashMap<Class<?>, ValueConverter>();
        this.addParser(Integer.TYPE, Integer.class, new ValueConverter<Integer>() {
            @Override
            public Integer parse(final String v) {
                return Integer.valueOf(v);
            }
        });
        this.addParser(Boolean.TYPE, Boolean.class, new ValueConverter<Boolean>() {
            @Override
            public Boolean parse(String v) {
                v = v.toLowerCase();
                if ("true".equals(v)) {
                    return true;
                }
                if ("false".equals(v)) {
                    return false;
                }
                throw new IllegalArgumentException("no boolean, true/false expected");
            }
        });
        this.addParser(Long.TYPE, Long.class, new ValueConverter<Long>() {
            @Override
            public Long parse(final String v) {
                return StandardPropertyParser.parseLongWithUnitSuffix(v);
            }
        });
        this.addParser(String.class, new ValueConverter<String>() {
            @Override
            public String parse(final String v) {
                return v;
            }
        });
        this.addParser(CacheType.class, new ValueConverter<CacheType>() {
            @Override
            public CacheType parse(String v) throws Exception {
                if (!v.contains(".")) {
                    v = "java.lang." + v;
                }
                return CacheTypeCapture.of((Class)Class.forName(v));
            }
        });
    }
    
    public Object parse(final Class<?> _targetType, final String _value) throws Exception {
        final ValueConverter p = this.type2parser.get(_targetType);
        if (p == null) {
            throw new IllegalArgumentException("Unknown target type: " + _targetType);
        }
        return p.parse(_value);
    }
    
    private void addParser(final Class<?> _type, final ValueConverter<?> p) {
        this.type2parser.put(_type, p);
    }
    
    private void addParser(final Class<?> _primitiveType, final Class<?> _type, final ValueConverter<?> p) {
        this.type2parser.put(_primitiveType, p);
        this.type2parser.put(_type, p);
    }
    
    static long parseLongWithUnitSuffix(String v) {
        v = v.replace("_", "");
        long _multiplier = 1L;
        int pos = v.length();
        while (--pos >= 0 && !Character.isDigit(v.charAt(pos))) {}
        if (pos < v.length() - 1) {
            final String _unitSuffix = v.substring(pos + 1);
            final Long _newMultiplier = StandardPropertyParser.UNIT2LONG.get(_unitSuffix);
            if (_newMultiplier == null) {
                throw new NumberFormatException("Unknown unit suffix in: \"" + v + "\"");
            }
            v = v.substring(0, pos + 1);
            _multiplier = _newMultiplier;
        }
        return Long.valueOf(v) * _multiplier;
    }
    
    static {
        UNIT2LONG = new HashMap<String, Long>() {
            {
                this.put("KiB", 1024L);
                this.put("MiB", 1048576L);
                this.put("GiB", 1073741824L);
                this.put("TiB", 1099511627776L);
                this.put("k", 1000L);
                this.put("M", 1000000L);
                this.put("G", 1000000000L);
                this.put("T", 1000000000000L);
                this.put("s", 1000L);
                this.put("m", 60000L);
                this.put("h", 3600000L);
                this.put("d", 86400000L);
            }
        };
    }
}
