package plus.sol.legacy;

import plus.sol.core.exception.ScriptSyntaxError;
import plus.sol.utls.FileEnv;
import plus.sol.utls.flux.EStrFlux;
import plus.sol.utls.flux.StrLinkedFileEnv;
import java.util.ArrayList;


public class Funcs {
    public static void splitToSectors(FileEnv env){
        boolean preStart = true;
        boolean emptyLine = true;

        int eSize = 0;
        int curSize = 0;

        ArrayList<String> groups = new ArrayList<>(5);
        StringBuilder builder = new StringBuilder(128);
        StringBuilder temp = new StringBuilder();

        while (env.hasNext()) {
            char c = env.next();
            if(preStart){
                if(c == ' ') {
                    eSize += 1;
                    temp.append(c);
                    continue;
                } else if(c == '\n'){
                    eSize = 0;
                    temp = new StringBuilder();
                    continue;
                } else {
                    preStart = false;
                    curSize = eSize;
                }
            }
            if(c == ' '){
                if(emptyLine) {
                    curSize += 1;
                }
                temp.append(' ');
            } else if(c == '\n'){
                curSize = 0;
                if(!emptyLine){
                    builder.append(temp).append('\n');
                    temp = new StringBuilder();
                }
                emptyLine = true;
            } else {
                if(emptyLine) {
                    emptyLine = false;
                    if(curSize<eSize) throw new ScriptSyntaxError(env, "Less padding than expected!");

                    if(eSize == curSize){
                        builder.append(temp);
                        if(builder.length()!=0) {
                            groups.add(builder.substring(0, builder.length()-1));
                            builder = new StringBuilder(128);
                        }
                        temp = new StringBuilder("" + c);
                        continue;
                    }
                }
                temp.append(c);
            }
        }
        groups.add(builder.toString());


        for(String s:groups){
            System.out.println("----------\n"+s);
        }
    }


    public static FileEnv remComments(FileEnv env){
        StringBuilder builder = new StringBuilder(512);
        StringBuilder temp = new StringBuilder(32);
        boolean preStart = true;
        boolean emptyLine = true;
        boolean ignoreToEndLine = false;
        boolean ignoreAllLine = false;

        while (env.hasNext()){
            char c = env.next();
            if(c == '\r') continue;
            if(preStart){
                if(c == ' ') {
                    temp.append(c);
                    continue;
                } else if(c == '\n'){
                    temp = new StringBuilder(32);
                    continue;
                } else {
                    builder = temp;
                    temp = new StringBuilder(32);
                    preStart = false;
                }
            }
            if(c == '#'){
                ignoreToEndLine = true;
                ignoreAllLine = emptyLine;
                continue;
            }
            if(ignoreToEndLine){
                ignoreToEndLine = c != '\n';
                if(!ignoreToEndLine){
                    if(ignoreAllLine){
                        temp = new StringBuilder(32);
                    } else temp.append('\n');
                }
                continue;
            }
            if(c == '\n'){
                emptyLine = true;
                builder.append(temp).append('\n');
                temp = new StringBuilder();
                continue;
            } else {
                if(emptyLine && c != ' ') emptyLine = false;
            }
            temp.append(c);
        }
        builder.append(temp);

        return build(env, builder +"\n");
    }


    public static EStrFlux<?> build(Object env, String val){
        if(env==null)return new EStrFlux<>(val);
        return new StrLinkedFileEnv<>(null, null);
    }
}
