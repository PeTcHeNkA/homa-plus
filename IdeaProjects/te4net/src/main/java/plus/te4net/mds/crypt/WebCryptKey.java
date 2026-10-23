package plus.te4net.mds.crypt;

import plus.te4net.core.api.PublicUsage;
import java.nio.charset.StandardCharsets;
import static plus.te4net.mds.cap4a.Cap4a.fillRandomId;


@PublicUsage
public class WebCryptKey extends CryptKey{
    public WebCryptKey(long time, short usages) {
        super(time, usages);
    }

    public static WebCryptKey newKey(long time, short usages){
        WebCryptKey result = new WebCryptKey(time, usages);
        fillRandomId(result.key);
        return result;
    }


    @Override
    public String toString() {
        return new String(key, StandardCharsets.UTF_8);
    }
}