package plus.fserver.api;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class ServerCommand extends CommandBase {
    public static ArrayList<ServerCommand> commands = new ArrayList<>();
    protected static final List<String> empty = new AbstractList<String>() {
        @Override
        public String get(int index) {
            return null;
        }
        @Override
        public int size() {
            return 0;
        }
    };
    private final String name;
    private final String usage;
    private final int permLvl;

    public ServerCommand(String name, String usage, int permLvl) {
        this.name = name;
        this.usage = usage;
        this.permLvl = permLvl;
    }


    public final ServerCommand register(){
        commands.add(this);
        return this;
    }


    public static void reply(ICommandSender sender, String msg){
        sender.sendMessage(new TextComponentAuto(msg.replace("&", "§")));
    }


    public static void reply(ICommandSender sender, String format, Object... args){
        sender.sendMessage(new TextComponentAuto((String.format(format, args)).replace("&", "§")));
    }


    public void onError(ICommandSender sender, Throwable e){
        onError(sender, getName(), e);
    }


    public static void onError(ICommandSender sender, String cmdName, Throwable e){
        reply(sender, "&4Critical server error on executing [%s]", cmdName);

        if(sender.canUseCommand(4, cmdName)){
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream stream = new PrintStream(out);
            stream.print("§4");
            e.printStackTrace(stream);
            stream.close();
            reply(sender, out.toString());
        } else {
            reply(sender, "&4Contact with administrator to get more details");
            e.printStackTrace();
        }
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
        return Collections.emptyList();
    }


    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(permLvl, name);
    }


    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return Collections.emptyList();
    }


    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }


    @Override
    public final int compareTo(ICommand o) {
        return name.compareTo(o.getName());
    }
}
