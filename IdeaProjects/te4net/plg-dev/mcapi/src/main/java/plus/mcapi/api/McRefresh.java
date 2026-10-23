package plus.mcapi.api;

import plus.mcapi.McAuth;
import plus.requests.McRefreshRequest;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.PluginRoute;
import plus.tson.TJsonParser;
import plus.tson.TsonMap;


public class McRefresh implements PluginRoute {
    @Override
    public String get() {
        return McAuth.basicApiRoute+"refresh";
    }

    @Override
    public void handle(RequestSession session) {
        System.out.println(session);

        TsonMap map = new TJsonParser(session.encodeBody()).getMap();
        McRefreshRequest mrr = new McRefreshRequest(map);
        //TODO
    }
}
