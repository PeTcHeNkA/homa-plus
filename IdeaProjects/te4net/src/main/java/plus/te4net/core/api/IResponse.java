package plus.te4net.core.api;

import io.netty.handler.codec.http.HttpResponseStatus;
import java.nio.charset.StandardCharsets;


@PublicUsage
public interface IResponse {
    String getRHeader(CharSequence name);


    byte[] encodeBody();


    default String encodeBodyAsStr(){
        return new String(encodeBody(), StandardCharsets.UTF_8);
    }


    HttpResponseStatus status();
}