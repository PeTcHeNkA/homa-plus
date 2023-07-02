package plus.te4net.core.html;

import plus.te4net.core.node.HWorker;
import java.io.File;


public interface IBuilder {
    SimpleCompressedTextFile build(HWorker worker, File file);
}