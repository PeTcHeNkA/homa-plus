package plus.mcapi.api;

import plus.mcapi.McUser;
import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.api.PluginRoute;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.Var;
import plus.te4net.core.bsys.DataProviderBuilder;
import plus.te4net.core.node.HEWorker;
import plus.te4net.mds.util.CharNum;
import plus.te4net.mds.util.FastRandom;
import plus.tson.TsonMap;
import java.util.Objects;


public class McSkinLib implements PluginRoute {
    private final IDataProvider provider;

    public McSkinLib(HEWorker worker, @Var(key = "userskins", def = "sql:userskins.db")
            TsonMap provider){
       this.provider = DataProviderBuilder.ofTson(worker, provider);
    }


    @Override
    public String get() {
        return "mcapi/skins";
    }


    @Override
    public void handle(RequestSession session) {
        if(Objects.equals("set", session.splat()[0])) {
            McUser user = (McUser) session.auth();
            if (user == null) {
                session.requestError("Auth please");
                return;
            }
            HEWorker.getLogger().info(session);
            String rSkinName = CharNum.Default.getCharNumOf(FastRandom.INST.nextInt()) + ".png";

            provider.saveRaw(session.encodeBody(), user.name, rSkinName);
            String url = session.worker().getMainHost()+"/"+get()+"/"+user.name+"/"+rSkinName;

            user.setSkin(url, Objects.equals(session.queryParams("slim"),"true"));
            session.worker().getAuthSystem().update(user);

            session.send(url);
        } else {
            byte[] res = provider.getRaw(session.routeKey().afterRoute());
            if(res==null) session.notFound();
            else session.send(res);
        }
    }
}