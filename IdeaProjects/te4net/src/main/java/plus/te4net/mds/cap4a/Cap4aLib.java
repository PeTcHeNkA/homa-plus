package plus.te4net.mds.cap4a;

import plus.te4net.mds.crypt.CryptoLibSimple;



public class Cap4aLib<V> extends CryptoLibSimple<Cap4a,V> {
    public Cap4aLib() {
        super((short) 180);
    }


    @Override
    protected Cap4a newKey(long time, short usages) {
        return new Cap4a(time);
    }

}
