package plus.elch.event;

import org.bukkit.event.Event;


public interface ElEventListener<T extends Event> {
    void handle(T event);
}