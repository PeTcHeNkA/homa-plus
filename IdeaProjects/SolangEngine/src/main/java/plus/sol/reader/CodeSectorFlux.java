package plus.sol.reader;

import plus.sol.api.IEnvironment;
import plus.sol.core.exception.ScriptSyntaxError;
import plus.sol.utls.FileEnv;
import plus.sol.utls.flux.Flux;


public class CodeSectorFlux<U> implements Flux<CodeSector<U>> {
    private FileEnv<U> env;
    private StringBuilder builder = new StringBuilder(128);
    private StringBuilder temp = new StringBuilder(32);

    private boolean emptyLine = true;
    private int eSize = 0;
    private int curSize = 0;


    public CodeSectorFlux(FileEnv<U> env) {
        if(env == null) throw new NullPointerException();
        if(env.hasNext()) {
            this.env = env;
        }

        while (env.hasNext()) {
            char c = env.next();
            if (c == ' ') {
                eSize += 1;
                temp.append(c);
            } else if (c == '\n') {
                eSize = 0;
                temp = new StringBuilder(32);
            } else {
                curSize = eSize;
                env.setCursor(env.getCursor()-1);
                break;
            }
        }
    }


    public IEnvironment<U> getParent(){
        return env.getParent();
    }


    @Override
    public CodeSector<U> next() {
        while (env.hasNext()) {
            char c = env.next();
            if(c == ' '){
                if(emptyLine) {
                    curSize += 1;
                }
                if(curSize>eSize || !emptyLine) temp.append(' ');
            } else if(c == '\n'){
                curSize = 0;
                if(!emptyLine){
                    builder.append(temp).append('\n');
                    temp = new StringBuilder(32);
                }
                emptyLine = true;
            } else {
                if(emptyLine) {
                    emptyLine = false;
                    if(curSize<eSize) throw new ScriptSyntaxError(env, "Less padding than expected!");
                    if(eSize == curSize){
                        builder.append(temp);
                        if(builder.length()!=0) {
                            StringBuilder b2 = builder;
                            builder = new StringBuilder(128);
                            temp = new StringBuilder("" + c);

                            String s = b2.substring(0, b2.length()-1);
                            if(!s.trim().isEmpty()) return new CodeSector<>(env, s);
                        } else {
                            temp = new StringBuilder("" + c);
                        }
                        continue;
                    }
                }
                temp.append(c);
            }
        }


        CodeSector<U> result = new CodeSector<>(env, builder.append(temp).toString());
        eSize = -1;
        temp = null;
        builder = null;
        return result;
    }


    @Override
    public boolean hasNext() {
        return eSize != -1;
    }
}