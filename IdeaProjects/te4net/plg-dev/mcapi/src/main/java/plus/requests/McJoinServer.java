package plus.requests;

import plus.mcapi.McAuth;
import plus.mcapi.McUser;
import plus.te4net.core.api.RequestSession;
import plus.tson.TJsonParser;
import plus.tson.TsonMap;


public class McJoinServer {
    public final String accessToken;
    public final String serverId;
    public final McUser user;

    public McJoinServer(RequestSession session){
        TsonMap map = new TJsonParser(session.encodeBody()).getMap();
        accessToken            = map.getStr("accessToken"    );
        serverId               = map.getStr("serverId"       );
        user = McAuth.inst.getByUUID(map.getStr("selectedProfile"));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        McJoinServer that = (McJoinServer) o;

        if (!accessToken.equals(that.accessToken)) return false;
        if (user == null) return that.user == null;
        if(!user.uuid.equals(that.user.uuid))return false;
        return serverId.equals(that.serverId);
    }


    @Override
    public int hashCode() {
        int result = accessToken.hashCode();
        if(user != null)
            result = 31 * result + user.uuid.hashCode();
        result = 31 * result + serverId.hashCode();
        return result;
    }
}