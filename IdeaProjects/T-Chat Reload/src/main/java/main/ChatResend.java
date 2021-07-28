package main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatResend implements CommandExecutor {

    private String WrongNickName;
    private String NullMassage;

    public ChatResend(String wrongNickName, String nullMassage){
        WrongNickName = wrongNickName;
        NullMassage = nullMassage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            System.out.println("This command only to Player!");
            return true;
        }

        ChatUser user = Floader.getUser(sender.getName());

        if(user.getLastMessageTarget() == null){
            sender.sendMessage(WrongNickName);
            return true;
        }
        if(args.length == 0){
            sender.sendMessage(NullMassage);
            return true;
        }

        String[] trg = {user.getLastMessageTarget()};

        return ChatHundler.privateMessage(sender, command, label, addargs(trg, args));
    }
    private String[] addargs(String[] a, String[] b) {
        String[] r = new String[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }
}
