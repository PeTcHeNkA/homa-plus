package plus.sol.core;

import plus.sol.api.IEnvironment;
import plus.sol.api.IScript;
import java.util.HashMap;


public class Environment<U> extends HashMap<Integer, IScript<U>> implements IEnvironment<U> {
    protected final IEnvironment<U> parent;

    public Environment(IEnvironment<U> parent) {
        this.parent = parent;
    }


    @Override
    public boolean hasScript(int id){
        if(parent != null && parent.hasScript(id)){
            return true;
        }
        return containsKey(id);
    }


    public IEnvironment<U> getParent(){
        return parent;
    }


    @Override
    public void addScript(int id, IScript<U> script){
        put(id, script);
    }


    @Override
    public IScript<U> getScriptById(int id){
        IScript<U> result = get(id);
        if(result==null && parent != null){
            return parent.getScriptById(id);
        }
        return result;
    }
}