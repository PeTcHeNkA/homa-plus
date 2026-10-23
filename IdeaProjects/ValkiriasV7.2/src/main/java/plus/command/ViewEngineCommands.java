package plus.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plus.util.cmd.CommandArgs;
import plus.util.cmd.SubCommand;
import plus.util.cmd.YandeReCommand;
import plus.quest.motion.BasicPos;
import plus.quest.motion.TpMotion;
import plus.quest.Scene;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ViewEngineCommands extends YandeReCommand {

    public ViewEngineCommands() {
        super("view_engine", "tests, run and debug scenes", Arrays.asList("veng"));

        addSubCommand(
                new SubCommand("run") {

                    @Override
                    public SubCommand[] injectSubCommands() {
                        return new SubCommand[0];
                    }

                    @Override
                    public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                        return Scene.getListScenes();
                    }

                    @Override
                    public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                        if (commandSender instanceof Player player) {
                            if (string == null) {
                                commandSender.sendMessage("scene not defined!");
                                return;
                            }
                            Scene scene = Scene.get(string);
                            if (scene == null) {
                                commandSender.sendMessage("scene not founded!");
                                return;
                            }

                            BasicPos pos;
                            if (args.size() > 0) pos = TpMotion.build(args.get(0));
                            else pos = TpMotion.build(player.getLocation());

                            scene.run(player, pos);

                        } else commandSender.sendMessage("Allowed only for players!");
                    }
                },
                new SubCommand("runt"){
                    @Override
                    public SubCommand[] injectSubCommands() {
                        return new SubCommand[0];
                    }

                    @Override
                    public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                        return Scene.getListScenes();
                    }

                    @Override
                    public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                        if (commandSender instanceof Player player) {
                            if (string == null) {
                                commandSender.sendMessage("scene not defined!");
                                return;
                            }
                            Scene scene = Scene.get(string);
                            if (scene == null) {
                                commandSender.sendMessage("scene not founded!");
                                return;
                            }

                            BasicPos pos;
                            if (args.size() > 0) pos = TpMotion.build(args.get(0));
                            else pos = TpMotion.build(player.getLocation());
                            long time = System.currentTimeMillis();
                            scene.run(player, pos);
                            time = System.currentTimeMillis() - time;
                            commandSender.sendMessage(String.format("| time: %sms",time));

                        } else commandSender.sendMessage("Allowed only for players!");
                    }
                },

                new SubCommand("list") {
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
                        commandSender.sendMessage("| "+String.join("\n| ", Scene.getListScenes()));
                    }
                }
        );
    }


    private final List<String> noPerms = Collections.singletonList("This allowed only for operators!");
    @Override
    public void exec(CommandSender commandSender, CommandArgs args){
        if(!commandSender.isOp()){
            commandSender.sendMessage(noPerms.get(0));
        } else super.exec(commandSender, args);
    }


    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] strings){
        if(!sender.isOp()) {
            return noPerms;
        } else return super.tabComplete(sender, alias, strings);
    }
}