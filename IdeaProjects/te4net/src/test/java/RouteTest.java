import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.mapping.HRouteMap;

public class RouteTest {
    public static void main(String[] args) {

        HRouteMap map = new HRouteMap();
        IRequestProcessor proc = new IRequestProcessor() {
            @Override
            public void handle(RequestSession session) {

            }
        };
//        map.put("/tweak", proc);
//        map.put("tweak", proc);

        System.out.println(map.get("/tweak/result.json"));

    }
}
