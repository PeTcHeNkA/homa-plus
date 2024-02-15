//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.configuration;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

public class CacheTypeCapture<T> implements CacheType<T>
{
    private final CacheType<T> descriptor;
    
    protected CacheTypeCapture() {
        this.descriptor = (org.cache2k.configuration.CacheType<T>)of(((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }
    
    public static <T> CacheType<T> of(final Class<T> t) {
        return (CacheType<T>)of((Type)t);
    }
    
    public static CacheType of(final Type t) {
        if (t instanceof ParameterizedType) {
            final ParameterizedType pt = (ParameterizedType)t;
            final Class c = (Class)pt.getRawType();
            final CacheType[] ta = new CacheType[pt.getActualTypeArguments().length];
            for (int i = 0; i < ta.length; ++i) {
                ta[i] = of(pt.getActualTypeArguments()[i]);
            }
            return (CacheType)new OfGeneric(c, ta);
        }
        if (t instanceof GenericArrayType) {
            final GenericArrayType gat = (GenericArrayType)t;
            return (CacheType)new OfArray(of(gat.getGenericComponentType()));
        }
        if (!(t instanceof Class)) {
            throw new IllegalArgumentException("The run time type is not available, got: " + t);
        }
        final Class c2 = (Class)t;
        if (c2.isArray()) {
            return (CacheType)new OfArray(of(c2.getComponentType()));
        }
        return (CacheType)new OfClass(c2);
    }
    
    public CacheType<T> getBeanRepresentation() {
        return this.descriptor;
    }
    
    public CacheType<?> getComponentType() {
        return (CacheType<?>)this.descriptor.getComponentType();
    }
    
    public Class<T> getType() {
        return (Class<T>)this.descriptor.getType();
    }
    
    public CacheType<?>[] getTypeArguments() {
        return (CacheType<?>[])this.descriptor.getTypeArguments();
    }
    
    public String getTypeName() {
        return this.descriptor.getTypeName();
    }
    
    public boolean hasTypeArguments() {
        return this.descriptor.hasTypeArguments();
    }
    
    public boolean isArray() {
        return this.descriptor.isArray();
    }
    
    @Override
    public boolean equals(final Object o) {
        return this.descriptor.equals(o);
    }
    
    @Override
    public int hashCode() {
        return this.descriptor.hashCode();
    }
    
    static String arrayToString(final CacheType[] a) {
        if (a.length < 1) {
            throw new IllegalArgumentException();
        }
        final StringBuilder sb = new StringBuilder();
        final int l = a.length - 1;
        int i = 0;
        while (true) {
            sb.append(a[i].getTypeName());
            if (i == l) {
                break;
            }
            sb.append(',');
            ++i;
        }
        return sb.toString();
    }
    
    private abstract static class BaseType implements CacheType, Serializable
    {
        public CacheType getComponentType() {
            return null;
        }
        
        public Class getType() {
            return null;
        }
        
        public CacheType[] getTypeArguments() {
            return null;
        }
        
        public boolean hasTypeArguments() {
            return false;
        }
        
        public boolean isArray() {
            return false;
        }
        
        public CacheType getBeanRepresentation() {
            return (CacheType)this;
        }
        
        @Override
        public final String toString() {
            return "CacheTypeDescriptor#" + this.getTypeName();
        }
    }
    
    public static class OfClass extends BaseType
    {
        Class<?> type;
        
        public OfClass() {
        }
        
        public OfClass(final Class<?> type) {
            if (type.isArray()) {
                throw new IllegalArgumentException("array is not a regular class");
            }
            this.type = type;
        }
        
        @Override
        public Class<?> getType() {
            return this.type;
        }
        
        public void setType(final Class<?> type) {
            this.type = type;
        }
        
        static String shortenName(final String s) {
            final String _LANG_PREFIX = "java.lang.";
            if (s.startsWith("java.lang.")) {
                return s.substring("java.lang.".length());
            }
            return s;
        }
        
        public String getTypeName() {
            return shortenName(this.type.getCanonicalName());
        }
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final OfClass classType = (OfClass)o;
            return this.type.equals(classType.type);
        }
        
        @Override
        public int hashCode() {
            return this.type.hashCode();
        }
    }
    
    public static class OfArray extends BaseType
    {
        CacheType componentType;
        
        public OfArray() {
        }
        
        public OfArray(final CacheType componentType) {
            this.componentType = componentType;
        }
        
        @Override
        public boolean isArray() {
            return true;
        }
        
        @Override
        public CacheType getComponentType() {
            return this.componentType;
        }
        
        public void setComponentType(final CacheType componentType) {
            this.componentType = componentType;
        }
        
        static int countDimensions(CacheType td) {
            int cnt;
            for (cnt = 0; td.isArray(); td = td.getComponentType(), ++cnt) {}
            return cnt;
        }
        
        static Class<?> finalPrimitiveType(CacheType td) {
            while (td.isArray()) {
                td = td.getComponentType();
            }
            return (Class<?>)td.getType();
        }
        
        public String getTypeName() {
            final StringBuilder sb = new StringBuilder();
            final int _dimensions = countDimensions((CacheType)this);
            if (_dimensions > 1) {
                sb.append(finalPrimitiveType((CacheType)this));
            }
            else {
                sb.append(this.getComponentType().getTypeName());
            }
            for (int i = 0; i < _dimensions; ++i) {
                sb.append("[]");
            }
            return sb.toString();
        }
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final OfArray arrayType = (OfArray)o;
            return this.componentType.equals(arrayType.componentType);
        }
        
        @Override
        public int hashCode() {
            return this.componentType.hashCode();
        }
    }
    
    public static class OfGeneric extends BaseType
    {
        CacheType[] typeArguments;
        Class<?> type;
        
        public OfGeneric() {
        }
        
        public OfGeneric(final Class<?> type, final CacheType[] typeArguments) {
            this.typeArguments = typeArguments;
            this.type = type;
        }
        
        @Override
        public Class<?> getType() {
            return this.type;
        }
        
        public void setType(final Class<?> type) {
            this.type = type;
        }
        
        @Override
        public boolean hasTypeArguments() {
            return true;
        }
        
        @Override
        public CacheType[] getTypeArguments() {
            return this.typeArguments;
        }
        
        public void setTypeArguments(final CacheType[] typeArguments) {
            this.typeArguments = typeArguments;
        }
        
        public String getTypeName() {
            return OfClass.shortenName(this.type.getCanonicalName()) + "<" + CacheTypeCapture.arrayToString(this.typeArguments) + '>';
        }
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final OfGeneric that = (OfGeneric)o;
            return Arrays.equals(this.typeArguments, that.typeArguments) && this.type.equals(that.type);
        }
        
        @Override
        public int hashCode() {
            int result = Arrays.hashCode(this.typeArguments);
            result = 31 * result + this.type.hashCode();
            return result;
        }
    }
}
