package plus.mcapi.api;

import plus.mcapi.McUser;
import plus.mcapi.McAuth;
import plus.requests.McResponse;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.PluginRoute;
import java.util.ArrayList;
import static plus.mcapi.McAuth.basicApiRoute;


public class McSearchProfiles implements PluginRoute {
    @Override
    public String get() {
        return basicApiRoute+"users";
    }


    @Override
    public void handle(RequestSession session) {
        System.out.println(session);

        String name = session.splat()[0];
        ArrayList<McUser> users = McAuth.inst.getByName(name);
        if(users.isEmpty()){
            session.send(new McResponse("Error", "Not founded", "Not founded").toString());
            return;
        }
        StringBuilder builder = new StringBuilder("[");
        for(McUser user:users){
            builder.append(user.toGameProfile()).append(",");
        }
        builder.setCharAt(builder.length()-1, ']');
        session.send(builder.toString());
    }
}