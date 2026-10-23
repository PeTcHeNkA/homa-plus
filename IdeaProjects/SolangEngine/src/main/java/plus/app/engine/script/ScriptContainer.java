package plus.app.engine.script;

import plus.app.engine.module.User;
import plus.sol.api.IScript;
import java.util.Collection;


public class ScriptContainer extends plus.sol.core.script.ScriptContainer<User> implements IAppScript {
    public ScriptContainer(Collection<IScript<User>> iScripts, int id) {
        super(iScripts, id);
    }
}