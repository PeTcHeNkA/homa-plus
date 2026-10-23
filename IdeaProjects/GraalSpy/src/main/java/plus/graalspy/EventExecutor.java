package plus.graalspy;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;


public class EventExecutor implements org.bukkit.plugin.EventExecutor {
    public static final EventExecutor INSTANCE = new EventExecutor();

    private EventExecutor() {}
    @Override
    public void execute(Listener listener, Event event) throws EventException {
        ((GraalEventListener)listener).execute(event);
    }
}