import plus.tcord.TQueue;
import plus.tcord.TServer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class DebugTCord {
    public static void main(String[] args) throws IOException {
        String port = "23700";
        String lang = "eng:ru";
//        String port = args[0]; //"23700";
//        String lang = args[1]; //"eng:ru";

        TServer server = new TServer(Integer.parseInt(port));
        System.out.println("Starting server ...");
        server.setHello(lang, "ready");

        server.setQueue(new TQueue() {
            final ArrayList<String> data = new ArrayList<>(Arrays.asList(new String[]{
                    "Centaureissi, do you read me? Tap your earpiece if you do."
            }));
            @Override
            public String next() {
                synchronized (data){
                    if(data.isEmpty())return null;
                    return data.removeLast();
                }
            }
            @Override
            public boolean isCompleted() {
                return data.isEmpty();
            }
            @Override
            public void onError(String msg) {
                System.out.println("ERROR TRANSLATE: "+msg);
            }
            @Override
            public void onSuccess(String msg, String result) {
                System.out.println(msg +"->"+ result);
                server.stop();
            }
        });
        server.start(true);
    }
}