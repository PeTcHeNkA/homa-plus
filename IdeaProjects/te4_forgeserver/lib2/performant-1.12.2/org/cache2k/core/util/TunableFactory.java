//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.util;

import java.util.*;
import org.cache2k.core.*;
import java.io.*;
import java.lang.reflect.*;

public final class TunableFactory
{
    static Log log;
    public static final String DEFAULT_TUNING_FILE_NAME = "/org/cache2k/default-tuning.properties";
    public static final String CUSTOM_TUNING_FILE_NAME = "/org/cache2k/tuning.properties";
    public static final String TUNE_MARKER = "org.cache2k.tuning";
    private static Map<Class<?>, Object> map;
    private static Properties defaultProperties;
    private static Properties customProperties;
    
    public static synchronized void reload() {
        TunableFactory.map = new HashMap<Class<?>, Object>();
        TunableFactory.customProperties = loadFile("/org/cache2k/tuning.properties");
        TunableFactory.defaultProperties = loadFile("/org/cache2k/default-tuning.properties");
    }
    
    static Properties loadFile(final String _fileName) {
        final InputStream in = TunableConstants.class.getResourceAsStream(_fileName);
        if (in != null) {
            try {
                final Properties p = new Properties();
                p.load(in);
                in.close();
                return p;
            }
            catch (IOException ex) {
                throw new CacheInternalError("tuning properties not readable", (Throwable)ex);
            }
        }
        return null;
    }
    
    public static synchronized <T extends TunableConstants> T get(final Properties p, final Class<T> c) {
        T cfg = getDefault(c);
        if (p != null && p.containsKey("org.cache2k.tuning") && p.containsKey(cfg.getClass().getName() + ".tuning")) {
            cfg = (T)cfg.clone();
            apply(p, cfg);
        }
        return cfg;
    }
    
    public static synchronized <T extends TunableConstants> T get(final Class<T> c) {
        return (T)getDefault((Class<TunableConstants>)c);
    }
    
    private static <T extends TunableConstants> T getDefault(final Class<T> c) {
        if (TunableFactory.map == null) {
            reload();
        }
        T cfg = (T)TunableFactory.map.get(c);
        if (cfg == null) {
            try {
                cfg = c.newInstance();
            }
            catch (Exception ex) {
                throw new CacheInternalError("cannot instantiate tunable constants", (Throwable)ex);
            }
            apply(TunableFactory.defaultProperties, cfg);
            apply(TunableFactory.customProperties, cfg);
            apply(System.getProperties(), cfg);
            TunableFactory.map.put(c, cfg);
        }
        return cfg;
    }
    
    static void apply(final Properties p, final Object cfg) {
        if (p == null) {
            return;
        }
        String _propName = null;
        try {
            for (final Field f : cfg.getClass().getFields()) {
                _propName = cfg.getClass().getName().replace('$', '.') + "." + f.getName();
                String o = p.getProperty(_propName);
                if (o != null) {
                    final Class<?> _fieldType = f.getType();
                    if (_fieldType == Boolean.TYPE) {
                        o = o.toLowerCase();
                        if ("off".equals(o) || "false".equals(o) || "disable".equals(o)) {
                            f.set(cfg, false);
                        }
                        else {
                            f.set(cfg, true);
                        }
                        if (TunableFactory.log.isDebugEnabled()) {
                            TunableFactory.log.debug(_propName + "=" + f.get(cfg));
                        }
                    }
                    else if (_fieldType == Integer.TYPE) {
                        f.set(cfg, Integer.valueOf(o));
                        if (TunableFactory.log.isDebugEnabled()) {
                            TunableFactory.log.debug(_propName + "=" + f.get(cfg));
                        }
                    }
                    else if (_fieldType == Long.TYPE) {
                        f.set(cfg, Long.valueOf(o));
                        if (TunableFactory.log.isDebugEnabled()) {
                            TunableFactory.log.debug(_propName + "=" + f.get(cfg));
                        }
                    }
                    else if (_fieldType == String.class) {
                        f.set(cfg, o);
                        if (TunableFactory.log.isDebugEnabled()) {
                            TunableFactory.log.debug(_propName + "=" + f.get(cfg));
                        }
                    }
                    else {
                        if (_fieldType != Class.class) {
                            throw new CacheInternalError("Changing this tunable type is not supported. Tunable: " + _propName + ", " + _fieldType);
                        }
                        final Class<?> c = Class.forName(o);
                        f.set(cfg, c);
                        if (TunableFactory.log.isDebugEnabled()) {
                            TunableFactory.log.debug(_propName + "=" + c.getName());
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            if (_propName != null) {
                throw new CacheInternalError("error applying tuning setup, property=" + _propName, (Throwable)ex);
            }
            throw new CacheInternalError("error applying tuning setup", (Throwable)ex);
        }
    }
    
    static {
        TunableFactory.log = Log.getLog((Class)TunableFactory.class);
    }
}
