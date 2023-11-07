package plus.mcapi.api;

import plus.mcapi.MUser;
import plus.mcapi.McAuth;
import plus.te4net.core.api.PluginRoute;
import plus.te4net.core.api.RequestSession;
import plus.tson.TsonMap;

import static plus.mcapi.McAuth.basicApiRoute;


public class McProfile implements PluginRoute {
    @Override
    public String get() {
        return basicApiRoute+"profile";
    }


    @Override
    public void handle(RequestSession session) {
        System.out.println(session);

        String uuid = session.splat()[0];
        MUser user = McAuth.inst.getByUUID(uuid);
        if(user==null){
            session.notFound();
            return;
        }
        session.send(user.toGameProfile());
    }
}








