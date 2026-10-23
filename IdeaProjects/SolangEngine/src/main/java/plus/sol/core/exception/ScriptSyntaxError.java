package plus.sol.core.exception;

import plus.sol.utls.FileEnv;


public class ScriptSyntaxError extends RuntimeException{
    private static final int expandErrorCode = 2;
    private static final String head = "----------------------------------";

    public ScriptSyntaxError(FileEnv<?> flux, String msg) {
        super(buildMessage(flux, msg));
    }

    public static void notHere(FileEnv<?> flux, char c){
        throw new ScriptSyntaxError(flux, "Char ["+c+"] not allowed here!");
    }

    public static void notNewLine(FileEnv<?> flux){
        throw new ScriptSyntaxError(flux, "Char [\\n] not allowed here!");
    }

    public static void notExist(FileEnv<?> flux, String msg){
        throw new ScriptSyntaxError(flux, "["+msg+"] not exist!");
    }


    private static String buildMessage(FileEnv<?> flux, String msg){
        final int errorCursor = flux.getCursor();
        StringBuilder msg2 = new StringBuilder();

        int countLines = expandErrorCode;
        int startCursor = errorCursor;
        boolean isErrorString = false;
        boolean isPrintedError = false;

        while (startCursor > 0 && countLines >= 0){
            startCursor-=1;
            flux.setCursor(startCursor);
            if(flux.current() == '\n'){
                countLines -= 1;
            }
        }
        countLines = expandErrorCode+1+expandErrorCode;

        while (flux.hasNext()){
            char c = flux.next();
            int cursor = flux.getCursor();
            if(cursor >= startCursor){
                if(c == '\n'){
                    if(countLines-->0) {
                        if(cursor > errorCursor && !isPrintedError){
                            isErrorString = true;
                            isPrintedError = true;
                        }
                        if(isErrorString){
                            msg2 = new StringBuilder(msg2.substring(0, msg2.length() - 1));
                            msg2.append(" #<-- ERROR! ").append(msg).append('\n');
                            isErrorString = false;
                        } else {
                            msg2.append('\n');
                        }
                    } else break;
                } else msg2.append(c);
            }
        }
        if(!isPrintedError){
            return msg + '\n'+head+'\n'+msg2+'\n'+head;
        }
        return '\n'+head+'\n'+msg2+'\n'+head;
    }
}