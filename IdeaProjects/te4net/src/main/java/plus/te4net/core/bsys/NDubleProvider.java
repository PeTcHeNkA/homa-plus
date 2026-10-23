package plus.te4net.core.bsys;

import plus.te4net.core.api.IDataProvider;
import java.util.HashSet;
import java.util.concurrent.Executor;


public class NDubleProvider implements IDataProvider {
    private final HashSet<String[]> blocked = new HashSet<>();
    private final IDataProvider fastProvider;
    private final IDataProvider defProvider;
    private final Executor service;

    public NDubleProvider(IDataProvider fastProvider, IDataProvider defProvider, Executor service) {
        this.fastProvider = fastProvider;
        this.defProvider = defProvider;
        this.service = service;
    }


    @Override
    public byte[] getRaw(String... path) {
        byte[] result;
        if((result = fastProvider.getRaw(path)) != null)return result;
        if((result = defProvider .getRaw(path)) == null)return null;

        if(blocked.add(path)) {
            byte[] finalResult = result;
            service.execute(() -> {
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


    @Override
    public String[] getItems(String... path) {
        return defProvider.getItems(path);
    }
}
