package plus.te4net.core.auth;

public final class TokenIKey extends TokenInfo {
    private long lastUsage = System.currentTimeMillis();

    public TokenIKey(String token, byte[] data) {
        super(token, data);
    }

    long getUsages() {
        return lastUsage;
    }


    public void use() {
        lastUsage = System.currentTimeMillis();
    }
}