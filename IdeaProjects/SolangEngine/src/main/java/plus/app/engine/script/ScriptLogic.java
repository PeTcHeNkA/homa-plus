package plus.app.engine.script;

import plus.app.engine.module.AppVarProvider;
import plus.app.engine.module.User;
import plus.sol.utls.ScriptInitArgs;


public class ScriptLogic extends plus.sol.core.script.ScriptLogic<User> implements IAppScript {
    public ScriptLogic(ScriptInitArgs<User> args) {
        super(AppVarProvider::get, args);
    }
}