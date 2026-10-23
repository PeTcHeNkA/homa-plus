package plus.requests;

import plus.tson.TsonMap;
import plus.tson.TsonObj;


public class McRefreshRequest {
    public final String clientToken;
    public final String accessToken;
    public final TsonObj selectedProfile;
    public final boolean requestUser;

    public McRefreshRequest(TsonMap map){
        clientToken     = map.getStr("clientToken");
        accessToken     = map.getStr("accessToken");
        selectedProfile = map.get("selectedProfile");
        requestUser     = map.getOrDefaultBool("requestUser", false);
    }
}