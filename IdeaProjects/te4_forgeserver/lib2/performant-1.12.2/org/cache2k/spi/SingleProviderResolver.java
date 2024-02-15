//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.spi;

import java.io.*;
import java.util.*;

public class SingleProviderResolver
{
    private static Map<Class, Object> providers;
    
    public static <T> T resolveMandatory(final Class<T> c) {
        final T obj = (T)resolve((Class<Object>)c);
        if (obj == null) {
            final Error err = new LinkageError("No implementation found for: " + c.getName());
            throw err;
        }
        return obj;
    }
    
    public static <T> T resolve(final Class<T> c) {
        return resolve(c, null);
    }
    
    public static synchronized <T> T resolve(final Class<T> c, final Class<? extends T> defaultImpl) {
        if (SingleProviderResolver.providers.containsKey(c)) {
            return (T)SingleProviderResolver.providers.get(c);
        }
        try {
            final String _className = readFile("org/cache2k/services/" + c.getName());
            T obj = null;
            if (_className == null) {
                final ServiceLoader<T> sl = ServiceLoader.load(c);
                final Iterator<T> it = sl.iterator();
                if (it.hasNext()) {
                    obj = it.next();
                }
            }
            else {
                obj = (T)SingleProviderResolver.class.getClassLoader().loadClass(_className).newInstance();
            }
            if (obj == null && defaultImpl != null) {
                obj = (T)defaultImpl.newInstance();
            }
            SingleProviderResolver.providers.put(c, obj);
            return obj;
        }
        catch (Exception ex) {
            final Error err = new LinkageError("Error instantiating " + c.getName(), ex);
            err.printStackTrace();
            throw err;
        }
    }
    
    private static String readFile(final String _name) throws IOException {
        final InputStream in = SingleProviderResolver.class.getClassLoader().getResourceAsStream(_name);
        if (in == null) {
            return null;
        }
        try {
            final LineNumberReader r = new LineNumberReader(new InputStreamReader(in));
            for (String l = r.readLine(); l != null; l = r.readLine()) {
                if (!l.startsWith("#")) {
                    return l;
                }
            }
        }
        finally {
            in.close();
        }
        return null;
    }
    
    static {
        SingleProviderResolver.providers = new HashMap<Class, Object>();
    }
}
