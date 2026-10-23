package plus.app.engine.script;

import plus.app.engine.module.User;
import plus.sol.utls.ScriptInitArgs;


public class ScriptChoice implements IAppScript{
    public ScriptChoice(ScriptInitArgs<User> userScriptInitArgs) {
    }

    @Override
    public boolean run(User user) {
        return true;
    }
}