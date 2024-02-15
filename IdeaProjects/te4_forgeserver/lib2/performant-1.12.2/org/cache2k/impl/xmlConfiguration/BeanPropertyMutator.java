//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.xmlConfiguration;

import java.lang.reflect.*;
import java.util.*;

public class BeanPropertyMutator
{
    private static final String SETTER_PREFIX = "set";
    private final Map<String, Method> settersLookupMap;
    
    public BeanPropertyMutator(final Class<?> _class) {
        this.settersLookupMap = generateSetterLookupMap(_class);
    }
    
    public Class<?> getType(final String _propertyName) {
        final Method m = this.settersLookupMap.get(_propertyName);
        if (m == null) {
            return null;
        }
        return m.getParameterTypes()[0];
    }
    
    public void mutate(final Object _target, final String _propertyName, final Object _value) throws IllegalAccessException, InvocationTargetException {
        final Method m = this.settersLookupMap.get(_propertyName);
        m.invoke(_target, _value);
    }
    
    private static boolean preferCacheTypeAndNotClass(final Class<?> c) {
        return !Class.class.equals(c);
    }
    
    static Map<String, Method> generateSetterLookupMap(final Class<?> c) {
        final Map<String, Method> map = new HashMap<String, Method>();
        for (final Method m : c.getMethods()) {
            if (m.getName().startsWith("set") && m.getReturnType() == Void.TYPE && m.getParameterTypes().length == 1 && preferCacheTypeAndNotClass(m.getParameterTypes()[0])) {
                final String _propertyName = generatePropertyNameFromSetter(m.getName());
                final Method m2 = map.put(_propertyName, m);
                if (m2 != null) {
                    throw new IllegalArgumentException("Ambiguous setter for property '" + _propertyName + "' in class '" + c.getSimpleName() + "'");
                }
            }
        }
        return map;
    }
    
    static String generatePropertyNameFromSetter(final String s) {
        return changeFirstCharToLowerCase(s.substring("set".length()));
    }
    
    static String changeFirstCharToLowerCase(final String v) {
        return Character.toLowerCase(v.charAt(0)) + v.substring(1);
    }
}
