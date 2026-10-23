package plus.sol.utls.flux;

import plus.sol.api.IEnvironment;
import plus.sol.api.IScript;
import plus.tson.TsonFile;
import java.io.File;
import java.util.HashMap;


public class StrLinkedFileEnv<U> extends EStrFlux<U> {
    private final HashMap<Integer, IScript<U>> scripts = new HashMap<>();
    protected final IEnvironment<U> parent;

    public StrLinkedFileEnv(IEnvironment<U> parent, File file) {
        super(readFile(file));
        this.parent = parent;
    }


    public static String readFile(File file){
        return TsonFile.read(file,"").replace("\t","    ");
    }


    @Override
    public boolean hasScript(int id){
        if(parent != null && parent.hasScript(id)){
            return true;
        }
        return scripts.containsKey(id);
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