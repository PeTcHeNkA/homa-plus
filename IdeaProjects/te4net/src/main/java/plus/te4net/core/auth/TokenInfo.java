package plus.te4net.core.auth;

import java.util.Arrays;


public class TokenInfo {
    public final String token;
    private final byte[] data;

    public TokenInfo(String token, byte[] data) {
        this.token = token;
        this.data = data;
    }

    @Override
    public final boolean equals(Object o) {
        if (o instanceof TokenInfo) {
            TokenInfo tokenInfo = (TokenInfo) o;
            return token.equals(tokenInfo.token) && Arrays.equals(data, tokenInfo.data);
        } else return false;
    }


    @Override
    public final int hashCode() {
        return token.hashCode();
    }


    long getUsages() {
        return -1;
    }


    public void use() {
    }
}