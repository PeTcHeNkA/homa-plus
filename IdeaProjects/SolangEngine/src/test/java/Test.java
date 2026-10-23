import plus.app.engine.module.User;
import plus.app.engine.script.ScriptContainer;
import plus.sol.api.IScript;
import plus.sol.core.exception.ScriptSyntaxError;
import plus.sol.reader.*;
import plus.sol.reader.waiter.ComboWaiter;
import plus.sol.reader.waiter.IWaiter;
import plus.sol.reader.waiter.SmWaither;
import plus.sol.reader.waiter.ToEndWaiter;
import java.util.ArrayList;


public class Test {
    public static void main(String... args){
        try {
            //compile(new CodeSector<>(null,"\"test1\" (10) -> \"test2\"\n  a\n  b"), true);
            compileCommand(new CodeSector<>(null,"TEST 1"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    protected static IScript<User> compileCommand(CodeSector<User> func){
        ArrayList<Object> args = new ArrayList<>();
        String cmdName = readCmdName(func);
        Object arg = readNextArg(func);

        while (arg != null){
            Object nArg = readNextArg(func);
            if(nArg == null && arg instanceof String){
                if(((String)arg).trim().equals("->")){
                    args.add(compileSector(new CodeSectorFlux<>(func),-1));
                    break;
                }
            }
            args.add(arg);
            arg = nArg;
        }

        return compileCommand(cmdName, args);
    }

    protected static Object readNextArg(CodeSector<User> func){
        boolean emptyLine = true;
        IWaiter waiter = new ToEndWaiter(' ');
        while (func.hasNext()) {
            char c = func.next();
            if(emptyLine){
                if(c=='\n')return null;
                if(c==' ')continue;
                emptyLine = false;
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
            }
            if(waiter.wait(c)){
                break;
            }
            if(c == '\n' && waiter.isActive()){
                break;
            }
        }
        String val = waiter.getValue();
        if(val.isEmpty()){
            return null;
        }
        try {
            return Integer.parseInt(val);
        } catch (Exception ignored){}
        return val;
    }


    protected static void pArgs(String name, Object[] args){
        StringBuilder s = new StringBuilder();
        for(Object o:args){s.append(o).append(" ");}
        System.out.println(
                "Script Native lib has`nt script ["+name+"] args=("+ s.toString().trim()+")"
        );
    }


    protected static IScript<User> compile(CodeSector<User> func, boolean system){
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
        return null;
    }


    protected static IScript<User> compileSector(CodeSectorFlux<User> code, int scriptID){
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


    protected static IScript<User> compileCommand(String name, ArrayList<Object> args){
        pArgs(name, args.toArray());
        return null;
    }


    protected static String readCmdName(CodeSector<User> func){
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


    protected static IScript<User> compile0(boolean wordSet, CodeSector<User> func, boolean system){
        String txtTrigger = readTextTrigger(func);
        return compile1(txtTrigger, wordSet, func, system);
    }


    protected static IScript<User> compile1(String txtTrigger, boolean wordSet, CodeSector<User> func, boolean system){
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


    protected static String readBeforeEnd(CodeSector<User> func){
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


    protected static void readToSuper(CodeSector<User> func){
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


    protected static int readId(CodeSector<User> func){
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


    protected static String readTextTrigger(CodeSector<User> func){
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
