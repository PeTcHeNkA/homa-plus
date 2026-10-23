package net.minecraft.server.network;


public class PubNetState {
    public static final NetHandlerLoginServer.LoginState AUTHENTICATING
            = NetHandlerLoginServer.LoginState.AUTHENTICATING;

    public static final NetHandlerLoginServer.LoginState KEY
            = NetHandlerLoginServer.LoginState.KEY;

    public static final NetHandlerLoginServer.LoginState READY_TO_ACCEPT
            = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
}
