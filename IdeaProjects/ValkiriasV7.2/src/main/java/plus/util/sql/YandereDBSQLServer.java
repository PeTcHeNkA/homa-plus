package plus.util.sql;

import java.util.HashMap;
import java.util.List;

public class YandereDBSQLServer extends plus.util.sql.SqlSector implements QuerryExucator{

    protected final YandereDBServer yandereDBServer;
    protected final HashMap<String, CommandExecutor> commands = new HashMap<>();

    public YandereDBSQLServer(String sqlName, String table, List<String> collums, int port) {
        super(sqlName, table, collums);
        initCommands();
        yandereDBServer = new YandereDBServer(this, port);

//        commands.keySet().toString();
        //DataFloader f = new YandereDBSQLServer(sqlName, table, collums, port);
    }

    protected void initCommands(){
        commands.put("PUT", args -> {
            putData(args.remove(0), args);
            return null;
        });
        commands.put("GET", args -> YandereDBServer.listToString(getData(args.get(0))));
    }


    public void Stop(){
        yandereDBServer.Stop();
    }

    @Override
    public CommandExecutor getExucator(String command) {
        return commands.get(command);
    }
}
