package plus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import plus.Motions.CatScene;
import plus.Motions.DialogScene;
import java.util.ArrayList;
import java.util.List;


public class ControlCommands implements CommandExecutor, TabCompleter {

    final Camera c;

    public ControlCommands(Camera c){
        this.c = c;
        subCommands.add("run");
        subCommands.add("s_run");
        subCommands.add("n_run");
        subCommands.add("d_run");
        subCommands.add("reload");
        subCommands2.add("messages");
        subCommands2.add("scenes");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command,String string, String[] args) {
        if(args[0].equals("run")) {
            if (commandSender instanceof Player player) {
                CatScene.runCatScene(args[1], player);
            } else {
                System.out.println("This command only to player!");
            }
            return true;
        }

        if(args[0].equals("d_run")) {
            if (commandSender instanceof Player player) {
                DialogScene.runDialogScene(args[1], player);
            } else {
                System.out.println("This command only to player!");
            }
            return true;
        }


        if(args[0].equals("s_run")) {
            if (commandSender instanceof Player player) {
                CatScene.getCatScene(args[1]).safeRun(player);
            } else {
                System.out.println("This command only to player!");
            }
            return true;
        }

        if(args[0].equals("n_run")) {
            if (commandSender instanceof Player player) {
                List<Player> players = new ArrayList<>();
                Location startLoc = player.getLocation();

                double distance = 7.0f;
                if(args.length >2) {
                    distance = Double.parseDouble(args[2]);
                }

                for(Player p:Bukkit.getOnlinePlayers()){
                    if(p.getLocation().getWorld().equals(startLoc.getWorld())){
                        if(p.getLocation().distance(startLoc)<distance){
                            players.add(p);
                        }
                    }
                }
                CatScene.getCatScene(args[1]).run(players);
            } else {
                System.out.println("This command only to player!");
            }
            return true;
        }

        if(args[0].equals("reload") || args[0].equals("rel")){
            if(args.length>1){
                if(args[1].equals("messages") || args[1].equals("msg")){
                    Messages.Reload();
                    commandSender.sendMessage("Messages reloaded.");
                }
                if(args[1].equals("scenes") || args[1].equals("scn")){
                    CatScene.reloadAllCatScenes();
                    DialogScene.reloadAllCatScenes();
                    commandSender.sendMessage("CatScenes reloaded.");
                }
            } else {
                CatScene.reloadAllCatScenes();
                Messages.Reload();
                commandSender.sendMessage("Reloaded all.");
            }
            return true;
        }

        return true;
    }

    private final List<String> subCommands = new ArrayList<>();
    private final List<String> subCommands2 = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String string, String[] strings) {
        if(strings.length < 2){
            return subCommands;
        }
        if(strings[0].equals("run") && strings.length == 2){
            return CatScene.getListNames();
        }
        if(strings[0].equals("d_run") && strings.length == 2){
            return DialogScene.getListNames();
        }
        if(strings[0].equals("reload") || strings[0].equals("rel")){
            if(strings.length == 2){
                return subCommands2;
            }
        }
        return null;
    }
}
