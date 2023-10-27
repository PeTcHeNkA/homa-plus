package plus.te4net.core.html;

import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.node.HWorker;
import java.io.File;

@PublicUsage
public class HGzipRoute extends CGzipRoute{
    private final IRequestProcessor preProcessor;
    public HGzipRoute(HWorker worker, File dir, int cacheTime, IBuilder builder, IRequestProcessor preProcessor) {
        super(worker, dir, cacheTime, builder);
        this.preProcessor = preProcessor;
    }

    @Override
    public void handle(IRequestSession session) {
        preProcessor.handle(session);
        super.handle(session);
    }
}
