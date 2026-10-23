package plus.te4net.mds.crypt;

import plus.te4net.core.api.PublicUsage;


@PublicUsage
public class CryptException extends RuntimeException{
    enum Type{
        INVALID_KEY,
        NULL_KEY;
    }
    public final Type type;
    public final Object target;


    public CryptException(Type type) {
        this.type = type;
        target = null;
    }


    CryptException(Type type, Object target){
        this.type = type;
        this.target = target;
    }


    @Override
    public String toString() {
        return "ERROR "+(target==null?type.name():type.name()+" "+target);
    }
}