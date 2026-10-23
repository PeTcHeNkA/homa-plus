package plus.requests;

import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.TsonSerelizable;


public class McResponse implements TsonSerelizable {
    private final String error;
    private final String errorMessage;
    private final String cause;

    public McResponse(String errorMessage){
        this(errorMessage, null, null);
    }

    public McResponse(String error, String errorMessage, String cause) {
        this.error = error;
        this.errorMessage = errorMessage;
        this.cause = cause;
    }

    @Override
    public TsonObj toTson() {
        TsonMap map = new TsonMap();
        if(error != null){
            map.put("error", error);
        }
        if(errorMessage != null){
            map.put("errorMessage", error);
        }
        if(cause != null){
            map.put("cause", error);
        }
        return map;
    }

    @Override
    public String toString() {
        return toTson().toJsonStr();
    }
}