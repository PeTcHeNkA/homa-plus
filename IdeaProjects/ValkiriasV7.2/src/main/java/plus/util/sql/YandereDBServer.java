package plus.util.sql;

import plus.util.FastRandom;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;;
import java.util.Arrays;
import java.util.List;


public class YandereDBServer {

    private final int port;
    private boolean Enabled = true;
    protected final QuerryExucator querryExucator;
    private static final List<Connection> connections = new ArrayList<>();


    public YandereDBServer(QuerryExucator exucator, int port) {
        this.port = port;
        querryExucator = exucator;
        startYandeReBDServer();
    }


    private void startYandeReBDServer(){
        Runnable task = () -> {
            System.out.println("Started!");
            ServerSocket server;
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

    private static final String sep = String.valueOf((char) 1024);
    public static String listToString(List<String> args){
        if(args==null)return ";null";
//        // sep = String.valueOf(FastRandom.r.nextChar());
//        for(String s:args){
//            if(s.contains(sep))return listToString(args);
//        }
        return sep+String.join(sep, args);
    }
    public static String listToString(String... args){
        if(args==null)return ";null";
        String sep = String.valueOf(FastRandom.r.nextChar());
        for(String s:args){
            if(s.contains(sep))return listToString(args);
        }
        return sep+String.join(sep, args);
    }

    protected String perform(String rawQuery){
        List<String> qrValue = stringToList(rawQuery);
//        System.out.println(qrValue.get(0));
        CommandExecutor e = querryExucator.getExucator(qrValue.remove(0));
//        System.out.println(e==null);
        if(e==null)return null;
        else return e.execute(qrValue);
    }

    public static List<String> stringToList(String rawQuery){
        String sep = String.valueOf(rawQuery.charAt(0));
        rawQuery = rawQuery.substring(1);
        if(rawQuery.equals("null"))return null;
        return new ArrayList<>(Arrays.asList(rawQuery.split(sep)));
    }
}

class Connection{
    private boolean Enabled = true;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final YandereDBServer server;

    public Connection(Socket socket, YandereDBServer server) throws IOException{
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
//                    System.out.println(quest);
                    String result = server.perform(quest);
                    if(result != null){
                        out.write(result+"\n");
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