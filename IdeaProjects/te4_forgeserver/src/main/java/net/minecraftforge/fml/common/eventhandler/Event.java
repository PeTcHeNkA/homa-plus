package net.minecraftforge.fml.common.eventhandler;

import javax.annotation.*;
import java.lang.annotation.*;


public class Event {
    private boolean isCanceled;
    private Result result;
    private EventPriority phase;

    public Event() {
        this.isCanceled = false;
        this.result = Result.DEFAULT;
        this.phase = null;
        this.setup();
    }


    public boolean isCancelable() {
        return false;
    }


    public boolean isCanceled() {
        return this.isCanceled;
    }


    public void setCanceled(final boolean cancel) {
        if (!this.isCancelable()) {
            throw new UnsupportedOperationException("Attempted to call Event#setCanceled() on a non-cancelable event of type: " + this.getClass().getCanonicalName());
        }
        this.isCanceled = cancel;
    }


    public boolean hasResult() {
        return false;
    }


    public Result getResult() {
        return this.result;
    }


    public void setResult(final Result value) {
        this.result = value;
    }


    protected void setup() {}

    //event system
    private static ListenerList listeners;
    public ListenerList getListenerList() {
        return listeners;
    }
    static {
        listeners = new ListenerList();
    }
    //event system


    @Nullable
    public EventPriority getPhase() {
        return this.phase;
    }


    public void setPhase(@Nonnull final EventPriority value) {
        this.phase = value;
    }


    public enum Result {
        DENY,
        DEFAULT,
        ALLOW;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE })
    public @interface HasResult { }
}