package plus.mcapi.api;

import plus.mcapi.MUser;
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

        HashMap<String, MUser> server = McJoin.cache.get(serverId);
        if(server==null){
            session.requestError("Not joined!");
            return;
        }
        MUser user = server.remove(username);

        if(user==null){
            session.requestError("Not joined!");
            return;
        }

        session.send(new McHasJoined(user.uuid, user.properties()).toString());
    }
}





