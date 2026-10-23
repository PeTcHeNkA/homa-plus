package plus.app.engine.script;

import plus.app.engine.module.User;
import plus.sol.api.IScript;
import plus.sol.core.exception.ScriptSyntaxError;
import plus.sol.utls.FileEnv;
import plus.sol.utls.Logger;
import plus.sol.utls.ScriptInitArgs;


public class ScriptJump implements IAppScript{
    private IScript<User> script;
    public ScriptJump(ScriptInitArgs<User> args) {
        try {
            int id = (Integer) args.args[0];
            script = (IAppScript) env -> {
                IScript<User> temp = args.environment.getScriptById(id);
                if(temp==null){
                    script = (IAppScript) env1 -> true;
                    Logger.log("Script ["+id+"] not founded! Skipped.");
                    return true;
                }
                script = temp;
                return temp.run(env);
            };
        } catch (IndexOutOfBoundsException | ClassCastException e){
            if(args.environment instanceof FileEnv<?>){
                throw new ScriptSyntaxError((FileEnv<?>) args.environment, "");
            }
            throw new RuntimeException();
        }
    }


    @Override
    public boolean run(User user) {
        return script.run(user);
    }
}