package plus.system.teleporter;

import java.util.HashMap;

public class TeleportClient implements TeleportSender{

    protected final TeleportSender Default;
    protected final HashMap<String, TeleportSender> clients = new HashMap<>();
    public TeleportClient(String server, int port){
        Default = new TeleporterInExternalServer(port);
    }

    public void addTeleportConnection(String name, int port){
        clients.put(name, new TeleporterInExternalServer(port));
    }

    @Override
    public void teleportPlayer(String player, String loc) {
        Default.teleportPlayer(player, loc);
    }

    public void teleportPlayer(String server, String player, String loc) {
        clients.getOrDefault(server, Default).teleportPlayer(player, loc);
    }
}
