package plus.sol.core.script;

import plus.app.engine.module.User;
import plus.sol.api.IScript;
import plus.sol.utls.ScriptInitArgs;


public class ScriptLogicPart<U> extends ScriptProxy<U> {
    private final boolean mode;

    public static<U> IScript<U> buildWhen(ScriptInitArgs<User> args){
        return new ScriptLogicPart(true, args);
    }


    public static<U> IScript<U> buildElse(ScriptInitArgs<User> args){
        return new ScriptLogicPart(false, args);
    }


    public ScriptLogicPart(boolean mode, ScriptInitArgs<User> args){
        this(args.ID, mode, (IScript<U>) args.args[args.args.length-1]);
    }


    public ScriptLogicPart(int id, boolean mode, IScript<U> parent) {
        super(id, parent);
        this.mode = mode;
    }


    public boolean getMode(){
        return mode;
    }
}