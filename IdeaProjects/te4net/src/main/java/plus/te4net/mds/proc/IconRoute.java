package plus.te4net.mds.proc;

import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.node.HEWorker;
import plus.te4net.mds.img.Compressor;
import plus.te4net.mds.img.ImgSize;
import plus.te4net.mds.img.NSimpleImgProcessor;
import plus.tson.TsonFile;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;


public class IconRoute implements IRequestProcessor {
    private final byte[] p100, p75;
    private final int hs100, hs75;

    public IconRoute(HEWorker worker, File file){
        AtomicReference<byte[]> ref = new AtomicReference<>();

        p100 = getOrPress(worker, file, ref, ImgSize.P100);
        p75  = getOrPress(worker, file, ref, ImgSize.P75 );

        hs100 = Arrays.hashCode(p100);
        hs75  = Arrays.hashCode(p75);
    }


    private byte[] getOrPress(HEWorker worker, File file, AtomicReference<byte[]> orig, ImgSize size){
        byte[] res = worker.getImgLib().getRaw(worker.getMainDomain(), size.suffix(), "favicon.ico");
        if(res == null){
            readIfNeed(orig, file);
            res = Compressor.press(worker.getCompressorSettings(), "favicon.ico", orig.get(), size);
            worker.getImgLib().saveRaw(res, worker.getMainDomain(), size.suffix(), "favicon.ico");
        }
        return res;
    }


    private void readIfNeed(AtomicReference<byte[]> orig, File file){
        if(orig.get() == null){
            orig.set(TsonFile.readFile(file));
        }
    }


    @Override
    public void handle(RequestSession session) {
        ImgSize size = NSimpleImgProcessor.getOptimalSize(session);

        if(size == ImgSize.P100 || size == ImgSize.ORIG){
            NSimpleImgProcessor.handleImg(session, p100, hs100);
        } else {
            NSimpleImgProcessor.handleImg(session, p75, hs75);
        }
    }
}