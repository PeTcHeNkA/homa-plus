package plus.sol.reader;

import plus.sol.core.Environment;
import plus.sol.utls.FileEnv;


public class CleanedCodeFlux<U> extends Environment<U> implements FileEnv<U> {
    private StringBuilder builder = new StringBuilder(512);
    private StringBuilder temp = new StringBuilder(32);

    boolean emptyLine = true;
    boolean ignoreToEndLine = false;
    boolean ignoreAllLine = false;

    private int cursor;
    private int size = -1;

    public CleanedCodeFlux(FileEnv<U> env) {
        super(env);

        while (env.hasNext()) {
            char c = env.next();
            if (c == '\r') continue;
            if (c == ' ') {
                temp.append(c);
            } else if (c == '\n') {
                temp = new StringBuilder(32);
            } else {
                builder = temp;
                temp = new StringBuilder(32);
                env.setCursor(env.getCursor()-1);
                break;
            }
        }
    }


    private void filterNextBatch(){
        while (true){
            FileEnv<U> env = (FileEnv<U>) parent;
            if(!env.hasNext()){
                builder.append(temp).append('\n');
                size = builder.length();
                return;
            }
            char c = env.next();
            if(c == '\r') continue;

            if(c == '#'){
                ignoreToEndLine = true;
                ignoreAllLine = emptyLine;
                continue;
            }
            if(ignoreToEndLine){
                ignoreToEndLine = c != '\n';
                if(!ignoreToEndLine){
                    if(ignoreAllLine) temp = new StringBuilder(32);
                    else temp.append('\n');
                }
            } else {
                if(c == '\n'){
                    emptyLine = true;
                    builder.append(temp).append('\n');
                    temp = new StringBuilder();
                    return;
                } else if(emptyLine && c != ' ') emptyLine = false;
                temp.append(c);
            }
        }
    }


    @Override
    public int getCursor() {
        return cursor;
    }


    @Override
    public void setCursor(int cursor) {
        this.cursor = cursor;
    }


    @Override
    public char next() {
        if(size>0) {
            return builder.charAt(cursor++);
        } else {
            int i = cursor++;
            if (builder.length() <= i) {
                filterNextBatch();
            }
            return builder.charAt(i);
        }
    }


    @Override
    public char current() {
        return builder.charAt(cursor);
    }


    @Override
    public boolean hasNext() {
        return size < 0 || cursor < size;
    }
}