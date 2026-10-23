package plus.system.teleporter;

import plus.util.sql.YandereDBServer;
import java.io.*;
import java.net.Socket;


public class TeleporterInExternalServer implements TeleportSender {
    private final String host;
    private final int port;
    private BufferedReader in;
    private BufferedWriter out;


    public TeleporterInExternalServer(int port) {
        host = "localhost";
        try {
            this.createConnection(host, port);
        } catch (Exception var4) {
            System.out.println("Not connected.");
        }
        this.port = port;
    }


    private void createConnection(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }


    @Override
    public void teleportPlayer(String player, String loc){
        String result = YandereDBServer.listToString("TP", player, loc) + "\n";
        try {
            this.out.write(result);
            this.out.flush();
        } catch (IOException var7) {
            try {
                this.createConnection(this.host, this.port);
                this.out.write(result);
                this.out.flush();
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }
    }
}
