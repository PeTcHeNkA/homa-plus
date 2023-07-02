package plus.te4net.core.bsys;

import plus.tson.utl.Tuple;
import java.util.HashSet;
import static plus.te4net.core.node.HWorkerRepository.pool;


public class NDubleProvider implements IDataProvider {
    private final HashSet<Tuple<String,String>> blocked = new HashSet<>();
    private final IDataProvider fastProvider;
    private final IDataProvider defProvider;

    public NDubleProvider(IDataProvider fastProvider, IDataProvider defProvider) {
        this.fastProvider = fastProvider;
        this.defProvider = defProvider;
    }


    @Override
    public byte[] getRaw(String name, String table) {
        byte[] result = fastProvider.getRaw(name, table);
        if(result != null)return result;
        result = defProvider.getRaw(name, table);
        if(result==null)return null;
        Tuple<String, String> key = new Tuple<>(name, table);
        if(blocked.add(key)) {
            byte[] finalResult = result;
            pool.put(() -> {
                fastProvider.saveRaw(name, table, finalResult);
                blocked.remove(key);
            });
        }
        return result;
    }


    @Override
    public void saveRaw(String name, String table, byte[] data) {
        fastProvider.saveRaw(name, table, data);
    }
}
