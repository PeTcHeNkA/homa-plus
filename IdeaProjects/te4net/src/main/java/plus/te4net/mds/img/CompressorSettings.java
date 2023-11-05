package plus.te4net.mds.img;

import plus.te4net.mds.fastreg.FrMap;
import plus.tson.TsonBool;
import plus.tson.TsonFloat;
import plus.tson.TsonMap;
import plus.tson.TsonObj;


public class CompressorSettings extends FrMap<CompressorSettings.ImageInfo> {
    static final SNode def = new SNode(false, 0.67f,1);

    public void addSettings(String math, TsonMap map){
        add(math, ofMap(map));
    }


    public void addSettings(String math, ImageInfo info){
        add(math, info);
    }


    public static ImageInfo ofMap(TsonMap map){
        return new ImageInfo(
                nodeOfMap(map,"P100", 1f),
                nodeOfMap(map,"P75", 0.75f),
                nodeOfMap(map,"P50", 0.5f)
        );
    }


    private static SNode nodeOfMap(TsonMap map0, String key, float defSize){
        TsonObj mp0 = map0.get(key);
        TsonMap map;
        if(mp0==null){
            map = new TsonMap();
        } else {
            map = mp0.getMap();
        }
        return new SNode(
                !map.getOrDefault("lossy", new TsonBool(false)).getBool(),
                map.getOrDefault("quality", new TsonFloat(0.67f)).getFloat(),
                map.getOrDefault("size", new TsonFloat(defSize)).getFloat()
        );
    }


    public static final class ImageInfo {
        public final SNode P100;
        public final SNode P75;
        public final SNode P50;

        public ImageInfo(SNode p100, SNode p75, SNode p50) {
            P100 = p100;
            P75 = p75;
            P50 = p50;
        }


        public SNode ofSize(ImgSize size){
            if(ImgSize.P100==size)return P100;
            if(ImgSize.P75==size)return P75;
            if(ImgSize.P50==size)return P50;
            return new SNode(false, (byte) 64, (byte) (size.calc(255)-128));
        }
    }


    static final class SNode{
        public final boolean lossLess;
        public final byte quality;
        public final byte size;

        public SNode(boolean lossLess, float quality, float size){
            this.lossLess = lossLess;
            this.quality = (byte) (quality*100-128);
            this.size = (byte) (size*100-128);
        }


        public int calc(int size){
            float fsize = ((float) (((int)(this.size))+128))/100f;
            return (int) (size*fsize);
        }


        public float quality(){
            return ((float) (quality+128))/100f;
        }
    }
}
