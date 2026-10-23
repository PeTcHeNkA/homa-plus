package plus.app.engine.script;

import plus.app.engine.module.User;
import plus.sol.utls.ScriptInitArgs;


public class ScriptRun extends ScriptJump{
    public ScriptRun(ScriptInitArgs<User> args) {
        super(args);
    }


    @Override
    public boolean run(User user) {
        super.run(user);
        return true;
    }
}
