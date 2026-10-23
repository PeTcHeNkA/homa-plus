package plus.graalspy;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.graalvm.polyglot.*;


public final class GraalEventListener implements Listener {
    private final Value func;

    public GraalEventListener(Value func) {
        if(!func.canExecute())throw new IllegalArgumentException("Value is not executable");
        this.func = func;
    }

    void execute(Event event) {
        func.execute(event);
    }
}