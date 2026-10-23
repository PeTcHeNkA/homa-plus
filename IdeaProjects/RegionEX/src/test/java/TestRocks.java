import org.rocksdb.RocksDBException;
import plus.region.data.db.RocksDataManager;
import java.nio.charset.StandardCharsets;
import java.util.Random;


public class TestRocks {
    static RocksDataManager<String> db = new RocksDataManager<>("test.db", true, new RocksDataManager.Coder<String>() {
        @Override
        public byte[] encode(String obj) {
            return obj.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String decode(byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    });


    public static void main(String[] args) throws RocksDBException {
        for (int i=0;i<20;i++){
            main();
        }
        db.close();
    }


    public static void main() throws RocksDBException {


        int count = 1_000;

        Random random = new Random(1);
        byte[] data = new byte[128*8*16];
        random.nextBytes(data);

        //System.out.println("Summ size: "+((data.length/1024)*count) );

        long time = System.currentTimeMillis();

        for (int i=0;i<count;i++){
            data[20] = (byte) i;
            db.putRaw(i, data);
        }

        time = System.currentTimeMillis() - time;

        System.out.println("Spend time: "+time);

    }
}
