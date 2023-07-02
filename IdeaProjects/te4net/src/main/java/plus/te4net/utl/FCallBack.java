package plus.te4net.utl;


public class FCallBack extends RuntimeException{
    @Override
    public Throwable fillInStackTrace(){
        return this;
    }
}