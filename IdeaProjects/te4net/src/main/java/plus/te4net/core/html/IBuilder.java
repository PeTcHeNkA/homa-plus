package plus.te4net.core.html;

import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.node.HWorker;
import java.io.File;

@PublicUsage
public interface IBuilder {
    IRequestProcessor build(HWorker worker, File file);
}