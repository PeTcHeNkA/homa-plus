package plus.fmsg.cmd;

import plus.tson.ext.FuncCompiler;
import plus.tson.security.ClassManager;
import plus.tson.utl.ByteStrBuilder;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class TsonFuncCompiler extends ByteStrBuilder {
    private byte[] data;
    private int cursor;

    private String[]   varNames;
    private byte[][]   rawVarNames;
    private Class<?>[] varTypes;
    private final ClassManager manager;

    private ArrayList<FuncInfo> funcs = new ArrayList<>();

    public TsonFuncCompiler(int initLength, ClassManager manager) {
        super(16);
        this.manager = manager;
    }


    private String readMethodName(final byte[] data, int cursor){
        clear();
        byte c;
        for(final int length = data.length; cursor < length; ++cursor){
            if((c = data[cursor]) == '(' || c == '{') break;
            if(c == ' ' || c == '\n')continue;
            append(c);
        }
        this.cursor = cursor;
        return cString();
    }


    private void readRootVars(final byte[] data, int cursor){
        boolean isNone = true;
        boolean awaitName = false, awaitType = true;
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Class<?>> types = new ArrayList<>();

        byte c;
        clear();
        for(final int length = data.length; cursor < length; ++cursor){
            if((c = data[cursor]) == ')') break;
            if(c == ' ' || c == '\n'){
                if(awaitType){
                    awaitType = false;
                    awaitName = true;

                    types.add(manager.forName(cString()));
                    clear();
                    continue;
                }
                if(awaitName){
                    names.add(cString());
                    clear();
                    awaitName = false;
                }
                continue;
            }
            if(c == ','){
                if(awaitName){
                    names.add(cString());
                    clear();
                }
                isNone = true;
                awaitType = false;
                awaitName = false;
                continue;
            }
            if(isNone){
                awaitType = true;
                isNone = false;
            }
            append(c);

        }
        if(awaitName){
            names.add(cString());
            clear();
        }
        if(names.size() != types.size()){
            throw new RuntimeException("root vars not match");
        }
        varNames = names.toArray(new String[0]);
        varTypes = types.toArray(new Class<?>[0]);
        rawVarNames = new byte[names.size()][];

        for (int i = 0; i < varNames.length; i++) {
            rawVarNames[i] = varNames[i].getBytes();
        }

        this.cursor = cursor;
    }


    private void goToNonEmpty(){
        byte c;
        for(final int length = data.length; cursor < length; ++cursor){
            if((c = data[cursor]) != ' ' && c != '\n') break;
        }
    }


    private static boolean isReturn(byte[] data, int cursor){
        return data[cursor] == 'r' && data[cursor + 1] == 'e' && data[cursor + 2] == 't' && data[cursor + 3] == 'u' && data[cursor + 4] == 'r' && data[cursor + 5] == 'n';
    }


    private int getVar(final int cursor){
        byte[][] rawVarNames = this.rawVarNames;
        byte[] data = this.data;

        for (int varIndex = 0; varIndex < rawVarNames.length; varIndex++){
            if(isEqInPos(data, cursor, rawVarNames[varIndex]))return varIndex;
        }
        return -1;
    }


    private int[] readMethodCallIndexes(byte[] data, int cursor){
        clear();
        byte c;
        int[] indexes = new int[6];
        int countArgs = 0;
        boolean isReading = false;
        int startVarName = -1;

        for(final int length = data.length; cursor < length; ++cursor){
            if((c = data[cursor]) == ')') {
                if(isReading){
                    int var = getVar(startVarName);
                    if(var == -1)throw new RuntimeException("Var [" + new String(data, startVarName, cursor - startVarName) + "] not founded");

                    indexes[countArgs] = var;

                }
                break;
            }
            if(c == ' ' || c == '\n'){
                if(isReading){
                    int var = getVar(startVarName);
                    if(var == -1)throw new RuntimeException("Var [" + new String(data, startVarName, cursor - startVarName) + "] not founded");

                    indexes[countArgs++] = var;

                    isReading = false;
                }
                continue;
            }

            if(c == ','){
                if(isReading){
                    int var = getVar(startVarName);
                    if(var == -1)throw new RuntimeException("Var [" + new String(data, startVarName, cursor - startVarName) + "] not founded");

                    indexes[countArgs++] = var;

                    //endVarName = cursor;
                    isReading = false;
                }
                continue;
            }
            if(!isReading){
                isReading = true;
                startVarName = cursor;
            }
            //append(c);
        }
        this.cursor = cursor;

        if(countArgs > indexes.length){
            int[] newIndexes = new int[countArgs];
            System.arraycopy(indexes, 0, newIndexes, 0, indexes.length);
            indexes = newIndexes;
        }
        return indexes;
    }


    private FuncInfo readFuncInfo(Class<?> inType, String funcName){
        int[] indexes = readMethodCallIndexes(data, cursor);
        Method mtd = findMethod(inType, funcName, indexes);

        FuncInfo next = null;
        if(data[cursor] == '.'){
            String name = readMethodName(data, ++cursor);
            next = readFuncInfo(mtd.getReturnType(), name);
        }
        return new FuncInfo(FuncCompiler.compile(mtd), indexes, next);
    }


    private Method findMethod(Class<?> instType, String name, int[] argIndexes){
        Method result = null;
        l1: for (Method method:instType.getMethods()){
            if(method.getName().equals(name)){
                Class<?>[] types = method.getParameterTypes();
                if(types.length != argIndexes.length)continue;
                result = method;

                for (int i = 0; i < argIndexes.length; i++){
                    Class<?> type = varTypes[argIndexes[i]];
                    if(type != Object.class && type != null){
                        if(type != types[i])continue l1;
                    }
                }
                return method;
            }
        }
        return result;
    }


    private static boolean isEqInPos(byte[] data, int cursor, byte[] src){
        return isEqInPos(data, cursor, src, src.length);
    }


    private static boolean isEqInPos(byte[] data, int cursor, byte[] src, int srcLen){
        if(cursor + srcLen > data.length)return false;
        for (int i = 0; i < srcLen; i++){
            if(data[cursor + i] != src[i])return false;
        }
        return true;
    }


    public static class FuncCtx{
        private final int countVars;
        private final FuncInfo[] funcs;

        private FuncCtx(int countVars, FuncInfo[] funcs) {
            this.countVars = countVars;
            this.funcs = funcs;
        }

        public Object apply(Object inst, Object... args) {
            if(args.length < countVars) {
                Object[] rArgs = new Object[countVars];
                System.arraycopy(args, 0, rArgs, 0, args.length);
                args = rArgs;
            }
            for (int i = 0, s = funcs.length-1; i < s; i++){
                FuncInfo func = funcs[i];
                Object res = func.invoke(inst, args);
                if(func.resultIndex > 0) args[func.resultIndex] = res;
            }
            return funcs[funcs.length-1].invoke(inst, args);
        }
    }


    private static class FuncInfo{
        private final int[] usedIndexes;
        private final Object func;
        private int resultIndex = -1;
        private FuncInfo next;

        private FuncInfo(Object func, int[] usedIndexes, FuncInfo next) {
            this.usedIndexes = usedIndexes;
            this.func = func;
            this.next = next;
        }

        public Object invoke(Object inst, Object[] varRefs){
            int[] usedIndexes = this.usedIndexes;
            Object[] args = new Object[usedIndexes.length];
            for (int i = 0; i < usedIndexes.length; i++) {
                args[i] = varRefs[usedIndexes[i]];
            }
            Object result = FuncCompiler.invokeRaw(func, inst, args);
            if(next != null) return next.invoke(result, varRefs);
            return result;
        }
    }
}
