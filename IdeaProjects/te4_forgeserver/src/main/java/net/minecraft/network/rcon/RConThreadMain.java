package net.minecraft.network.rcon;

import net.minecraftforge.fml.relauncher.*;
import com.google.common.collect.*;
import java.util.*;
import java.io.*;
import java.net.*;


@SideOnly(Side.SERVER)
public class RConThreadMain extends RConThreadBase {
    private int rconPort;
    private final int serverPort;
    private String hostname;
    private ServerSocket serverSocket;
    private final String rconPassword;
    private Map<SocketAddress, RConThreadClient> clientThreads;
    private final int maxConnections;
    private final int acceptRate;
    private long lastAccept = System.currentTimeMillis();

    public RConThreadMain(final IServer server) {
        super(server, "RCON Listener");
        this.rconPort = server.getIntProperty("rcon.port", 0);
        this.rconPassword = server.getStringProperty("rcon.password", "");
        this.maxConnections = server.getIntProperty("rcon.max_connections", 3);
        this.acceptRate     = server.getIntProperty("rcon.accept_rate", 3000);
        this.hostname = server.getHostname();
        this.serverPort = server.getPort();
        if (0 == this.rconPort) {
            this.rconPort = this.serverPort + 10;
            this.logInfo("Setting default rcon port to " + this.rconPort);
            server.setProperty("rcon.port", (Object)this.rconPort);
            if (this.rconPassword.isEmpty()) {
                server.setProperty("rcon.password", (Object)"");
            }
            server.saveProperties();
        }
        if (this.hostname.isEmpty()) {
            this.hostname = "0.0.0.0";
        }
        this.initClientThreadList();
        this.serverSocket = null;
    }


    private void initClientThreadList() {
        this.clientThreads = Maps.newHashMap();
    }


    private void cleanClientThreadsMap() {
        final Iterator<Map.Entry<SocketAddress, RConThreadClient>> itr = this.clientThreads.entrySet().iterator();
        while (itr.hasNext()) {
            final Map.Entry<SocketAddress, RConThreadClient> entry = itr.next();
            if (!entry.getValue().isRunning()) {
                itr.remove();
            }
        }
    }


    private boolean canAccept(){
        return clientThreads.size() < maxConnections;
    }


    private boolean checkRate(){
        return lastAccept < System.currentTimeMillis();
    }

    private void updateRate(){
        lastAccept = System.currentTimeMillis() + acceptRate;
    }


    @Override
    public void run() {
        this.logInfo("RCON running on " + this.hostname + ":" + this.rconPort);
        try {
            while (this.running) {
                try {
                    final Socket sock = this.serverSocket.accept();
                    if(!checkRate()){
                        sock.close();
                        return;
                    }
                    sock.setSoTimeout(500);
                    if(!canAccept()) {
                        this.cleanClientThreadsMap();
                    }
                    if(!canAccept()) {
                        sock.close();
                        return;
                    }
                    updateRate();
                    final RConThreadClient cli = new RConThreadClient(this.server, sock);
                    cli.startThread();
                    this.clientThreads.put(sock.getRemoteSocketAddress(), cli);

                    this.cleanClientThreadsMap();
                } catch (SocketTimeoutException e) {
                    this.cleanClientThreadsMap();
                }
                catch (Exception e) {
                    if (!this.running) {
                        continue;
                    }
                    this.logInfo("IO: " + e.getMessage());
                }
            }
        } finally {
            this.closeServerSocket(this.serverSocket);
        }
    }


    @Override
    public void startThread() {
        if (this.rconPassword.isEmpty()) {
            this.logWarning("No rcon password set in '" + this.server.getSettingsFilename() + "', rcon disabled!");
            return;
        }
        if (0 >= this.rconPort || 65535 < this.rconPort) {
            this.logWarning("Invalid rcon port " + this.rconPort + " found in '" + this.server.getSettingsFilename() + "', rcon disabled!");
            return;
        }
        if (this.running) {
            return;
        }
        try {
            (this.serverSocket = new ServerSocket(this.rconPort, 0, InetAddress.getByName(this.hostname))).setSoTimeout(500);
            super.startThread();
        } catch (IOException e) {
            this.logWarning("Unable to initialise rcon on " + this.hostname + ":" + this.rconPort + " : " + e.getMessage());
        }
    }
}
