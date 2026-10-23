package plus.te4net.core.event;

import io.netty.util.collection.IntObjectHashMap;
import plus.te4net.core.api.IEventHandler;


public class EventBus {
    private final IntObjectHashMap<IEventHandler<?>[]> bus = new IntObjectHashMap<>();

    public static <T> Class<T> searchType(IEventHandler<T> handler){
        return handler.getHandledClass();
    }


    public <T> void addEventHandler(IEventHandler<T> handler){
        addEventHandler(searchType(handler), handler);
    }


    public <T> void addEventHandler(Class<T> clazz, IEventHandler<T> handler){
        try {
            int key = clazz.hashCode();
            IEventHandler[] handlers = bus.get(key);
            if (handlers == null) {
                handlers = new IEventHandler[1];
                bus.put(key, handlers);
            } else {
                IEventHandler[] newHandlers = new IEventHandler[handlers.length + 1];
                System.arraycopy(handlers, 0, newHandlers, 0, handlers.length);
                newHandlers[handlers.length] = handler;
                bus.put(key, newHandlers);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void addEventType(Class<?>... classes){
        IEventHandler[] empty = new IEventHandler[]{};
        for (Class<?> clazz:classes){
            bus.put(clazz.hashCode(), empty);
        }
    }


    public boolean throwEvent(Object obj){
        IEventHandler[] handlers = bus.get(obj.getClass().hashCode());
        if(handlers==null){
            throw new RuntimeException("Not founded supported type");
        }
        for (IEventHandler handler:handlers){
            try {
                if(handler.handle(obj))return true;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }
}