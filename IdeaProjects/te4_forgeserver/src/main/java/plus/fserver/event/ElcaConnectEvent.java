package plus.fserver.event;

import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import plus.fserver.ElcaConfig;


@Cancelable
public class ElcaConnectEvent extends NetHandlerLoginServer.ConnectEvent {
    private String rejectMsg;
    private Status status;
    private Object data;

    public ElcaConnectEvent(NetHandlerLoginServer loginServer) {
        super(loginServer);
    }


    public void reset(){
        rejectMsg = null;
    }


    public Status getStatus() {
        return status;
    }


    public Object getData() {
        return data;
    }


    public void setData(Object data) {
        this.data = data;
    }


    public void setStatus(Status status) {
        setCanceled(false);
        this.status = status;
    }


    public void reject(){
        reject(null);
    }


    public void reject(String msg){
        rejectMsg = msg == null?ElcaConfig.DEFAULT_REJECT_MSG:msg;
    }


    public boolean canJoin(){
        return rejectMsg == null;
    }


    public void accept(){
        setCanceled(true);
        rejectMsg = null;
    }


    public String getMessage(){
        return rejectMsg;
    }


    public enum Status {
        TRY_ACCEPT, SUCCESS_AUTH,
        REJECT_BANNED, REJECT_BANNED_IP, REJECT_NON_WHITELIST, REJECT_SERVER_FULL,
        REJECT_NO_AUTH, REJECT_NO_AUTH_SERVER, REJECT_ERROR, REJECT_TIME_OUT,
    }
}