package net.minecraftforge.fml.common.thread;

import java.util.concurrent.*;
import net.minecraftforge.fml.relauncher.*;
import javax.annotation.*;


public class SidedThreadGroup extends ThreadGroup implements ThreadFactory {
    private final Side side;

    SidedThreadGroup(final Side side) {
        super(side.name());
        this.side = side;
    }

    public Side getSide() {
        return this.side;
    }

    @Override
    public Thread newThread(@Nonnull final Runnable runnable) {
        return new Thread(this, runnable);
    }
}