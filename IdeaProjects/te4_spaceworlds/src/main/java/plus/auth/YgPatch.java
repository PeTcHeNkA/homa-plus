package plus.auth;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.response.Response;
import java.net.URL;

public interface YgPatch {
    <T extends Response> T makeRequestPub(final URL url, final
    Object input, final Class<T> classOfT) throws AuthenticationException;
}
