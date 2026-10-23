package plus.limbo;


import net.elytrium.limboapi.api.LimboSessionHandler;

public class EmptyListener implements LimboSessionHandler {
    public static final LimboSessionHandler I = new EmptyListener();
}
