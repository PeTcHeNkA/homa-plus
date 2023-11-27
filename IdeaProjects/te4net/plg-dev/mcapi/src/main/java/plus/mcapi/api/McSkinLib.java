package plus.mcapi.api;

import plus.mcapi.McUser;
import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.api.PluginRoute;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.Var;
import plus.te4net.core.node.HEWorker;
import plus.te4net.mds.util.CharNum;
import plus.te4net.mds.util.FastRandom;
import java.util.Objects;


public class McSkinLib implements PluginRoute {
    private final IDataProvider provider;

    public McSkinLib(@Var(key = "userskins", def = "sql:userskins.db") IDataProvider provider){
       this.provider = provider;
    }


    @Override
    public String get() {
        return "mcapi/skins";
    }


    @Override
    public void handle(RequestSession session) {
        HEWorker.getLogger().info(session);
        if(Objects.equals("set", session.splat()[0])) {
            McUser user = (McUser) session.auth();
            if (user == null) {
                session.requestError("Auth please");
                return;
            }
            String rSkinName = CharNum.Default.getCharNumOf(FastRandom.INST.nextInt()) + ".png";

            String url;
            byte[] body = session.encodeBody();
            if(body.length>0) {
                url = session.worker().getMainHost() + "/" + get() + "/" + user.name + "/" + rSkinName;
                provider.saveRaw(session.encodeBody(), "skins", user.name + "/" + rSkinName);
            } else {
                url = user.getSkinUrl();
            }
            user.setSkin(url, Objects.equals(session.queryParams("slim"),"true"));


            session.worker().getAuthSystem().update(user);

            session.send(url);
        } else {
            byte[] res = provider.getRaw("skins", session.routeKey().afterRoute());
            if(res==null) session.notFound();
            else session.send(res);
        }
    }
}