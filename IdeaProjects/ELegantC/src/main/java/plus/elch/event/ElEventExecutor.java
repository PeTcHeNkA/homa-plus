package plus.elch.event;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;


public final class ElEventExecutor implements EventExecutor {
    public static final ElEventExecutor INSTANCE = new ElEventExecutor();

    private ElEventExecutor(){}

    @Override
    public void execute(Listener listener, Event event) {
        ((ElEventListener)listener).handle(event);
    }
}