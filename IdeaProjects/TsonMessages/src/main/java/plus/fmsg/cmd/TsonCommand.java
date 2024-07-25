package plus.fmsg.cmd;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import plus.fmsg.cmd.proc.CmdMsgProcessor;
import plus.fmsg.cmd.proc.CompleteCmdProc;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


public class TsonCommand implements ICommand {
    private String name;
    private String usage;
    private Consumer<CmdMessage> task;
    private Function<CmdMessage, List<String>> completer;
    private int permLvl;
    private List<String> aliases;

    public TsonCommand(TsonMap map){
        this.name = map.getStr("name");
        this.usage = map.getOrDefaultStr("usage", "/"+name);
        this.task = CmdMsgProcessor.ofTson(map.get("task"));
        this.completer = CompleteCmdProc.ofTson(map.get("completer"));
        this.permLvl = map.getOrDefaultInt("permLvl", 4);
        this.aliases = readAliases(map.get("aliases"));
    }


    private static List<String> readAliases(TsonObj obj){
        if(obj == null)return Collections.emptyList();
        if(obj.isList()){
            TsonList list = obj.getList();
            ArrayList<String> arr = new ArrayList<>(list.size());
            for(int i = 0; i < list.size(); i++){
                arr.add(list.getStr(i));
            }
            return arr;
        }
        return Collections.emptyList();
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public String getUsage(ICommandSender sender) {
        return usage;
    }


    @Override
    public List<String> getAliases() {
        return aliases;
    }


    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        task.accept(new CmdMessage(server, sender, args));
    }


    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(permLvl, name);
    }


    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos blockPos) {
        return completer.apply(new CmdMessage(server, sender, args));
    }


    @Override
    public boolean isUsernameIndex(String[] strings, int i) {
        return false;
    }


    @Override
    public int compareTo(ICommand o) {
        return name.compareTo(o.getName());
    }
}