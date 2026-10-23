package plus.sol.reader;

import plus.sol.api.IEnvironment;
import plus.sol.api.IScript;
import plus.sol.utls.FileEnv;
import plus.sol.utls.flux.StrFlux;
import java.util.HashMap;


public class CodeSector<U> extends StrFlux implements FileEnv<U> {
    private final HashMap<Integer, IScript<U>> scripts = new HashMap<>();
    private final IEnvironment<U> parent;

    public CodeSector(IEnvironment<U> engine, String s) {
        super(s);
        this.parent = engine;
    }


    @Override
    public boolean hasScript(int id){
        if(parent != null && parent.hasScript(id)){
            return true;
        }
        return scripts.containsKey(id);
    }


    @Override
    public IEnvironment<U> getParent(){
        return parent;
    }


    @Override
    public void addScript(int id, IScript<U> script){
        scripts.put(id, script);
    }


    @Override
    public IScript<U> getScriptById(int id){
        IScript<U> result = scripts.get(id);
        if(result==null && parent != null){
            return parent.getScriptById(id);
        }
        return result;
    }
}