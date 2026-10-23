package plus.app.engine.module;

import plus.app.engine.module.event.IEvent;
import plus.sol.api.IEventListener;
import java.util.ArrayList;
import java.util.HashMap;


public class User{

    public enum UserType {
        USER,SYSTEM,ADMIN
    }

    public static final User admin = new User("admin"){
        public UserType getType(){return UserType.ADMIN;}
    };

    public static final User system = new User("system"){
        public UserType getType(){return UserType.SYSTEM;}
    };

    private HashMap<Object, Object> tempVars = new HashMap<>();
    public String name;


    public User(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }


    public UserType getType(){
        return UserType.USER;
    }


    public Object getVar(String key){
        return tempVars.get(key);
    }


    public void setVar(Object key, Object var){
        tempVars.put(key, var);
    }


    public boolean onCallEvent(IEvent arg) {
        Object obj = tempVars.get(arg.getType());
        if(obj==null){
            tempVars.put(arg.getType(), new ArrayList<IEventListener<User, IEvent>>());
        } else {
            ArrayList<IEventListener<User, IEvent>> listeners = (ArrayList<IEventListener<User, IEvent>>) obj;
            for (int i = 0; i < listeners.size(); i++) {
                IEventListener<User, IEvent> listener = listeners.get(i);
                if (listener.onCallEvent(this, arg)) {
                    listeners.remove(listener);
                }
            }
        }
        return true;
    }
}