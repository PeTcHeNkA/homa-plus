package plus.app.engine.module.event;


import plus.app.engine.module.User;
import plus.sol.api.IEventListener;
import plus.sol.api.IScript;


public class MsgEventListener implements IEventListener<User, EventMsg> {
    private final String keyPhrase;
    private final IScript<User> script;

    public MsgEventListener(String keyPhrase, IScript<User> script) {
        this.keyPhrase = keyPhrase;
        this.script = script;
    }


    @Override
    public boolean onCallEvent(User user, EventMsg arg) {
        boolean flag = check(arg);
        if(flag) {
            script.run(user);
        }
        return flag;
    }


    boolean check(EventMsg args){
        int summ = 1;
        for(String s: args.args){
            s = s.trim();
            if(s.isEmpty())continue;
            if(keyPhrase.contains(s)){
                ++summ;
            }
        }
        return summ >= args.args.length;
    }
}
