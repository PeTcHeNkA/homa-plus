package plus.te4net.mds.crypt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


public abstract class CryptoLibFile<T extends CryptKey,V> extends CryptoLibBase<T,V> {
    private final File file;

    public CryptoLibFile(File file){
        super((short) 20);
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