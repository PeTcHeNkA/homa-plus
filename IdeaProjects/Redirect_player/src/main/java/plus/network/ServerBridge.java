package plus.network;

import plus.utl.FastRandom;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerBridge {

    private final int port;
    private boolean Enabled = true;
    protected final QuerryExucator querryExucator;
    private static final List<Connection> connections = new ArrayList<>();


    public ServerBridge(QuerryExucator exucator, int port) {
        this.port = port;
        querryExucator = exucator;
        startYandeReBDServer();
    }


    private void startYandeReBDServer(){
        Runnable task = () -> {
            System.out.println("Started!");
            ServerSocket server = null;
            try {
                server = new ServerSocket(port);

                try {
                    while (Enabled) {
                        Socket socket = server.accept();
                        try {
                            if(Enabled)connections.add(new Connection(socket, this));
                        } catch (IOException e) {
                            socket.close();
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }


    protected final void disconnect(Connection c){
        connections.remove(c);
    }

    public final void Stop(){
        Enabled = false;
        disconnectAll();
    }


    public final void disconnectAll(){
        for(Connection c:connections){
            c.disconnect();
        }
    }

    protected static String listToString(List<String> args){
        if(args==null)return ";null";
        String sep = String.valueOf(FastRandom.r.nextChar());
        for(String s:args){
            if(s.contains(sep))return listToString(args);
        }
        return sep+String.join(sep, args);
    }
    protected static String listToString(String... args){
        if(args==null)return ";null";
        String sep = String.valueOf(FastRandom.r.nextChar());
        for(String s:args){
            if(s.contains(sep))return listToString(args);
        }
        return sep+String.join(sep, args);
    }

    protected String perform(String rawQuery){
        List<String> qrValue = stringToList(rawQuery);
        CommandExucator e = querryExucator.getExucator(qrValue.remove(0));
        if(e==null)return null;
        else return e.exucate(qrValue);
    }
    protected static List<String> stringToList(String rawQuery){
        String sep = String.valueOf(rawQuery.charAt(0));
        rawQuery = rawQuery.substring(1);
        return new ArrayList<>(Arrays.asList(rawQuery.split(sep)));
    }

}

class Connection{
    private boolean Enabled = true;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final ServerBridge server;

    public Connection(Socket socket, ServerBridge server) throws IOException{
        this.server = server;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        Runnable task = () -> {
            try {
                int i = 0;
                while (Enabled) {
                    String quest = in.readLine();
                    if(quest==null){
                        i++;
                        if(i==5)break;
                        Thread.sleep(1000);
                        continue;
                    }
                    String result = server.perform(quest);
                    if(result != null){
                        out.write(result);
                        out.flush();
                    }
                }
            } catch (IOException | InterruptedException e) {
                try {
                    in.close();
                    out.close();
                } catch (IOException ignored) {}
            }
            server.disconnect(this);
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    protected void disconnect(){
        Enabled = false;
        try {
            in.close();
            out.close();
        } catch (Exception ignored){}
        server.disconnect(this);
    }
}