package plus.app.engine.module;

import plus.app.engine.module.event.EventMsg;
import plus.app.engine.module.event.MsgEventListener;
import plus.sol.api.IEventListener;
import plus.sol.core.ScriptEngine;
import plus.sol.core.handler.ConsoleHandler;


public class AppConsoleHandler extends ConsoleHandler<User, EventMsg> {
    public AppConsoleHandler(ScriptEngine<User> engine, boolean deamon) {
        super(deamon);
    }


    @Override
    protected void onMsg(String[] args) {
        EventMsg arg = new EventMsg(args);
        for(IEventListener<User, EventMsg> listener:baseListeners){
            listener.onCallEvent(User.system, arg);
        }
        User.system.onCallEvent(arg);
    }
}