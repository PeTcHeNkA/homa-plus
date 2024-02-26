package plus.te4net.mds.crypt;

import java.util.concurrent.ScheduledExecutorService;


public abstract class CryptoLibSimple<T extends CryptKey,V> extends CryptoLibBase<T,V>{
    protected CryptoLibSimple(ScheduledExecutorService service, int skip) {
        super(service, skip);
    }


    @Override
    protected T encodeKey(byte[] bytes, int delta) {
        throw new UnsupportedOperationException();
    }


    @Override
    protected V encodeValue(byte[] data, int delta) {
        throw new UnsupportedOperationException();
    }
}
