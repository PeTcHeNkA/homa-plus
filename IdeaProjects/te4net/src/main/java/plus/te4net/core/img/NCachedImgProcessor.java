package plus.te4net.core.img;

import plus.asyutl.shoulder.IBasicTask;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.bsys.IDataProvider;
import plus.tson.utl.Te4HashMap;
import java.util.Arrays;
import static plus.te4net.core.img.NSimpleImgProcessor.*;
import static plus.te4net.core.node.HWorkerRepository.shoulder;


public class NCachedImgProcessor extends Te4HashMap<NCachedImgProcessor.Key, NCachedImgProcessor.Img> implements IRequestProcessor, IBasicTask {
    private final IDataProvider provider;
    private int skip = 0;

    public NCachedImgProcessor(IDataProvider provider) {
        this.provider = provider;
        shoulder.addTask(this);
    }


    @Override
    public void handle(IRequestSession session) {
        String splatKey = getImgName(session.splat());
        String key = session.worker().getMainDomain()+"/"+splatKey;
        ImgSize size = getOptimalSize(session);
        Key realKey = new Key(key, size);
        Node<Key, Img> val = getNode(realKey);
        if(val!=null){
            val.getKey().use();
            Img img = val.getValue();
            handleImg(session, img.bytes, img.hs);
        } else {
            byte[] result = getStd(session.worker(), provider, splatKey, size);
            if(result==null){
                session.notFound();
                return;
            }
            Img img = new Img(result);
            realKey.use();
            put(realKey, img);
            handleImg(session, img.bytes, img.hs);
        }
    }


    @Override
    public boolean needRun() {
        if(skip++>180){
            if(size()>0) {
                for (Key key : keysToArray(keySet().toArray(new Key[0]))) {
                    if (key.notUsed()) {
                        remove(key);
                    }
                }
            }
            skip = 0;
        }
        return false;
    }


    @Override
    public void run() {}


    static class Img {
        final byte[] bytes;
        final int hs;
        Img(byte[] bytes) {
            this.bytes = bytes;
            hs = Arrays.hashCode(bytes);
        }
    }


    static class Key{
        final String key;
        final ImgSize size;
        final int hs;
        int usages = 0;
        Key(String key, ImgSize size) {
            this.key = key;
            this.size = size;
            hs = 31 * key.hashCode() + size.calcHs();
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            Key key1 = (Key) o;
            if (!key.equals(key1.key)) return false;
            return size.calc(1000) == key1.size.calc(1000);
        }
        void use(){
            ++usages;
        }
        boolean notUsed(){
            if(usages>30){
                usages = usages/2;
                return false;
            } else {
                if((usages-=10)<0){
                    return true;
                }
            }
            return usages==0;
        }
        @Override
        public int hashCode() {
            return hs;
        }
    }
}














