package plus.mcapi.api;

import plus.requests.McJoinServer;
import plus.requests.McResponse;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.PluginRoute;
import plus.tson.utl.Te4HashMap;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import static plus.mcapi.McAuth.basicApiRoute;


public class McJoin implements PluginRoute, Runnable {
    private final ScheduledExecutorService service;
    private ScheduledFuture<?> future;
    static Te4HashMap<String, HashMap<String, McJoinServer>> cache = new Te4HashMap<>();
    private int usages = 0;

    public McJoin(ScheduledExecutorService service){
        this.service = service;
    }


    private void restartIfNeed(){
        if(future == null) future = service.scheduleAtFixedRate(this, 60_000, 120_000, TimeUnit.MILLISECONDS);
    }


    @Override
    public String get() {
        return basicApiRoute+"join";
    }


    @Override
    public void handle(RequestSession session) {
        System.out.println(session);
        McJoinServer request = new McJoinServer(session);

        if(request.user != null){
            session.ok();
            getServer(request).put(request.user.name, request);
        } else {
            session.requestError(new McResponse("Token auth failed!").toString());
        }
    }


    private HashMap<String, McJoinServer> getServer(McJoinServer request){
        HashMap<String, McJoinServer> res = cache.get(request.serverId);
        if(res == null){
            res = new HashMap<>();
            if(usages > 1000){
                usages = 0;
                run();
            }
            cache.fput(request.serverId, res);
            restartIfNeed();
        }
        return res;
    }


    @Override
    public void run() {
        if(cache.isEmpty()){
            future.cancel(false);
            future = null;
        } else cache = new Te4HashMap<>();
    }
}