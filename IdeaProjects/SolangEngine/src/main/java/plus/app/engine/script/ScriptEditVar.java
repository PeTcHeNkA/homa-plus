package plus.app.engine.script;

import plus.app.engine.module.AppVarProvider;
import plus.app.engine.module.User;
import plus.sol.api.IEnvironment;
import plus.sol.core.math.Editor;
import java.util.ArrayList;


public class ScriptEditVar implements IAppScript{
    private final Editor<User, ?> editor;
    private final IEnvironment<User> env;

    public ScriptEditVar(IEnvironment<User> env, String name, ArrayList<Object> args){
        this.env = env;
        ArrayList<String> args0 = new ArrayList<>();
        args0.add(name);
        for(Object o:args){
            args0.add(o.toString());
        }
        editor = Editor.build(AppVarProvider::get, args0);
    }


    @Override
    public boolean run(User user) {
        editor.edit(user, env);
        return true;
    }
}