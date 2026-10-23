package plus.quest.script.quest.listener;

import org.bukkit.command.CommandSender;
import plus.Main;
import plus.quest.AbstractQuestSession;
import plus.util.cmd.CommandArgs;
import plus.util.cmd.SubCommand;
import plus.util.cmd.YandeReCommand;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class CommandListener {

    private static YandeReCommand cmd;
    private static TSubCommand scmd;


    public static void define() {
        if (cmd == null) {
            cmd = new YandeReCommand("quests", "q", Collections.emptyList());
            scmd = new TSubCommand("int");
            cmd.addSubCommand(scmd);
        }
    }


    public static void addCmdListener(String name, String command, CmdListener listener){
        HashMap<String, CmdListener> listeners = scmd.cache.computeIfAbsent(name, k -> new HashMap<>());
        listeners.put(command, listener);
    }


    public static void clearFor(String name){
        scmd.cache.remove(name);
    }
}


//HashMap<Integer, RgnListener>
class TSubCommand extends SubCommand {
    protected final HashMap<String, HashMap<String, CmdListener>> cache = new HashMap<>();

    protected TSubCommand(String name) {
        super(name);
    }

    @Override
    public SubCommand[] injectSubCommands() {
        return new SubCommand[0];
    }

    @Override
    public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
        return null;
    }


    @Override
    public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
        HashMap<String, CmdListener> listeners = cache.get(commandSender.getName());
        if(listeners==null)return;
        CmdListener listener = listeners.get(string);

        if(listener != null){
            AbstractQuestSession session = Main.floader.getValkiria(commandSender.getName()).getQuestSession();
            if(listener.onCommand(session, args)){
                listener.unload(session);
                //listeners.remove(commandSender.getName());
            }
        }
    }
}