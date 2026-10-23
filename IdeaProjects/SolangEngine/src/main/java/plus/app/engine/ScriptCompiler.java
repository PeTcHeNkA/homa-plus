package plus.app.engine;

import plus.app.engine.module.AppVarProvider;
import plus.app.engine.module.User;
import plus.app.engine.module.event.MsgEventListener;
import plus.app.engine.script.ScriptContainer;
import plus.app.engine.script.ScriptEditVar;
import plus.app.engine.script.ScriptSendMsg;
import plus.sol.api.IEnvironment;
import plus.sol.api.IScript;
import plus.sol.api.IScriptCompiler;
import plus.sol.api.IVarProxy;
import plus.sol.core.exception.ScriptSyntaxError;
import plus.sol.reader.CodeSector;
import plus.sol.reader.CodeSectorFlux;
import plus.sol.reader.waiter.ComboWaiter;
import plus.sol.reader.waiter.IWaiter;
import plus.sol.reader.waiter.SmWaither;
import plus.sol.reader.waiter.ToEndWaiter;
import java.util.ArrayList;


public class ScriptCompiler implements IScriptCompiler<User> {
    private final AppScriptEngine engine;
    public ScriptCompiler(AppScriptEngine engine) {
        this.engine = engine;
    }


    @Override
    public IScript<User> compile(CodeSector<User> func) {
        return compile(func, true);
    }


    protected IScript<User> compile(CodeSector<User> func, boolean system){
        char c = func.current();
        if(c=='"'){
            return compile0(false, func, system);
        }
        if(c=='('){
            return compile1(null, false, func, system);
        }
        if(c=='&'){
            func.next();
            c = func.current();
            if(c=='"'){
                return compile0(true, func, system);
            } else {
                ScriptSyntaxError.notHere(func, c);
            }
        }
        if(c=='>'){
            func.next();
            func.next();
            return compileCommand(func);
        }
        return compileSimpleMsg(func);
    }


    protected IScript<User> compileSimpleMsg(CodeSector<User> code){
        StringBuilder msg = new StringBuilder();
        while (code.hasNext()){
            msg.append(code.next());
        }
        return new ScriptSendMsg(code, msg.toString());
    }


    protected IScript<User> compileSector(CodeSectorFlux<User> code, int scriptID){
        if(code.hasNext()) {
            ArrayList<IScript<User>> scripts = new ArrayList<>();
            while (code.hasNext()) {
                IScript<User> script = compile(code.next(), false);
                if (script != null) {
                    scripts.add(script);
                }
            }
            if(scripts.isEmpty()){
                return null;
            }
            IScript<User> result = new ScriptContainer(scripts, scriptID);
            if(scriptID != -1){
                code.getParent().addScript(scriptID, result);
            }
            return result;
        }
        return null;
    }


    protected IScript<User> compileCommand(IEnvironment<User> env, String name, ArrayList<Object> args){
        IVarProxy<User, ?> var = AppVarProvider.get(name);
        if(var instanceof IVarProxy.Constant) {
            return engine.newScript(env, name.trim(), args.toArray());
        } else {
            return new ScriptEditVar(env, name, args);
        }
    }


    @Deprecated
    protected IScript<User> compileCommand(CodeSector<User> func){
        ArrayList<Object> args = new ArrayList<>();
        String cmdName = readCmdName(func);
        Object arg = readNextArg(func);

        while (arg != null){
            Object nArg = readNextArg(func);
            if(nArg == null && arg instanceof String){
                if(((String)arg).equals("->")){
                    args.add(compileSector(new CodeSectorFlux<>(func),-1));
                    break;
                }
            }
            args.add(arg);
            arg = nArg;
        }
        return compileCommand(func, cmdName, args);
    }


    protected Object readNextArg(CodeSector<User> func){
        boolean emptyLine = true;
        IWaiter waiter = new ToEndWaiter(' ');
        while (func.hasNext()) {
            char c = func.next();
            if(emptyLine){
                if(c=='\n')return null;
                if(c==' ')continue;
                if(c=='"'){
                    waiter = new ToEndWaiter('"');
                    while (func.hasNext()) {
                        c = func.next();
                        if(waiter.wait(c)){
                            return waiter.getValue();
                        }
                    }
                    ScriptSyntaxError.notExist(func, "\"");
                }
                emptyLine = false;
            }
            if(waiter.wait(c)){
                break;
            }
            if(c == '\n' && waiter.isActive()){
                func.setCursor(func.getCursor()-1);
                break;
            }
        }
        String val = waiter.getValue().trim();
        if(val.isEmpty()){
            return null;
        }
        try {
            return Integer.parseInt(val);
        } catch (Exception ignored){}
        return val;
    }


    protected String readCmdName(CodeSector<User> func){
        ToEndWaiter toEnd = new ToEndWaiter(' ');
        boolean flag = true;
        while (func.hasNext()) {
            char c = func.next();
            if(c == ' ' && flag) continue;
            if(flag){
                flag = false;
            }
            if(toEnd.wait(c)){
                return toEnd.getValue();
            }
            if(toEnd.isActive() && c == '\n'){
                return toEnd.getValue();
            }
        }
        throw new ScriptSyntaxError(func, "Command name not founded!");
    }


    protected IScript<User> compile0(boolean wordSet, CodeSector<User> func, boolean system){
        String txtTrigger = readTextTrigger(func);
        IScript<User> result = compile1(txtTrigger, wordSet, func, system);

        if(system) {
            engine.consoleHandler.addBaseListener(new MsgEventListener(txtTrigger, result));
        } else {
            //
        }
        return result;
    }


    protected IScript<User> compile1(String txtTrigger, boolean wordSet, CodeSector<User> func, boolean system){
        int id = readId(func);
        readToSuper(func);

        String suffix = readBeforeEnd(func);
        CodeSectorFlux<User> subCode = new CodeSectorFlux<>(func);
        boolean flag = subCode.hasNext();

        if(suffix != null && !flag){
            return compileCommand(new CodeSector<>(func, suffix));
        }
        if(flag){
            return compileSector(subCode, id);
        }
        return null;
    }


    protected String readBeforeEnd(CodeSector<User> func){
        ToEndWaiter toEnd = new ToEndWaiter('\n');
        boolean empty = true;
        boolean flag1 = false;

        while (func.hasNext()) {
            char c = func.next();

            if(empty && c == '"'){
                toEnd = new ToEndWaiter('"');
                flag1 = true;
                continue;
            }

            if(c != ' ')empty = false;

            if(toEnd.wait(c)){
                if(!flag1) {
                    String val = toEnd.getValue().trim();
                    if (val.isEmpty()) return null;
                    return val;
                } else {
                    String toReturn = toEnd.getValue();
                    while (func.hasNext()) {
                        c = func.next();
                        if(c=='\n')return toReturn;
                        if(c!=' ')ScriptSyntaxError.notHere(func, c);
                    }
                }
            }
        }
        return null;
    }


    protected void readToSuper(CodeSector<User> func){
        ComboWaiter waitSuper = new ComboWaiter("->");
        while (func.hasNext()) {
            char c = func.next();
            if(waitSuper.wait(c)){
                return;
            }
            if(c=='\n'){
                throw new ScriptSyntaxError(func,"[->] Not founded!");
            }
        }
    }


    protected int readId(CodeSector<User> func){
        SmWaither waitID = new SmWaither('(',')');
        while (func.hasNext()) {
            char c = func.next();
            if(c==' ')continue;
            if(waitID.wait(c)){
                try {
                    String s = waitID.getValue();
                    return Integer.parseInt(s);
                } catch (NumberFormatException e){
                    throw new ScriptSyntaxError(func,"Number format exception!");
                }
            }
            if(waitID.isActive())continue;
            if(c == '-'){
                func.setCursor(func.getCursor()-1);
                return -1;
            } else ScriptSyntaxError.notHere(func, c);
        }
        return -1;
    }


    protected String readTextTrigger(CodeSector<User> func){
        SmWaither waitQ = new SmWaither('"','"');
        while (func.hasNext()) {
            char c = func.next();
            if(waitQ.wait(c)){
                return waitQ.getValue();
            }
            if(!waitQ.isActive()) {
                if (c == '(') {
                    func.setCursor(func.getCursor()-1);
                    return null;
                } else {
                    if(c == ' ')continue;
                    if(c == '\n')ScriptSyntaxError.notNewLine(func);
                    ScriptSyntaxError.notHere(func, c);
                }
            }
        }
        return null;
    }
}
