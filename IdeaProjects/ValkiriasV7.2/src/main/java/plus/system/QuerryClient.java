package plus.system;

import plus.util.sql.YandereDBServer;
import java.io.*;
import java.net.Socket;


public class QuerryClient{
    private final String host;
    private final int port;
    private BufferedReader in;
    private BufferedWriter out;

    public QuerryClient(String host, int port) {
        if (host == null) {
            host = "localhost";
        }

        try {
            this.createConnection(host, port);
        } catch (Exception var4) {
            System.out.println("Not connected.");
        }

        this.host = host;
        this.port = port;
    }

    private void createConnection(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }



    public void redirectPlayer(String player, String server) {
        String result = YandereDBServer.listToString("RED", player, server) + "\n";
        try {
            this.out.write(result);
            this.out.flush();
        } catch (IOException | NullPointerException e) {
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
