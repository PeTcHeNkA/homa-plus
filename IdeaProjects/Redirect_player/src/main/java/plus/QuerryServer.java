package plus;

import com.velocitypowered.api.proxy.Player;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.elytrium.limboapi.api.player.LimboPlayer;
import plus.network.CommandExucator;
import plus.network.QuerryExucator;
import plus.network.ServerBridge;
import plus.limbo.LimboListener;
import plus.limbo.SchemWorld;
import java.util.HashMap;


public class QuerryServer implements QuerryExucator {

    private final HashMap<String, CommandExucator> commands = new HashMap<>();
    private final ServerBridge serverBridge;
    private final SchemWorld server;



    public QuerryServer(int port){
        server = new SchemWorld("re_hub");
        serverBridge = new ServerBridge(this, port);
        difineCommands();
    }


    private void difineCommands(){
        commands.put("RED", args -> {
            redirectPlayer(args.get(0), args.get(1));
            return null;
        });
    }


    @Override
    public CommandExucator getExucator(String command) {
        return commands.get(command);
    }


    public void redirectPlayer(String playerName, String server){
        //ReadTimeoutHandler
        System.out.printf("| Redirecting player [%s] to server [%s]%n", playerName, server);
        LimboPlayer limPlayer = LimboListener.removeLimboPlayer(playerName);
        if(limPlayer != null){
            if(!server.equals(this.server.getName()))
                limPlayer.disconnect(Main.getServer().getServer(server).get());
        } else {
            Player p = Main.getServer().getPlayer(playerName).get();
            if(server.equals(this.server.getName())){
                this.server.spawnPlayer(p);
            } else {
                p.createConnectionRequest(Main.getServer().getServer(server).get()).connect();
            }
        }
    }
}