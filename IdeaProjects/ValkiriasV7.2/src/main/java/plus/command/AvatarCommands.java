package plus.command;

import org.bukkit.command.CommandSender;
import plus.Main;
import plus.util.ConfigHelper;
import plus.util.cmd.CommandArgs;
import plus.util.cmd.SubCommand;
import plus.util.cmd.YandeReCommand;
import plus.valcirias.Valkiria;
import plus.valcirias.avatar.AvatarFloader;
import plus.valcirias.avatar.AvatarSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AvatarCommands extends YandeReCommand {
    private final ArrayList<String> sets = new ArrayList<>(AvatarFloader.getAvatarSetNames());
    public AvatarCommands() {
        super("avatar", "avatar utilites", Arrays.asList("av", "ava"));
        addSubCommand(
                new SubCommand("set") {
                    @Override
                    public SubCommand[] injectSubCommands() {
                        return new SubCommand[0];
                    }

                    @Override
                    public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                        if(args.size()==1) return sets;
                        else return null;
                    }

                    @Override
                    public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                        Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                        AvatarSet set = AvatarFloader.getAvatarSet(string);
                        if (set == null) return;
                        int number = 1;
                        if (!args.isEmpty()) {
                            try {
                                number = Integer.parseInt(args.get(0));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        valkiria.setAvatar(set.getAvatar(number));
                    }
                },
                new SubCommand("page") {
                    @Override
                    public SubCommand[] injectSubCommands() {
                        return new SubCommand[0];
                    }

                    @Override
                    public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                        return sets;
                    }

                    @Override
                    public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                        AvatarSet set = AvatarFloader.getAvatarSet(string);
                        if (set == null) return;
                        int page = 1;
                        if (!args.isEmpty()) {
                            try {
                                page = Integer.parseInt(args.get(0));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        commandSender.spigot().sendMessage(set.getAvatarListView(page));
                    }

                    @Override
                    public String[] aliaces(){return new String[]{"p", "pg"};}
                },
                new SubCommand("setcolor") {
                    @Override
                    public SubCommand[] injectSubCommands() {
                        return new SubCommand[0];
                    }
                    final List<String> noPremium = Arrays.asList("Hey! Color codes only for premium!");
                    final List<String> colors = Arrays.asList(
                            "&a", "&b", "&c", "&d", "&e", "&f",
                            "&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9"
                    );
                    @Override
                    public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                        Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                        if(valkiria.isPremium())return colors;
                        else return noPremium;
                    }


                    @Override
                    public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                        Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                        if(!valkiria.isPremium()){
                            commandSender.spigot().sendMessage(ConfigHelper.messageOnlyPremium);
                            return;
                        }
                        if(string==null)return;
                        if(!colors.contains(string))return;
                        valkiria.setColor(string.substring(1));
                        commandSender.spigot().sendMessage(ConfigHelper.messageColorEdited);
                    }


                    @Override
                    public String[] aliaces(){return new String[]{"color"};}
                },

                new SubCommand("help") {
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
                        commandSender.spigot().sendMessage(ConfigHelper.helpAvatar);
                    }

                    @Override
                    public String[] aliaces(){return new String[]{"h"};}
                }
        );
    }
}