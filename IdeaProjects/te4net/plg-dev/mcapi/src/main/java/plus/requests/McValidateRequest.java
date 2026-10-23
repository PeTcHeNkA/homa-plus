package plus.requests;

import plus.tson.TsonMap;


public class McValidateRequest {
    public final String clientToken;
    public final String accessToken;

    public McValidateRequest(TsonMap map){
        clientToken = map.getStr("clientToken");
        accessToken = map.getStr("accessToken");
    }
}