package plus.app.engine;

import plus.app.engine.module.AppConsoleHandler;
import plus.app.engine.module.User;
import plus.app.engine.script.*;
import plus.sol.api.IScript;
import plus.sol.core.ScriptEngine;
import plus.sol.core.exception.ScriptSyntaxError;
import plus.sol.core.script.ScriptBreak;
import plus.sol.core.script.ScriptLogicPart;
import plus.sol.reader.CleanedCodeFlux;
import plus.sol.reader.CodeSector;
import plus.sol.reader.CodeSectorFlux;
import plus.sol.utls.Logger;
import plus.sol.utls.flux.StrLinkedFileEnv;
import java.io.File;


public class AppScriptEngine extends ScriptEngine<User> {
    public AppConsoleHandler consoleHandler;
    private ScriptCompiler compiler;
    public AppScriptEngine(String scriptDir) {
        super(scriptDir);
    }


    @Override
    protected void preInit() {
        consoleHandler = new AppConsoleHandler(this, false);
        compiler = new ScriptCompiler(this);

        reg("MSG",    ScriptSendMsg   ::new      );
        reg("JUMP",   ScriptJump      ::new      );
        reg("BREAK",  ScriptBreak     ::new      );
        reg("RUN",    ScriptRun       ::new      );

        reg("IF",     ScriptLogic     ::new      );
        reg("WHEN",   ScriptLogicPart ::buildWhen);
        reg("ELSE",   ScriptLogicPart ::buildElse);
    }


    @Override
    protected String allowedSuffix() {
        return ".sol";
    }


    @Override
    public void onLoadFile(File file){
        CleanedCodeFlux<User> env = new CleanedCodeFlux<>(
                new StrLinkedFileEnv<>(this, file));

        CodeSectorFlux<User> csf = new CodeSectorFlux<>(env);
        while (csf.hasNext()){
            onLoadCode(csf.next());
        }
    }


    protected void onLoadCode(CodeSector<User> func){
        try {
            IScript<User> script = compiler.compile(func);
            if(script != null && script.getID()!=-1) {
                addScript(script.getID(), script);
            }
        } catch (ScriptSyntaxError e){
            Logger.error(e);
        }
    }
}