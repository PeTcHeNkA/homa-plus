package plus.te4net.core.html;

import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.html.HtmlRoute;
import plus.te4net.core.node.HWorker;

@PublicUsage
public class SfHtmlRoute extends HtmlRoute {
    private final HWorker worker;

    public SfHtmlRoute(HWorker worker, String path, String routeName) {
        super(worker, path, routeName);
        this.worker = worker;
    }

    public SfHtmlRoute(HWorker worker, byte[] data, String routeName) {
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