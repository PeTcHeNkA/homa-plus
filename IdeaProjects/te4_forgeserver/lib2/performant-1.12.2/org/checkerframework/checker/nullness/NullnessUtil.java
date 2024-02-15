//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.nullness;

import org.checkerframework.checker.nullness.qual.*;

public final class NullnessUtil
{
    private NullnessUtil() {
        throw new AssertionError((Object)"shouldn't be instantiated");
    }
    
    @EnsuresNonNull({ "#1" })
    public static <T> T castNonNull(final T ref) {
        assert ref != null : "Misuse of castNonNull: called with a null argument";
        return ref;
    }
    
    @EnsuresNonNull({ "#1" })
    public static <T> T[] castNonNullDeep(final T[] arr) {
        return (T[])castNonNullArray((Object[])arr);
    }
    
    @EnsuresNonNull({ "#1" })
    public static <T> T[][] castNonNullDeep(final T[][] arr) {
        return castNonNullArray(arr);
    }
    
    @EnsuresNonNull({ "#1" })
    public static <T> T[][][] castNonNullDeep(final T[][][] arr) {
        return castNonNullArray(arr);
    }
    
    @EnsuresNonNull({ "#1" })
    public static <T> T[][][][] castNonNullDeep(final T[][][][] arr) {
        return castNonNullArray(arr);
    }
    
    @EnsuresNonNull({ "#1" })
    public static <T> T[][][][][] castNonNullDeep(final T[][][][][] arr) {
        return castNonNullArray(arr);
    }
    
    private static <T> T[] castNonNullArray(final T[] arr) {
        assert arr != null : "Misuse of castNonNullArray: called with a null array argument";
        for (int i = 0; i < arr.length; ++i) {
            assert arr[i] != null : "Misuse of castNonNull: called with a null array element";
            checkIfArray(arr[i]);
        }
        return arr;
    }
    
    private static void checkIfArray(final Object ref) {
        assert ref != null : "Misuse of checkIfArray: called with a null argument";
        final Class<?> comp = ref.getClass().getComponentType();
        if (comp != null) {
            if (!comp.isPrimitive()) {
                castNonNullArray((Object[])ref);
            }
        }
    }
}
