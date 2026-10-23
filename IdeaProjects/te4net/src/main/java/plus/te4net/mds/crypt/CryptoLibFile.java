package plus.te4net.mds.crypt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ScheduledExecutorService;


public abstract class CryptoLibFile<T extends CryptKey,V> extends CryptoLibBase<T,V> {
    private final File file;

    public CryptoLibFile(ScheduledExecutorService service, File file){
        super(service, 20_000);
        this.file = file;
        try {
            if(file.exists()) {
                fromBytes(Files.readAllBytes(file.toPath()));
            } else {
                file.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void onRun() {
        try {
            Files.write(file.toPath(), toBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}