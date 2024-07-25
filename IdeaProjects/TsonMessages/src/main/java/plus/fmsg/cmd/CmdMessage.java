package plus.fmsg.cmd;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import plus.fserver.api.TextComponentAuto;


public class CmdMessage {
    private final String[] args;
    private final ICommandSender sender;
    private final MinecraftServer server;
    private int cursor;

    public CmdMessage(MinecraftServer server, ICommandSender sender, String[] args){
        this.server = server;
        this.sender = sender;
        this.args = args;
    }


    public boolean fromPlayer(){
        return sender instanceof EntityPlayerMP;
    }


    public String[] getRawArgs() {
        return args;
    }


    public String getName(){
        return sender.getName();
    }


    public ICommandSender getSender() {
        return sender;
    }


    public MinecraftServer getServer() {
        return server;
    }


    public int getCursor() {
        return cursor;
    }


    public void setCursor(int cursor) {
        this.cursor = cursor;
    }


    public int availableArgs(){
        return args.length - cursor;
    }


    public int addCursor() {
        return this.cursor += 1;
    }


    public String curArg() {
        if(cursor >= args.length) return null;
        return args[cursor];
    }


    public String getArg(int index) {
        index += cursor;
        if(index >= args.length) return null;
        return args[index];
    }


    public void reply(String msg){
        sender.sendMessage(new TextComponentAuto(msg));
    }


    public void reply(ITextComponent msg){
        sender.sendMessage(msg);
    }


    public EntityPlayerMP getPlayer() {
        return ((EntityPlayerMP) sender);
    }
}