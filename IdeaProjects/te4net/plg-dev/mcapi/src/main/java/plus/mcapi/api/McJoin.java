package plus.mcapi.api;

import plus.asyutl.shoulder.IBasicTask;
import plus.mcapi.MUser;
import plus.mcapi.McAuth;
import plus.requests.McJoinServer;
import plus.requests.McResponse;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.PluginRoute;
import plus.te4net.core.node.HEWorker;
import plus.te4net.core.node.HWorker;
import plus.te4net.mds.util.ByteUtils;
import plus.tson.TJsonParser;
import plus.tson.TsonMap;
import java.util.HashMap;
import static plus.mcapi.McAuth.basicApiRoute;


public class McJoin implements PluginRoute, IBasicTask {
    static HashMap<String, HashMap<String, MUser>> cache = new HashMap<>();
    private int timeClear = 0;
    private int usages = 0;

    public McJoin(HWorker worker){
        HEWorker.shoulder.addTask(this);
    }


    @Override
    public String get() {
        return basicApiRoute+"join";
    }


    @Override
    public void handle(RequestSession session) {
        System.out.println(session);

        TsonMap map = new TJsonParser(session.encodeBody()).getMap();
        McJoinServer request = new McJoinServer(map);
        MUser user = McAuth.inst.auth(request.accessToken, ByteUtils.EMPTY);
        if(user != null && user.uuid.equals(request.selectedProfile)){
            session.ok();
            getServer(request).put(user.name, user);
        } else {
            session.requestError(new McResponse("Token auth failed!").toString());
        }
    }


    private HashMap<String,MUser> getServer(McJoinServer request){
        HashMap<String,MUser> res = cache.get(request.serverId);
        if(res==null){
            res = new HashMap<>();
            if(usages>1000){
                usages = 0;
                timeClear = 0;
                run();
            }
            cache.put(request.serverId, res);
        }
        return res;
    }


    @Override
    public boolean needRun() {
        if(timeClear++>120){
            timeClear = 0;
            usages = 0;
            run();
        }
        return false;
    }


    @Override
    public void run() {
        cache = new HashMap<>();
    }
}