package plus.requests;

import plus.tson.TsonMap;


public class McJoinServer {
    public final String accessToken;
    public final String selectedProfile; //uuid
    public final String serverId;

    public McJoinServer(TsonMap map){
        accessToken     = map.getStr("accessToken"    );
        selectedProfile = map.getStr("selectedProfile");
        serverId        = map.getStr("serverId"       );
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        McJoinServer that = (McJoinServer) o;

        if (!accessToken.equals(that.accessToken)) return false;
        if (!selectedProfile.equals(that.selectedProfile)) return false;
        return serverId.equals(that.serverId);
    }


    @Override
    public int hashCode() {
        int result = accessToken.hashCode();
        result = 31 * result + selectedProfile.hashCode();
        result = 31 * result + serverId.hashCode();
        return result;
    }
}