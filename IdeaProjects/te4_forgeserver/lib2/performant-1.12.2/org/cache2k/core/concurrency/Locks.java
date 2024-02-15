//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.concurrency;

public class Locks
{
    private static Class<? extends OptimisticLock> optimisticLockImplementation;
    
    public static OptimisticLock newOptimistic() {
        if (Locks.optimisticLockImplementation == null) {
            initializeOptimisticLock();
        }
        try {
            return (OptimisticLock)Locks.optimisticLockImplementation.newInstance();
        }
        catch (Exception ex) {
            throw new Error(ex);
        }
    }
    
    private static void initializeOptimisticLock() {
        try {
            if (System.getProperty(NonOptimisticLock.class.getName()) == null) {
                new OptimisticLockStamped();
                Locks.optimisticLockImplementation = OptimisticLockStamped.class;
                return;
            }
        }
        catch (NoClassDefFoundError noClassDefFoundError) {}
        Locks.optimisticLockImplementation = NonOptimisticLock.class;
    }
}
