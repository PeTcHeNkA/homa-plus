package plus.sol.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

import plus.sol.api.IEnvironment;
import plus.sol.api.IScript;
import plus.sol.utls.ScriptInitArgs;


public abstract class ScriptEngine<U> extends Environment<U> {
    private final HashMap<String,Function<ScriptInitArgs<U>, IScript<U>>> nativeLib = new HashMap<>();


    public ScriptEngine(String scriptDir){
        super(null);
        preInit();
        loadFiles(new File(scriptDir));
    }


    protected void preInit(){}


    protected abstract String allowedSuffix();


    public void loadFiles(File scriptDir){
        if(!scriptDir.exists()){
            scriptDir.mkdirs();
            return;
        }
        if(scriptDir.isFile()){
            onLoadFile(scriptDir);
        } else {
            loadAllFiles(scriptDir);
        }
    }


    private void loadAllFiles(File file){
        if(file.isFile()){
            if(file.getName().endsWith(allowedSuffix())) onLoadFile(file);
        } else {
            File[] files = file.listFiles();
            if(files != null){
                for(File file1:files){
                    loadAllFiles(file1);
                }
            }
        }
    }


    protected void reg(String name, Function<ScriptInitArgs<U>, IScript<U>> func){
        nativeLib.put(name, func);
    }


    public void runScriptById(int id, U user){
        get(id).run(user);
    }


    public IScript<U> newScript(IEnvironment<U> env, String name, Object... args){
        if(!nativeLib.containsKey(name)){
            StringBuilder s = new StringBuilder();
            for(Object o:args){s.append(o).append(" ");}
            throw new NullPointerException(
                    "Script Native lib has`nt script ["+name+"] args=("+ s.toString().trim()+")"
            );
        }
        return nativeLib.get(name).apply(new ScriptInitArgs<>(this, env, args));
    }


    protected abstract void onLoadFile(File file);
}