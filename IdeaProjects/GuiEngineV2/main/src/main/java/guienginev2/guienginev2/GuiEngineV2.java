package guienginev2.guienginev2;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class GuiEngineV2 extends JavaPlugin {

    public static String _Name_ = "None";

    @Override
    public void onEnable() {

        //String newtitle = getConfig().getString("gui_name");
        //if(newtitle != null){
        //    _Name_ = newtitle;
        //}

        Bukkit.getPluginManager().registerEvents(new GuiInteract(_Name_), this);

        getCommand("helpgui").setExecutor(this);

        this.getConfig().set

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public static String getGuiName(){
        return _Name_;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            Player player = (Player) sender;
            player.sendMessage("Inventory is open!");
            GuiInteract.OpenGui(player);

        } catch (Exception err) {
            sender.sendMessage("This command only from players");
        }


        return true;
    }

}
