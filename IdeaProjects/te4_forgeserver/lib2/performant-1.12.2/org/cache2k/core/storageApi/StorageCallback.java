//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.storageApi;

public interface StorageCallback
{
    void onReadSuccess(final StorageEntry p0);
    
    void onReadFailure(final Throwable p0);
    
    void onStoreSuccess(final boolean p0);
    
    void onStoreFailure(final Throwable p0);
}
