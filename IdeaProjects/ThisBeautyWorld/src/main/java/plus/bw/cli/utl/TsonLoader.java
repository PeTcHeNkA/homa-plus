package plus.bw.cli.utl;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import plus.tson.TsonFile;
import plus.tson.TsonMap;
import sun.misc.IOUtils;
import java.io.IOException;
import java.io.InputStream;


public class TsonLoader implements AssetLoader {
    public static class Key extends AssetKey<TsonMap>{
        public Key(String name){
            super(name);
        }
    }

    public static Key key(String name){
        return new Key(name);
    }

    @Override
    public Object load(AssetInfo assetInfo) throws IOException {
        InputStream stream = assetInfo.openStream();
        byte[] bytes = IOUtils.readAllBytes(stream);
        return new TsonMap(TsonFile.remBadChars(new String(bytes)));
    }
}