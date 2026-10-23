package plus.system;


import plus.util.sql.DataFloader;
import plus.util.sql.Queries;
import plus.util.sql.YandereDBServer;
import java.io.*;
import java.net.Socket;
import java.util.List;


public class SqlClient1 implements DataFloader {
    private final String host;
    private final int port;
    private BufferedReader in;
    private BufferedWriter out;

    public SqlClient1(String host, int port) {
        if(host==null)host = "localhost";
        try {
            createConnection(host, port);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(String.format("Not connection to sql in [%s:%s].", host, port));
        }
        this.host = host;
        this.port = port;
    }
    private void createConnection(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Override
    public List<String> getData(String quest) {
        try {
            out.write(YandereDBServer.listToString(Queries.GET, quest)+"\n");
            out.flush();
            String result = in.readLine();
            return YandereDBServer.stringToList(result);
        } catch (IOException | NullPointerException e){
            try {
                createConnection(host, port);
                out.write(YandereDBServer.listToString(Queries.GET, quest)+"\n");
                out.flush();
                String result = in.readLine();
                return YandereDBServer.stringToList(result);
            } catch (Exception ignored){return null;}
        }
    }

    @Override
    public void putData(String user, List<String> labels){
        labels.add(0, user);
        labels.add(0, Queries.PUT);
        String result = YandereDBServer.listToString(labels)+"\n";
        try {
            out.write(result);
            out.flush();
        } catch (IOException e){
            try {
                createConnection(host, port);
                out.write(result);
                out.flush();
            } catch (Exception err){
                err.printStackTrace();
            }
        }
    }
}