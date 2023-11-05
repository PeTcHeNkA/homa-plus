package plus.te4net.mds.html;

import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.node.HEWorker;

@PublicUsage
public class SfHtmlRoute extends HtmlRoute {
    private final HEWorker worker;

    public SfHtmlRoute(HEWorker worker, String path, String routeName) {
        super(worker, path, routeName);
        this.worker = worker;
    }

    public SfHtmlRoute(HEWorker worker, byte[] data, String routeName) {
        super(worker, data, routeName);
        this.worker = worker;
    }


    @Override
    public void handle(IRequestSession session) {
        if(!session.isSafe() && worker.maybeSafe()){
            session.redirect("/"+get()+"/"+session.routeKey().afterRoute());
            return;
        }
        if(worker.checkAuth(session)){
            super.handle(session);
        }
    }
}