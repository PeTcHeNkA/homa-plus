package plus.fserver.server.network;

public class FastExitException extends RuntimeException{
    public static final FastExitException INSTANCE = new FastExitException();
    private final StackTraceElement[] elems = new StackTraceElement[]{};
    private String msg;

    private FastExitException(){}


    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return elems;
    }


    public FastExitException msg(String msg){
        System.err.println(this.msg = msg);
        return this;
    }


    @Override
    public String getMessage() {
        return msg;
    }


    @Override
    public Throwable getCause() {
        return this;
    }
}