package plus.mcapi.api;

import plus.mcapi.McAuth;
import plus.requests.McValidateRequest;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.PluginRoute;
import plus.tson.TJsonParser;
import plus.tson.TsonMap;


public class McValidate implements PluginRoute {
    @Override
    public String get() {
        return McAuth.basicApiRoute+"validate";
    }

    @Override
    public void handle(RequestSession session) {
        System.out.println(session);

        TsonMap map = new TJsonParser(session.encodeBody()).getMap();
        McValidateRequest mvd = new McValidateRequest(map);
        //TODO

    }
}
