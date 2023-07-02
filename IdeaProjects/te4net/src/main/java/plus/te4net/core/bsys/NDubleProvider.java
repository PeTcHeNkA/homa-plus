package plus.te4net.core.bsys;

import java.util.HashSet;
import static plus.te4net.core.node.HWorkerRepository.pool;


public class NDubleProvider implements IDataProvider {
    private final HashSet<String[]> blocked = new HashSet<>();
    private final IDataProvider fastProvider;
    private final IDataProvider defProvider;

    public NDubleProvider(IDataProvider fastProvider, IDataProvider defProvider) {
        this.fastProvider = fastProvider;
        this.defProvider = defProvider;
    }


    @Override
    public byte[] getRaw(String... path) {
        byte[] result = fastProvider.getRaw(path);
        if(result != null)return result;
        result = defProvider.getRaw(path);
        if(result==null)return null;
        if(blocked.add(path)) {
            byte[] finalResult = result;
            pool.put(() -> {
                fastProvider.saveRaw(finalResult, path);
                blocked.remove(path);
            });
        }
        return result;
    }


    @Override
    public void saveRaw(byte[] data, String... path) {
        fastProvider.saveRaw(data, path);
    }
}
