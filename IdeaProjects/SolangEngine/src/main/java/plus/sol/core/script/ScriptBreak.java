package plus.sol.core.script;

import plus.app.engine.module.User;
import plus.sol.api.IScript;
import plus.sol.utls.ScriptInitArgs;


public final class ScriptBreak<U> implements IScript<U> {
    public ScriptBreak(ScriptInitArgs<User> userScriptInitArgs) {}

    @Override
    public boolean run(U user) {
        return false;
    }
}