package plus.tjo.listener;

import org.bukkit.event.Event;
import org.bukkit.plugin.EventExecutor;


public final class FExecutor implements EventExecutor {
    public static FExecutor INSTANCE = new FExecutor();

    @Override
    public void execute(org.bukkit.event.Listener listener, Event event) {
        ((Listener)listener).handle(event);
    }


    @FunctionalInterface
    public interface Listener<T extends Event> extends org.bukkit.event.Listener{
        void handle(T event);
    }
}