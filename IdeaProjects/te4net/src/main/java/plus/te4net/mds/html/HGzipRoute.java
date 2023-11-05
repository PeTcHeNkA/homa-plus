package plus.te4net.mds.html;

import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.node.HEWorker;
import java.io.File;

@PublicUsage
public class HGzipRoute extends CGzipRoute{
    private final IRequestProcessor preProcessor;
    public HGzipRoute(HEWorker worker, File dir, int cacheTime, IBuilder builder, IRequestProcessor preProcessor) {
        super(worker, dir, cacheTime, builder);
        this.preProcessor = preProcessor;
    }

    @Override
    public void handle(IRequestSession session) {
        preProcessor.handle(session);
        super.handle(session);
    }
}
