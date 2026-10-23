package plus.te4net.core.auth;


public final class AuthException extends Exception {
    public enum Type{
        NULL_USER,
        WRONG_PASSWORD,
        SQL,
        NO_CAP4A,
        NO_MAIL,
        NO_VALID_LOGIN,
        NO_VALID_PASSWORD,
        NO_VALID_NAME,
        READY_EXIST,
        READY_EXIST_NAME
    }
    public final Type type;
    public final Object target;

    public AuthException(Type type) {
        this.type = type;
        target = null;
    }


    AuthException(Type type, User user){
        this(type, user.login);
    }


    AuthException(Type type, Object target){
        this.type = type;
        this.target = target;
    }


    @Override
    public String toString() {
        return "ERROR "+(target==null?type.name():type.name()+" "+target);
    }
}