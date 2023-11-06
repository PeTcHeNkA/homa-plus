package plus.te4net.mds.html;

import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.node.HEWorker;
import java.io.File;


@PublicUsage
public class CustomGzipFile extends SimpleCompressedTextFile {
    private final String type;

    public CustomGzipFile(String type, HEWorker worker, String name, String data) {
        super(worker, name, data);
        this.type = type;
    }


    public CustomGzipFile(String type, HEWorker worker, File file) {
        super(worker, file);
        this.type = type;
    }


    public CustomGzipFile(String type, HEWorker worker, String name, String data, boolean compress) {
        super(worker, name, data, compress);
        this.type = type;
    }


    public CustomGzipFile(String type, IDataProvider worker, String name, String data){
        super(worker, name, data);
        this.type = type;
    }


    @Override
    public String type() {
        return type;
    }
}