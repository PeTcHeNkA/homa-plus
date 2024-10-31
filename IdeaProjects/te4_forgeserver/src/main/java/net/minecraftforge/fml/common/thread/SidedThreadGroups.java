package net.minecraftforge.fml.common.thread;

import net.minecraftforge.fml.relauncher.*;
import org.jetbrains.annotations.NotNull;
import plus.fserver.server.ElcaThread;


public final class SidedThreadGroups {
    public static final SidedThreadGroup CLIENT;
    public static final SidedThreadGroup SERVER;

    private SidedThreadGroups() {}


    static {
        CLIENT = new SidedThreadGroup(Side.CLIENT);
        SERVER = new SidedThreadGroup(Side.SERVER){
            @Override
            public Thread newThread(@NotNull Runnable runnable) {
                return new ElcaThread(runnable);
            }
        };
    }
}