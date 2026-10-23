package plus.sol.utls;

import plus.sol.api.IEnvironment;
import plus.sol.core.ScriptEngine;


public class ScriptInitArgs<U> {
    public final ScriptEngine<U> engine;
    public final IEnvironment<U> environment;
    public final Object[] args;
    public final int ID;

    public ScriptInitArgs(ScriptEngine<U> engine, IEnvironment<U> environment, int ID, Object[] args) {
        this.engine = engine;
        this.environment = environment;
        this.args = args;
        this.ID = ID;
    }


    public ScriptInitArgs(ScriptEngine<U> engine, IEnvironment<U> environment, Object[] args) {
        this.engine = engine;
        this.environment = environment;
        this.args = args;
        ID = -1;
    }
}