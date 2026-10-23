package plus.mcapi.api;

import plus.requests.McJoinServer;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.PluginRoute;
import java.util.HashMap;
import plus.requests.McHasJoined;
import static plus.mcapi.McAuth.basicApiRoute;


public class McJoined implements PluginRoute {
    @Override
    public String get() {
        return basicApiRoute+"hasJoined";
    }

    @Override
    public void handle(RequestSession session) {
        System.out.println(session);

        String username = session.queryParams("username");
        String serverId = session.queryParams("serverId");
        String ip       = session.queryParams("ip"      );

        HashMap<String, McJoinServer> server = McJoin.cache.get(serverId);
        if(server==null){
            session.requestError("Not joined!");
            return;
        }
        McJoinServer joinData = server.remove(username);

        if(joinData==null){
            session.requestError("Not joined!");
            return;
        }

        session.send(new McHasJoined(joinData.user.uuid, joinData.user.properties()).toString());
    }
}





