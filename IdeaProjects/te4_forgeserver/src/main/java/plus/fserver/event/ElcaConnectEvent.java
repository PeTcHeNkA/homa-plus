package plus.fserver.event;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import plus.fserver.ElcaConfig;
import java.net.SocketAddress;


@Cancelable
public class ElcaConnectEvent extends Event {
    private final PlayerList playerList;
    private final SocketAddress address;
    private GameProfile profile;
    private String rejectMsg;
    private Status status;
    private Object data;

    public ElcaConnectEvent(PlayerList playerList, SocketAddress address, GameProfile profile) {
        this.playerList = playerList;
        this.address = address;
        this.profile = profile;
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


    public void setProfile(GameProfile profile){
        this.profile = profile;
    }


    public GameProfile getProfile() {
        return profile;
    }


    public SocketAddress getAddress() {
        return address;
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


    public PlayerList getPlayerList() {
        return playerList;
    }


    public enum Status {
        TRY_ACCEPT, SUCCESS_AUTH,
        REJECT_BANNED, REJECT_BANNED_IP, REJECT_NON_WHITELIST, REJECT_SERVER_FULL,
        REJECT_NO_AUTH, REJECT_NO_AUTH_SERVER, REJECT_ERROR, REJECT_TIME_OUT,
    }
}