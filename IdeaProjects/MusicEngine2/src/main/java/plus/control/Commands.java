package plus.control;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plus.music.BSound;
import plus.music.MCategory;
import plus.music.MusicSession;
import plus.utils.commands.SubCommand;
import plus.utils.commands.YandeReCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Commands extends YandeReCommand {

    public Commands() {
        super("mus", "mus", "music utilities", Arrays.asList("music"));
        addSubCommand(
                new SubCommand("reload") {
                    @Override
                    public SubCommand[] SubCommands() {
                        return new SubCommand[0];
                    }

                    @Override
                    public List<String> onTabComplate(CommandSender commandSender, String string, List<String> args) {
                        return null;
                    }

                    @Override
                    public void onCommand(CommandSender commandSender, String string, List<String> args) {
                        try {
                            MCategory.loadStatic();
                            commandSender.sendMessage("Reloaded!");
                        } catch (Exception e) {
                            e.printStackTrace();
                            commandSender.sendMessage("Critical error! See console.");
                        }
                    }
                },
                new SubCommand("session") {
                    @Override
                    public SubCommand[] SubCommands() {
                        return new SubCommand[]{
                                new SubCommand("start") {
                                    @Override
                                    public SubCommand[] SubCommands() {
                                        return new SubCommand[0];
                                    }

                                    @Override
                                    public List<String> onTabComplate(CommandSender commandSender, String string, List<String> args) {
                                        return null;
                                    }

                                    @Override
                                    public void onCommand(CommandSender commandSender, String string, List<String> args) {
                                        Player target;
                                        if(string==null){
                                            if(commandSender instanceof Player p){
                                                target = p;
                                            } else {
                                                commandSender.sendMessage("Target not founded!");
                                                return;
                                            }
                                        } else {
                                            target = Bukkit.getPlayer(string);
                                            if(target==null){
                                                commandSender.sendMessage("Target not founded!");
                                                return;
                                            }
                                        }
                                        target.sendMessage("Started.");
                                        MusicSession.startNew(target);
                                    }
                                },
                                new SubCommand("stop") {
                                    @Override
                                    public SubCommand[] SubCommands() {
                                        return new SubCommand[0];
                                    }

                                    @Override
                                    public List<String> onTabComplate(CommandSender commandSender, String string, List<String> args) {
                                        return null;
                                    }

                                    @Override
                                    public void onCommand(CommandSender commandSender, String string, List<String> args) {
                                        Player target;
                                        if(string==null){
                                            if(commandSender instanceof Player p){
                                                target = p;
                                            } else {
                                                commandSender.sendMessage("Target not founded!");
                                                return;
                                            }
                                        } else {
                                            target = Bukkit.getPlayer(string);
                                            if(target==null){
                                                commandSender.sendMessage("Target not founded!");
                                                return;
                                            }
                                        }
                                        target.sendMessage("Stopped.");
                                        MusicSession.stopFor(target);
                                    }
                                }
                        };
                    }

                    @Override
                    public List<String> onTabComplate(CommandSender commandSender, String string, List<String> args) {
                        return null;
                    }

                    @Override
                    public void onCommand(CommandSender commandSender, String string, List<String> args) {

                    }
                },
                new SubCommand("play") {
                    @Override
                    public SubCommand[] SubCommands() {
                        return new SubCommand[0];
                    }

                    @Override
                    public List<String> onTabComplate(CommandSender commandSender, String string, List<String> args) {
                        return null;
                    }

                    @Override
                    public void onCommand(CommandSender commandSender, String string, List<String> args) {
                        Player target;
                        if(commandSender instanceof Player p){
                            target = p;
                        } else {
                            commandSender.sendMessage("Allowed only for players!");
                            return;
                        }
                        try {
                            MusicSession.stopFor(target);
                            Integer i = Integer.parseInt(string);

                            ArrayList<BSound> s = MCategory.valueOf(i).getSounds();

                            BSound sound = s.get(Integer.parseInt(args.get(0)));
                            sound.play(target);

                            commandSender.sendMessage(
                                    String.format("Played [%s], category = [%s]", sound.getSound(), sound.getCategory().id)
                            );
                        } catch (Exception e){
                            commandSender.sendMessage("Critical error! see console.");
                            e.printStackTrace();;
                        }
                    }
                }
        );
    }


    @Override
    public boolean execute(CommandSender commandSender, String string, String[] strings) {
        if(commandSender.isOp())return super.execute(commandSender, string, strings);
        else {
            commandSender.sendMessage("Hey! this allowed only for admins!");
            return true;
        }
    }


    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args){
        if(sender.isOp()){
            return super.tabComplete(sender, alias, args);
        } else {
            return Collections.singletonList("Hey! this allowed only for admins!");
        }
    }
}