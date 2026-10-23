package plus;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import plus.utls.LootBox.LootBox;
import plus.utls.Random.PsevdoRandom;

public class Commands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        if(args.length==0 ||args[0].contains("help")){
            p.sendMessage(String.join("\n", new String[]{
                    "| /te4hno edit   <- open lootBox editor",
                    "| /te4hno export <- export editor value to config",
                    "| /te4hno import <- load lootbox as config",
                    "| /te4hno open   <- emulate open lootbox",
                    "| /te4hno spawn  <- spawn new lootBox",
                    "| /te4hno reload <- reload this plugin",
                    "| /te4hno test   <- print in console hash main hand"
            }));
            return true;
        }
        if(args[0].contains("edit")){
            if(main.inv.edit==null)main.inv.edit = Bukkit.createInventory(null, 54);
            p.openInventory(main.inv.edit);
        }
        if(args[0].contains("export")){
            main.inv.Export();
        }
        if(args[0].contains("import")){
            main.inv.Import();
        }
        if(args[0].contains("test")){
            System.out.println("Hash of item in main hand");
            System.out.println("=========================");
            System.out.println(main.coder.codeItem(p.getInventory().getItemInMainHand()));
            System.out.println("=========================");
        }
        if(args[0].contains("random")){
            main.r = new PsevdoRandom();
        }
        if(args[0].contains("spawn")){
            main.m.SpawnBlock();
        }
        if(args[0].contains("open")){
            LootBox.TestFor(p);
        }
        if(args[0].contains("reload")){
            main.reLoad();
        }
        p.sendMessage("Complated.");
        return true;
    }
}
