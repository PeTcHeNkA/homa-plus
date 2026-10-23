package plus;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import plus.entity.Te4ActiveModel;
import plus.entity.Te4ModelledEntity;
import plus.util.Commands.SubCommand;
import plus.util.Commands.YandeReCommand;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class main extends JavaPlugin {


    public main(){
        new Commands(this);
    }
}


class Commands extends YandeReCommand{
    public JavaPlugin plugin;
    public Commands(JavaPlugin p) {
        super("cs", "create ME statue", Arrays.asList("sg"));
        plugin = p;
        addSubCommand(new SubCommand("create") {
            final List<String> tab = Arrays.asList("pose");
            @Override
            public SubCommand[] SubCommands() {
                return new SubCommand[0];
            }

            @Override
            public List<String> onTabComplate(CommandSender commandSender, String modelId, List<String> args) {
                return tab;
            }

            @Override
            public void onCommand(CommandSender commandSender, String modelId, List<String> args) {
                Player p = (Player) commandSender;

                ArmorStand bukkitTarget = (ArmorStand) p.getLocation().getWorld()
                        .spawnEntity(p.getLocation(), EntityType.ARMOR_STAND);

                UUID uid = bukkitTarget.getUniqueId();


                if (ModelEngineAPI.api.getModelManager().getModeledEntity(uid) != null)
                    ModelEngineAPI.api.getModelManager().removeModeledEntity(uid);

                Te4ModelledEntity en = new Te4ModelledEntity(bukkitTarget, args.get(0)/*state*/);
                Te4ActiveModel model = new Te4ActiveModel(modelId);
                en.addActiveModel(model);

                //model.showModel(p);
                en.addToParentPlayer(p);
                System.out.println("Phantom created! uid: "+uid);
                int countTick = args.size()>1?Integer.parseInt(args.get(1)):0;
                if(countTick>0) {
                    Thread t = new Thread(() -> {
                        for(int i = 0;i<countTick;i++) {
                            en.parentTick();
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        Thread.currentThread().stop();
                    });
                    t.start();
                }
            }
        });
    }
}