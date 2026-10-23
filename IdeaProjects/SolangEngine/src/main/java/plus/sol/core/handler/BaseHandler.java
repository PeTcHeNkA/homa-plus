package plus.sol.core.handler;

import plus.sol.api.IEventListener;
import java.util.ArrayList;


public class BaseHandler<U, E> {
    protected final ArrayList<IEventListener<U, E>> baseListeners = new ArrayList<>();

    public void addBaseListener(IEventListener<U, E> e){
        baseListeners.add(e);
    }
}
