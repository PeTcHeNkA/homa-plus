package plus.te4net.mds.img;

import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.IDataProvider;
import plus.tson.utl.Te4HashMap;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class NCachedImgProcessor extends Te4HashMap<NCachedImgProcessor.Key, NCachedImgProcessor.Img> implements IRequestProcessor, Runnable {
    private final ScheduledExecutorService service;
    private final IDataProvider provider;
    ScheduledFuture<?> future;

    public NCachedImgProcessor(ScheduledExecutorService service, IDataProvider provider) {
        this.provider = provider;
        this.service = service;
    }


    private void restartIfNeed(){
        if(future == null)future = service.scheduleAtFixedRate(this, 10_000, 10_000, TimeUnit.MILLISECONDS);
    }


    @Override
    public void handle(RequestSession session) {
        ImgSize size = NSimpleImgProcessor.getOptimalSize(session);
        if(size==null){
            session.requestError("You're a bot right?");
            return;
        }
        String key = session.routeKey().afterRoute();
        Key realKey = new Key(key, size);
        Node<Key, Img> val = getNode(realKey);
        if(val!=null){
            val.getKey().use();
            Img img = val.getValue();
            NSimpleImgProcessor.handleImg(session, img.bytes, img.hs);
        } else {
            byte[] result = NSimpleImgProcessor.getStd(session, provider, size);
            if(result==null){
                session.notFound();
                return;
            }
            Img img = new Img(result);
            realKey.use();
            fput(realKey, img);
            restartIfNeed();
            NSimpleImgProcessor.handleImg(session, img.bytes, img.hs);
        }
    }


    @Override
    public void run() {
        if(size()>0) {
            for (Key key : keysToArray(keySet().toArray(new Key[0]))) {
                if (key.notUsed()) {
                    remove(key);
                }
            }
        } else {
            if(future != null){
                future.cancel(true);
                future = null;
            }
        }
    }


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