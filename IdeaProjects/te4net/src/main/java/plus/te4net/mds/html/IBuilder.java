package plus.te4net.mds.html;

import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.node.HEWorker;
import java.io.File;


@PublicUsage
public interface IBuilder {
    IRequestProcessor build(HEWorker worker, File file);
}